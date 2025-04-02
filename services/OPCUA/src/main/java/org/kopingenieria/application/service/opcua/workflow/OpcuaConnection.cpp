#include <exception>
#include <optional>
#include <chrono>
#include <future>
#include <map>
#include <memory>
#include <stdexcept>
#include <string>
#include <thread>
#include <functional>

class OpcUaConnectionResponse;
class OpcUaUserPoolManager;
class OpcUaClient;
class UaClient;

enum class ConnectionStatus {
    UNKNOWN,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
    RECONNECTION_FAILED,
    FAILED,
    ERROR,
    NO_RESPONSE
};

class OpcuaConnection {
private:
    static const int MAX_RETRIES = 3;
    static const int INITIAL_WAIT = 1000;
    static constexpr double BACKOFF_FACTOR = 2.0;
    static const int LINEAR_RETRY_INTERVAL = 5000;
    static const int CONNECTION_TIMEOUT = 10000;

    OpcUaUserPoolManager* poolManager;
    std::shared_ptr<OpcUaClient> pooledClient;
    UrlType lastConnectedUrl;
    std::atomic<ConnectionStatus> currentStatus;
    std::chrono::system_clock::time_point lastActivityTime;
    UserConfiguration loadConfiguration;

public:
    OpcuaConnection() {
        currentStatus = ConnectionStatus::UNKNOWN;
        lastActivityTime = std::chrono::system_clock::now();
        // Assuming UserConfigFile and related classes are adequately ported
        UserConfigFile configFile;
        loadConfiguration = configFile.loadConfiguration(configFile.extractExistingFilename());
    }

    std::future<OpcUaConnectionResponse> connect() {
        if (!lastConnectedUrl.isValid()) {
            throw std::runtime_error("No hay URL disponible para la conexión");
        }
        return connect(lastConnectedUrl);
    }

    std::future<OpcUaConnectionResponse> connect(UrlType url) {
        validateUrl(url);
        lastConnectedUrl = url;
        updateConnectionStatus(ConnectionStatus::CONNECTING);
        updateLastActivity();

        try {
            auto optionalClient = poolManager->obtenerCliente(loadConfiguration);

            if (!optionalClient) {
                throw std::runtime_error("No se pudo obtener un cliente del pool");
            }

            pooledClient = optionalClient.value();
            auto opcUaClient = pooledClient->getClient();

            return std::async(std::launch::async, [&]() {
                if (!validateConnection(url, opcUaClient)) {
                    throw std::runtime_error("Validación de conexión fallida");
                }
                return connectClient(opcUaClient).get();
            }).then([this](auto result) {
                return result;
            });
        } catch (const std::exception& e) {
            throw std::runtime_error(std::string("Error en la conexión OPC UA: ") + e.what());
        }
    }

    std::future<OpcUaConnectionResponse> disconnect() {
        if (!pooledClient) {
            return std::async(std::launch::deferred, [&]() {
                return createConnectionResponse(ConnectionStatus::DISCONNECTED);
            });
        }

        return pooledClient->getClient()->disconnect().then([this](auto) {
            cleanup();
            return createConnectionResponse(ConnectionStatus::DISCONNECTED);
        }).handle([this](const std::exception& ex) {
            cleanup();
            std::rethrow_exception(std::make_exception_ptr(std::runtime_error("Error durante la desconexión")));
        });
    }

    std::future<OpcUaConnectionResponse> backoffreconnection() {
        if (!lastConnectedUrl.isValid()) {
            throw std::runtime_error("No hay URL previa disponible para la reconexión");
        }
        return backoffreconnection(lastConnectedUrl);
    }

    std::future<OpcUaConnectionResponse> backoffreconnection(UrlType url) {
        validateUrl(url);
        cleanup();
        return attemptBackoffReconnection(url, 1, INITIAL_WAIT);
    }

    std::future<OpcUaConnectionResponse> linearreconnection() {
        if (!lastConnectedUrl.isValid()) {
            throw std::runtime_error("No hay URL previa disponible para la reconexión");
        }
        return linearreconnection(lastConnectedUrl);
    }

    std::future<OpcUaConnectionResponse> linearreconnection(UrlType url) {
        validateUrl(url);
        cleanup();
        return attemptLinearReconnection(url, 1);
    }

    std::future<OpcUaConnectionResponse> ping() {
        if (!pooledClient || !pooledClient->isConnected()) {
            throw std::runtime_error("Cliente OPC UA no conectado");
        }

        NodeId pingNodeId = NodeId::parse("ns=0;i=2259");
        return pooledClient->getClient()->readValue(0, TimestampsToReturn::Both, pingNodeId).then([this](auto value) {
            return handlePingResponse(value);
        }).handle([this](const std::exception& ex) {
            return createConnectionResponse(ConnectionStatus::ERROR);
        });
    }

    void close() {
        cleanup();
    }

private:
    std::future<OpcUaConnectionResponse> attemptBackoffReconnection(UrlType url, int retryCount, double waitTime) {
        if (retryCount > MAX_RETRIES) {
            updateConnectionStatus(ConnectionStatus::RECONNECTION_FAILED);
            return std::async(std::launch::deferred, [&]() {
                return createConnectionResponse(ConnectionStatus::RECONNECTION_FAILED);
            });
        }

        return std::async(std::launch::deferred, [&]() {
            std::this_thread::sleep_for(std::chrono::milliseconds(static_cast<long>(waitTime)));
            return connect(url).then([this, url, retryCount, waitTime](auto response) {
                if (response.getStatus() == ConnectionStatus::CONNECTED) {
                    return response;
                }
                return attemptBackoffReconnection(url, retryCount + 1, waitTime * BACKOFF_FACTOR).get();
            }).get();
        });
    }

    std::future<OpcUaConnectionResponse> attemptLinearReconnection(UrlType url, int retryCount) {
        if (retryCount > MAX_RETRIES) {
            updateConnectionStatus(ConnectionStatus::RECONNECTION_FAILED);
            return std::async(std::launch::deferred, [&]() {
                return createConnectionResponse(ConnectionStatus::RECONNECTION_FAILED);
            });
        }

        return std::async(std::launch::deferred, [&]() {
            std::this_thread::sleep_for(std::chrono::milliseconds(LINEAR_RETRY_INTERVAL));
            return connect(url).then([this, url, retryCount](auto response) {
                if (response.getStatus() == ConnectionStatus::CONNECTED) {
                    return response;
                }
                return attemptLinearReconnection(url, retryCount + 1).get();
            }).get();
        });
    }

    bool validateConnection(UrlType url, OpcUaClient* client) {
        return validatorConnection->validateActiveSession(client) &&
               validatorConnection->validateLocalHost(url.getUrl());
    }

    std::future<OpcUaConnectionResponse> connectClient(OpcUaClient* client) {
        return client->connect().then([this](auto connection) {
            updateConnectionStatus(ConnectionStatus::CONNECTED);
            return createConnectionResponse(ConnectionStatus::CONNECTED, connection);
        });
    }

    OpcUaConnectionResponse handlePingResponse(DataValue value) {
        updateLastActivity();
        if (value.isValid()) {
            updateConnectionStatus(ConnectionStatus::CONNECTED);
            return createConnectionResponse(ConnectionStatus::CONNECTED);
        }
        updateConnectionStatus(ConnectionStatus::NO_RESPONSE);
        return createConnectionResponse(ConnectionStatus::NO_RESPONSE);
    }

    void validateUrl(UrlType url) {
        if (!url.isValid()) {
            throw std::runtime_error("La URL no puede ser nula o vacía");
        }
    }

    OpcUaConnectionResponse createConnectionResponse(ConnectionStatus status) {
        return createConnectionResponse(status, nullptr);
    }

    OpcUaConnectionResponse createConnectionResponse(ConnectionStatus status, UaClient* client) {
        updateLastActivity();
        auto opcUaClient = pooledClient ? pooledClient->getClient() : nullptr;
        return OpcUaConnectionResponse::builder()
            .endpointUrl(opcUaClient ? opcUaClient->getConfig()->getEndpoint()->getEndpointUrl() : "")
            .applicationName(opcUaClient ? opcUaClient->getConfig()->getApplicationName()->getText() : "")
            .applicationUri(opcUaClient ? opcUaClient->getConfig()->getApplicationUri() : "")
            .productUri(opcUaClient ? opcUaClient->getConfig()->getProductUri() : "")
            .status(status)
            .lastActivity(lastActivityTime)
            .client(client)
            .build();
    }

    void updateConnectionStatus(ConnectionStatus status) {
        currentStatus = status;
        updateLastActivity();
    }

    void updateLastActivity() {
        lastActivityTime = std::chrono::system_clock::now();
    }

    void cleanup() {
        if (pooledClient) {
            poolManager->liberarCliente(pooledClient);
            pooledClient.reset();
        }
        updateConnectionStatus(ConnectionStatus::DISCONNECTED);
    }
};