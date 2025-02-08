package org.kopingenieria.services;

import org.kopingenieria.model.SessionObject;
import org.kopingenieria.model.Url;

public final class OpcuaClient {

    private String ip;
    private int puerto;
    private String usuario;
    private String clave;
    private SessionObject sessionObject;

    public OpcuaClient(String ip, int puerto, String usuario, String clave) {
        this.ip = ip;
        this.puerto = puerto;
        this.usuario = usuario;
        this.clave = clave;
    }

    public void conectar(Url url) throws Exception {
        ConnectionProcess processConnection = new SSLConnectionProcess(conexionservice, url);
        processConnection.call();
    }

    public void desconectar() {
        conexionservice.desconexion();
    }

    public void reconectar(Url url) throws Exception {
        conexionservice.reconexion(url);
    }

    public String ip() {
        return ip;
    }

    public OpcuaClient setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int puerto() {
        return puerto;
    }

    public OpcuaClient setPuerto(int puerto) {
        this.puerto = puerto;
        return this;
    }

    public String usuario() {
        return usuario;
    }

    public OpcuaClient setUsuario(String usuario) {
        this.usuario = usuario;
        return this;
    }

    public String clave() {
        return clave;
    }

    public OpcuaClient setClave(String clave) {
        this.clave = clave;
        return this;
    }
}
