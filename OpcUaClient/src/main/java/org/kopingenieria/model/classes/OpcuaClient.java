package org.kopingenieria.model.classes;

public class OpcuaClient extends Client {
    private Connection<?> connection;
    private MonitoreoEventos monitoreoEventos;
    private Suscripciones suscripciones;
    private SSLSession sslSession;
    private String ip;
    private int port;
    private String user;
    private String password;
}
