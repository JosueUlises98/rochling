package org.kopingenieria.services;

import org.kopingenieria.domain.enums.client.network.connection.UrlType;

public class OpcuaConnectionProcess extends ConnectionProcess {

    public OpcuaConnectionProcess(ConnectionService conexion, UrlType url) {
        this.conexion = conexion;
        this.url = url;
    }

    @Override
    public Boolean call() throws Exception {
        return null;
    }

    @Override
    public String toString() {
        return "OpcuaConnectionProcess{" +
                "conexion=" + conexion +
                ", url=" + url +
                "} ";
    }

}
