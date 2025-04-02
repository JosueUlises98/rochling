package org.kopingenieria.domain.enums.connection;


public enum UrlType {

    // URLs HTTP/HTTPS
    HTTP_LOCAL(Http.LOCAL),
    HTTP_REMOTE(Http.REMOTE),
    HTTP_SECURE(Http.SECURE),

    // URLs OPC UA
    OPCUA_LOCAL(OpcUa.LOCAL),
    OPCUA_REMOTE(OpcUa.REMOTE),
    OPCUA_SECURE(OpcUa.SECURE);

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

        private Http() {
        }
    }

    public static class OpcUa {
        public static final String LOCAL = "opc.tcp://localhost:4840";
        public static final String REMOTE = "opc.tcp://192.168.1.12:4840";
        public static final String SECURE = "opc.tcp://192.168.1.65:4840";

        private OpcUa() {
        }
    }

    public String getProtocol() {
        if (url != null && url.contains("://")) {
            return url.split("://")[0];
        }
        return null;
    }

    public String getIpAddress() {
        if (url != null && url.contains("://")) {
            String[] parts = url.split("://");
            if (parts.length > 1 && parts[1].contains(":")) {
                return parts[1].split(":")[0];
            }
        }
        return null;
    }

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
