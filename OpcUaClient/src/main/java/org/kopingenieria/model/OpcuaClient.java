package org.kopingenieria.model;

public class OpcuaClient {
    // Attributes of an OPC UA client
    private String endpointUrl;        // URL of the OPC UA server endpoint
    private String securityPolicy;     // Security policy used for connection
    private String applicationName;    // Name of the client application
    private long sessionTimeout;       // Timeout for the session in milliseconds
    private boolean connected;         // Indicator if the client is currently connected
}
