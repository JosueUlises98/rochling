package org.kopingenieria.domain.enums.client.network.connection;


/**
 * Represents different types of URL configurations for various protocols and
 * connection types, including HTTP/HTTPS, OPC UA, TCP, MQTT, TLS/SSL, and SSH.
 * Each enum constant is mapped to a predefined URL string.
 */
public enum UrlType {

    // URLs HTTP/HTTPS
    HTTP_LOCAL(Http.LOCAL),
    HTTP_REMOTE(Http.REMOTE),
    HTTP_SECURE(Http.SECURE),

    // URLs OPC UA
    OPCUA_LOCAL(OpcUa.LOCAL),
    OPCUA_REMOTE(OpcUa.REMOTE),
    OPCUA_SECURE(OpcUa.SECURE),

    // URLs TCP
    TCP_LOCAL(Tcp.LOCAL),
    TCP_REMOTE(Tcp.REMOTE),
    TCP_SECURE(Tcp.SECURE),

    // URLs MQTT
    MQTT_BROKER(Mqtt.BROKER),
    MQTT_CLIENT(Mqtt.CLIENT),
    MQTT_SECURE(Mqtt.SECURE),

    // URLs SSL/TLS
    TLS_SERVER(Tls.SERVER),
    TLS_CLIENT(Tls.CLIENT),
    TLS_MUTUAL(Tls.MUTUAL_AUTH),
    SSL_LEGACY(Tls.SSL_LEGACY),

    // URLs SSH
    SSH_SERVER(Ssh.SERVER),
    SSH_SFTP(Ssh.SFTP),
    SSH_TUNNEL(Ssh.TUNNEL),
    SSH_SCP(Ssh.SCP);

    private final String url;

    UrlType(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }


    public static class Http {
        public static final String LOCAL = "http://localhost:8080";
        public static final String REMOTE = "http://api.ejemplo.com";
        public static final String SECURE = "https://secure.ejemplo.com";

        private Http() {}
    }

    public static class OpcUa {
        public static final String LOCAL = "opc.tcp://localhost:4840";
        public static final String REMOTE = "opc.tcp://servidor.ejemplo.com:4840";
        public static final String SECURE = "opc.tcp://secure.ejemplo.com:4843";

        private OpcUa() {}
    }

    public static class Tcp {
        public static final String LOCAL = "tcp://localhost:9090";
        public static final String REMOTE = "tcp://servidor.ejemplo.com:9090";
        public static final String SECURE = "tcp://secure.ejemplo.com:9093";

        private Tcp() {}
    }

    public static class Mqtt {
        public static final String BROKER = "mqtt://broker.ejemplo.com:1883";
        public static final String CLIENT = "mqtt://localhost:1883";
        public static final String SECURE = "mqtts://secure.ejemplo.com:8883";

        private Mqtt() {}
    }

    public static class Tls {
        public static final String SERVER = "tls://secure-server.ejemplo.com:443";
        public static final String CLIENT = "tls://client.ejemplo.com:443";
        public static final String MUTUAL_AUTH = "tls://mutual-auth.ejemplo.com:8443";
        public static final String SSL_LEGACY = "ssl://legacy.ejemplo.com:443";

        // Ejemplos con parámetros adicionales
        public static final String SERVER_WITH_PARAMS = "tls://secure-server.ejemplo.com:443?cert=server&version=1.3";
        public static final String CLIENT_WITH_PARAMS = "tls://client.ejemplo.com:443?truststore=client.jks&keystore=client.p12";

        // Constantes para configuración
        public static final int DEFAULT_TLS_PORT = 443;
        public static final int DEFAULT_SSL_PORT = 443;
        public static final String DEFAULT_TLS_VERSION = "TLSv1.3";

        private Tls() {}
    }

    public static class Ssh {
        public static final String SERVER = "ssh://servidor-ssh.ejemplo.com:22";
        public static final String SFTP = "sftp://storage.ejemplo.com:22";
        public static final String TUNNEL = "ssh://tunnel.ejemplo.com:22?localPort=8080&remotePort=80";
        public static final String SCP = "scp://file-transfer.ejemplo.com:22";

        // Ejemplos con autenticación y parámetros
        public static final String SERVER_WITH_USER = "ssh://usuario@servidor-ssh.ejemplo.com:22";
        public static final String SFTP_WITH_KEY = "sftp://usuario@storage.ejemplo.com:22?keyfile=id_rsa";

        // Constantes para configuración
        public static final int DEFAULT_SSH_PORT = 22;
        public static final int DEFAULT_SFTP_PORT = 22;
        public static final String DEFAULT_KEY_TYPE = "RSA";

        private Ssh() {}
    }

    /**
     * Extracts and returns the protocol from the URL associated with the enum constant.
     * For example, in "opc.tcp://192.168.1.12:4840", it will return "opc.tcp".
     *
     * @return The protocol part of the URL.
     */
    public String getProtocol() {
        if (url != null && url.contains("://")) {
            return url.split("://")[0];
        }
        return null;
    }
    /**
     * Extracts and returns the IP address from the URL associated with the enum constant.
     * For example, in "opc.tcp://192.168.1.12:4840", it will return "192.168.1.12".
     *
     * @return The IP address part of the URL, or null if missing.
     */
    public String getIpAddress() {
        if (url != null && url.contains("://")) {
            String[] parts = url.split("://");
            if (parts.length > 1 && parts[1].contains(":")) {
                return parts[1].split(":")[0];
            }
        }
        return null;
    }
    /**
     * Extracts and returns the port from the URL associated with the enum constant.
     * For example, in "opc.tcp://192.168.1.12:4840", it will return 4840.
     *
     * @return The port part of the URL as an integer, or -1 if no port is found.
     */
    public int getPort() {
        if (url != null && url.contains(":")) {
            try {
                String[] parts = url.split(":");
                if (parts.length > 2) {
                    return Integer.parseInt(parts[2]);
                }
            } catch (NumberFormatException e) {
                // Log error or handle invalid port number, if necessary.
            }
        }
        return -1;
    }


}
