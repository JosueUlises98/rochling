package org.kopingenieria.services;


import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kopingenieria.exceptions.ConnectionException;
import org.kopingenieria.exceptions.SSLConnectionException;
import org.kopingenieria.model.SSLConfigurations;
import org.kopingenieria.model.UrlType;
import org.kopingenieria.tools.ConfigurationLoader;
import org.kopingenieria.validators.ValidatorConexion;

import javax.net.ssl.*;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;


public class SSLConnection extends ConnectionService {
    /**
     * Logger instance used for logging informational, warning, and error messages
     * related to the operations of the PlcConnect class.
     * <p>
     * This logger is configured to track critical events such as connection requests,
     * connection status updates, error handling, and other operational details within the class.
     * It facilitates debugging and monitoring by providing a detailed audit trail of
     * activities and errors encountered during runtime.
     */
    private static final Logger logger = LogManager.getLogger(SSLConnection.class);
    /**
     * The MAX_RETRIES constant represents the maximum number of retry attempts
     * allowed when trying to establish or re-establish a connection with a
     * Programmable Logic Controller (PLC) or other external systems.
     * <p>
     * This constant is used to control the number of reconnection attempts before
     * considering the operation as failed, ensuring that the connection handling
     * logic adheres to a defined retry policy.
     * <p>
     * Value: 10
     */
    private static final int MAX_RETRIES;
    /**
     * The INITIAL_WAIT constant specifies the initial waiting time in milliseconds
     * before performing retry attempts when establishing or re-establishing a connection in the PlcConnect class.
     * <p>
     * This value is used as the base delay for implementing connection retry logic.
     * It helps in spacing out retry attempts to avoid overwhelming the system or remote server.
     * The determination of this value depends on the typical responsiveness of the system being connected to.
     */
    private static final int INITIAL_WAIT; // Milisegundos
    /**
     * The BACKOFF_FACTOR is a constant multiplier used to increase the wait time between
     * connection retry attempts in case of failures when establishing or re-establishing
     * connections to a PLC or other external systems.
     * <p>
     * This factor implements an exponential backoff strategy where the delay between
     * retry attempts grows exponentially by multiplying the initial wait time by this
     * factor after each failure.
     * <p>
     * For example, with a BACKOFF_FACTOR of 2.0, the wait time for retries would double
     * after each unsuccessful connection attempt.
     * <p>
     * It helps mitigate potential system overloads or throttling issues caused by frequent
     * retry attempts and improves system stability and reliability.
     */
    private static final double BACKOFF_FACTOR;
    /**
     * Represents the predetermined wait time in seconds between connection attempts,
     * especially used in backoff or linear reconnection strategies.
     * <p>
     * This value is utilized to control the delay before retrying a connection attempt
     * in scenarios where previous attempts have failed. It assists in maintaining a
     * controlled load on the server and avoiding excessive immediate retries.
     * <p>
     * The {@code WAIT_TIME} is defined as a constant to ensure uniformity of usage
     * and prevent accidental alteration during runtime.
     */
    private static final double WAIT_TIME;
    /**
     * Represents the initial number of retry attempts for reconnection in the {@code TcpConnection} class.
     * This constant defines the starting point for retry mechanisms when attempting
     * to re-establish a connection, particularly in backoff or linear reconnection strategies.
     * <p>
     * The value of {@code INITIAL_RETRY} serves as a fundamental configuration to control the behavior
     * of the retry logic and provides a baseline for subsequent retries.
     */
    private static final int INITIAL_RETRY;
    /**
     * Represents an instance of {@link ValidatorConexion} used to perform validation operations related
     * to OPC UA client configurations and server connectivity within the {@code ConexionClienteService}.
     * <p>
     * This field acts as a utility for ensuring the integrity and preconditions of the operations
     * performed, covering aspects such as validating server endpoints, checking client availability,
     * and facilitating preparatory validation prior to establishing a connection.
     */
    private ValidatorConexion validatorConection;
    /**
     * Represents the server endpoint URL for establishing TCP connections.
     * <p>
     * This field is used to initialize, manage, and maintain communication
     * with a predefined OPC-UA server. The {@code url} is of type {@link UrlType},
     * which defines strongly typed references to specific server addresses.
     * <p>
     * Typically configured during the initialization of the {@code TcpConnection} class
     * and may be used in connection management tasks such as establishing, reconnecting,
     * or verifying server connections.
     */
    private UrlType targeturl;

    private static final int DEFAULT_CONNECTION_TIMEOUT;

    private static final int DEFAULT_READ_TIMEOUT;

    private static final int DEFAULT_WRITE_TIMEOUT;

    private static final int MAX_RETRY_ATTEMPTS;

    private static final int PING_TIMEOUT;

    private final SSLContext sslContext;

    private volatile SSLSocket sslSocket;

    private final Properties conf;

    private final CertificateValidator certificateValidator;

    private volatile UrlType currentUrl;

    private volatile boolean isConnected;

    private volatile SSLSession session;

    static {
        Properties properties = ConfigurationLoader.loadProperties("sslconnection.properties");
        INITIAL_RETRY = Integer.parseInt(properties.getProperty("initial_retry", "0"));
        MAX_RETRIES = Integer.parseInt(properties.getProperty("max_retries", "10"));
        INITIAL_WAIT = Integer.parseInt(properties.getProperty("initial_wait", "1000"));
        BACKOFF_FACTOR = Double.parseDouble(properties.getProperty("backoff_factor", "2.0"));
        WAIT_TIME = Double.parseDouble(properties.getProperty("wait_time", "3000"));
        DEFAULT_CONNECTION_TIMEOUT = Integer.parseInt(properties.getProperty("default_connection_timeout", "30000"));
        DEFAULT_READ_TIMEOUT = Integer.parseInt(properties.getProperty("default_read_timeout", "30000"));
        DEFAULT_WRITE_TIMEOUT = Integer.parseInt(properties.getProperty("default_write_timeout", "30000"));
        MAX_RETRY_ATTEMPTS = Integer.parseInt(properties.getProperty("max_retry_attempts", "3"));
        PING_TIMEOUT = Integer.parseInt(properties.getProperty("ping_timeout", "5000"));
    }

    public SSLConnection(SSLConfigurations config) throws SSLConnectionException {
        this.conf = loadSSLConfiguration(config);
        this.certificateValidator = createCertificateValidator();
        this.sslContext = initializeSSLContext();
        configureSecurity();
    }

    private void configureSecurity() throws SSLConnectionException {
        try {
            System.setProperty("javax.net.ssl.keyStore", conf.getProperty("keystore.path"));
            System.setProperty("javax.net.ssl.keyStorePassword", conf.getProperty("keystore.password"));
            System.setProperty("javax.net.ssl.trustStore", conf.getProperty("truststore.path"));
            System.setProperty("javax.net.ssl.trustStorePassword", conf.getProperty("truststore.password"));
        } catch (Exception e) {
            logger.error("Error configurando propiedades de seguridad SSL", e);
            throw new SSLConnectionException("Error en la configuracion de seguridad", e);
        }
    }

    private CertificateValidator createCertificateValidator() throws SSLConnectionException {
        try {
            return new CertificateValidator();
        } catch (Exception e) {
            throw new SSLConnectionException("Error creating certificate validator", e);
        }
    }

    private Properties loadSSLConfiguration(SSLConfigurations sslConfig) {
        Properties config = new Properties();
        config.setProperty("keystore.path", sslConfig.keystorePath());
        config.setProperty("keystore.password", sslConfig.keystorePassword());
        config.setProperty("truststore.path", sslConfig.truststorePath());
        config.setProperty("truststore.password", sslConfig.truststorePassword());
        return config;
    }

    private SSLContext initializeSSLContext() throws SSLConnectionException {
        try {
            KeyManager[] keyManagers = configureKeyManagers();
            TrustManager[] trustManagers = configureTrustManagers();
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(keyManagers, trustManagers, new SecureRandom());
            return context;
        } catch (Exception e) {
            throw new SSLConnectionException("Error al inicializar el contexto", e.getCause());
        }
    }

    private KeyManager[] configureKeyManagers() throws SSLConnectionException {
        String keystorePath = conf.getProperty("keystore.path");
        String keystorePassword = conf.getProperty("keystore.password");
        String keystoreType = conf.getProperty("keystore.type", "PKCS12");

        try (InputStream is = Files.newInputStream(Paths.get(keystorePath))) {
            KeyStore keyStore = KeyStore.getInstance(keystoreType);
            keyStore.load(is, keystorePassword.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keystorePassword.toCharArray());

            return kmf.getKeyManagers();
        } catch (IOException | KeyStoreException | UnrecoverableKeyException | CertificateException |
                 NoSuchAlgorithmException e) {
            throw new SSLConnectionException("Error en la configuracion en el gestor de claves", e);
        }
    }

    private TrustManager[] configureTrustManagers() throws SSLConnectionException {

        String truststorePath = conf.getProperty("truststore.path");
        String truststorePassword = conf.getProperty("truststore.password");
        String truststoreType = conf.getProperty("truststore.type", "PKCS12");

        try (InputStream is = Files.newInputStream(Paths.get(truststorePath))) {
            KeyStore trustStore = KeyStore.getInstance(truststoreType);
            trustStore.load(is, truststorePassword.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            return createCustomTrustManager(tmf);
        } catch (IOException | KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            throw new SSLConnectionException("Error en la configuracion de los trust managers", e);
        }
    }

    private TrustManager[] createCustomTrustManager(TrustManagerFactory tmf) {
        return new TrustManager[]{
                new X509ExtendedTrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }

                    private final X509TrustManager delegate = (X509TrustManager) tmf.getTrustManagers()[0];

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) throws CertificateException {
                        certificateValidator.validateCertificateChain(chain);
                        delegate.checkClientTrusted(chain, authType);
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) {

                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) {

                    }

                    // Implementar otros métodos requeridos del X509ExtendedTrustManager
                }
        };
    }

    private SSLSocket createAndConfigureSocket(UrlType url) throws SSLConnectionException {
        SSLSocket socket;
        try {
            socket = (SSLSocket) sslContext.getSocketFactory().createSocket(url.getUrl(), 4840);
            configureSSLSocket(socket);
        } catch (IOException e) {
            logger.error("Error creando socket SSL", e);
            throw new SSLConnectionException("Error creando socket SSL", e);
        }
        return socket;
    }

    private void configureSSLSocket(SSLSocket socket) throws SSLConnectionException {
        try {
            socket.setEnabledProtocols(new String[]{"TLSv1.3", "TLSv1.2"});
            socket.setEnabledCipherSuites(getSecureCipherSuites());
            socket.setSoTimeout(DEFAULT_READ_TIMEOUT);
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            socket.setSoLinger(true, 0);
            socket.setNeedClientAuth(true);
            socket.setWantClientAuth(true);
        } catch (SocketException e) {
            logger.error("Error en la configuracion del socket SSL", e);
            throw new SSLConnectionException("Error en la configuracion del socket SSL", e);
        }
    }

    private String[] getSecureCipherSuites() {
        return new String[]{
                "TLS_AES_256_GCM_SHA384",
                "TLS_CHACHA20_POLY1305_SHA256",
                "TLS_AES_128_GCM_SHA256"
        };
    }

    @PreDestroy
    public void shutdown() throws SSLConnectionException {
        try {
            disconnect().get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error durante la desconexion: ", e);
            throw new SSLConnectionException("Error durante la desconexion",e.getCause());
        }
    }

    private void performSSLHandshake() throws SSLConnectionException {
        try {
            sslSocket.startHandshake();
            session = sslSocket.getSession();
            logger.debug("Protocolo negociado: {}", session.getProtocol());
            logger.debug("Cipher Suite: {}", session.getCipherSuite());
            validateSSLSession(session);
        } catch (Exception e) {
            logger.error("Error en el handshake SSL", e.getCause());
            throw new SSLConnectionException("Error en el handshake SSL", e.getCause());
        }
    }

    private void validateSSLSession(SSLSession session) throws SSLConnectionException {
        try {
            Certificate[] certs = session.getPeerCertificates();
            certificateValidator.validateCertificates(certs);
        } catch (Exception e) {
            try {
                throw new SSLHandshakeException("Error en la validacion de certificados: " + e.getCause().getMessage(), e.getCause());
            } catch (SSLHandshakeException ex) {
                logger.error("Error en el handshake de conexion", ex.getCause());
                throw new SSLConnectionException("Error en validacion de sesion", ex.getCause());
            }
        }
    }

    private void scheduleHealthCheck() throws SSLConnectionException {
        try {
            if (isConnected) {
                ping()
                        .thenAccept(isAlive -> {
                            if (!isAlive) {
                                try {
                                    handleConnectionLoss();
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        })
                        .exceptionally(throwable -> {
                            logger.error("Health check failed: ", throwable);
                            return null;
                        });
            }
        }catch (Exception e){
            logger.error("Error en el health check connection: {}", e.getMessage());
            throw new SSLConnectionException("Error en el health check connection",e.getCause());
        }
    }

    private void startHeartbeat() throws SSLConnectionException {
        try {
            if (!isConnectionAlive()) {
                handleConnectionLoss();
            }
        } catch (Exception e) {
            logger.error("Error en el monitoreo de conexion activa: ", e);
            throw new SSLConnectionException("Error en el monitoreo de conexion activa",e.getCause());
        }
    }

    @Override
    public void close() throws SSLConnectionException {
        shutdown();
    }

    private boolean isConnectionAlive(){
        try {
            sslSocket.getOutputStream().write(0);
            return true;
        } catch (IOException e) {
            logger.error("Error al enviar ping: ", e);
            return false;
        }
    }

    private void handleConnectionLoss() throws SSLConnectionException {
        try {
            if (!isConnected) {
                logger.warn("Conexion perdida. Iniciando reconexion...");
                backoffreconnection(currentUrl)
                        .thenAccept(reconnected -> {
                            if (!reconnected) {
                                logger.error("Reconnexion fallida");
                            }
                            isConnected = true;
                        })
                        .exceptionally(throwable -> {
                            logger.error("Error de reconexion: ", throwable);
                            return null;
                        });
            }
        } catch (Exception e) {
            logger.error("Error en el monitoreo de conexion activa: ", e);
            throw new SSLConnectionException("Error en el monitoreo de conexion activa",e.getCause());
        }
    }

    private boolean executePing() throws SSLConnectionException {
        CountDownLatch responseLatch = new CountDownLatch(1);
        AtomicBoolean pingSuccess = new AtomicBoolean(false);
        try {
            sslSocket.setSoTimeout(PING_TIMEOUT);

            CompletableFuture.runAsync(() -> {

                try (OutputStream out = sslSocket.getOutputStream();
                     InputStream in = sslSocket.getInputStream()) {

                    out.write(0);
                    out.flush();
                    int response = in.read();
                    pingSuccess.set(response != -1);
                    responseLatch.countDown();
                } catch (IOException e) {
                    logger.error("Error durante el ping: ", e);
                    responseLatch.countDown();
                }
            });
            return responseLatch.await(PING_TIMEOUT, TimeUnit.MILLISECONDS) &&
                    pingSuccess.get();
        } catch (Exception e) {
            throw new SSLConnectionException("Error durante el ping", e.getCause());
        } finally {
            try {
                sslSocket.setSoTimeout(DEFAULT_READ_TIMEOUT);
            } catch (SocketException e) {
                logger.error("Error al restablecer el timeout de lectura del socket SSL", e.getCause());
            }
        }
    }

    private boolean tryReconnect(UrlType url) {
        try {
            disconnect().get(5, TimeUnit.SECONDS);
            return connect(url).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Intento de reconexión fallido: {}", e.getMessage());
            return false;
        }
    }

    private void validateConnectionParameters(UrlType url) throws SSLConnectionException{
        if (url == null) {
            throw new SSLConnectionException("Url no especificada");
        } else if (!Objects.equals(url.getProtocol(), "https")) {
            throw new SSLConnectionException("Url no es HTTPS");
        }
        // Validar certificados y revocación
        try {
            certificateValidator.validateCertificates(session.getPeerCertificates());
        } catch (Exception e) {
            throw new SSLConnectionException("Cadena de certificados inválida", e);
        }
    }

    private Boolean executeReconnectionStrategy(UrlType url, Supplier<Boolean> shouldContinue, Supplier<Long> getDelay) throws SSLConnectionException {
        try {
            while (shouldContinue.get()) {
                if (tryReconnect(url)) {
                    return true;
                }
                Thread.sleep(getDelay.get());
            }
        }catch (Exception e){
            logger.error("Error en la reconnection strategy: {}", e.getMessage());
            throw new SSLConnectionException("Error en la reconnection strategy",e.getCause());
        }
        return false;
    }

    private void handleConnectionError(Exception e){
        logger.error("Error de conexión: {}", e.getMessage());
        isConnected = false;
    }

    public CompletableFuture<Boolean> connect() throws SSLConnectionException {
        if (currentUrl == null) {
            throw new SSLConnectionException("No se ha especificado una URL para la conexión");
        }
        return connect(currentUrl);
    }

    public CompletableFuture<Boolean> connect(UrlType url) throws Exception {
        return CompletableFuture.supplyAsync(() -> {
            try {
                    if (isConnected) {
                        logger.warn("Ya existe una conexión activa");
                        return true;
                    }
                    validateConnectionParameters(url);
                    currentUrl = url;
                    configureSSLSocket(sslSocket);
                    performSSLHandshake();
                    startHeartbeat();
                    isConnected = true;
                    logger.info("Conexión SSL establecida exitosamente con: {}", url.getIpAddress());
                    return true;
            } catch (Exception e) {
                handleConnectionError(e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> disconnect() throws SSLConnectionException {
        try {
            return CompletableFuture.supplyAsync(() -> {
                    if (!isConnected) {
                        logger.warn("No hay conexión activa para desconectar");
                        return true;
                    }
                    if (sslSocket != null && !sslSocket.isClosed()) {
                        try {
                            sslSocket.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    isConnected = false;
                    logger.info("Conexión SSL cerrada correctamente");
                    return true;
            });
        }catch (Exception e){
            logger.error("Error en la desconexion: {}", e.getMessage());
            throw new SSLConnectionException("Error en la desconexion",e.getCause());
        }
    }

    @Override
    public CompletableFuture<Boolean> backoffreconnection(UrlType url) throws Exception {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int attempt = 0;
                long delay = 1000; // Delay inicial de 1 segundo

                while (attempt < MAX_RETRY_ATTEMPTS) {
                    if (tryReconnect(url)) {
                        return true;
                    }
                    Thread.sleep(delay);
                    delay *= 2; // Incremento exponencial del delay
                    attempt++;
                }
                return false;
            } catch (Exception e) {
                logger.error("Error en reconexión con backoff: {}", e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> backoffreconnection() throws Exception {
        if (currentUrl == null) {
            throw new ConnectionException("No hay URL previa para reconexión");
        }
        return backoffreconnection(currentUrl);
    }

    @Override
    public CompletableFuture<Boolean> linearreconnection(UrlType url) throws Exception {
        return CompletableFuture.supplyAsync(() -> {
            try {
                int attempt = 0;
                final long FIXED_DELAY = 2000; // Delay fijo de 2 segundos

                while (attempt < MAX_RETRY_ATTEMPTS) {
                    if (tryReconnect(url)) {
                        return true;
                    }
                    Thread.sleep(FIXED_DELAY);
                    attempt++;
                }
                return false;
            } catch (Exception e) {
                logger.error("Error en reconexión lineal: {}", e.getMessage());
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> linearreconnection() throws Exception {
        if (currentUrl == null) {
            throw new SSLConnectionException("Url vacia o nula");
        }
        return linearreconnection(currentUrl);
    }

    @Override
    public CompletableFuture<Boolean> ping() {
        return CompletableFuture.supplyAsync(() -> {
                if (!isConnected || sslSocket == null || sslSocket.isClosed()) {
                    return false;
                }
                try {
                    return executePing();
                } catch (Exception e) {
                    logger.error("Ping error: ", e);
                    return false;
                }
        });
    }

    // Clase interna CertificateValidator
    private static class CertificateValidator {

        private final CertPathValidator validator;
        private final CertificateFactory certFactory;

        public CertificateValidator() throws CertificateException, NoSuchAlgorithmException {
            this.validator = CertPathValidator.getInstance("PKIX");
            this.certFactory = CertificateFactory.getInstance("X.509");
        }

        public void validateCertificateChain(X509Certificate[] chain) throws CertificateException {
            try {
                CertPath certPath = certFactory.generateCertPath(Arrays.asList(chain));
                PKIXParameters params = new PKIXParameters(createTrustAnchors(chain[chain.length - 1]));
                params.setRevocationEnabled(false);
                validator.validate(certPath, params);
            } catch (Exception e) {
                throw new CertificateException("Certificate chain validation failed", e);
            }
        }

        private Set<TrustAnchor> createTrustAnchors(X509Certificate rootCert) {
            return Collections.singleton(new TrustAnchor(rootCert, null));
        }

        public void validateCertificates(Certificate[] certificates) throws CertificateException {
            if (certificates == null || certificates.length == 0) {
                throw new CertificateException("No certificates provided");
            }

            for (Certificate cert : certificates) {
                if (!(cert instanceof X509Certificate)) {
                    throw new CertificateException("Non-X509 certificate found");
                }
                ((X509Certificate) cert).checkValidity();
            }
        }
    }
}