package org.kopingenieria.services;

import org.kopingenieria.model.enums.client.network.connection.UrlType;

public class TCPConnectionProcess extends ConnectionProcess{

    public TCPConnectionProcess(ConnectionService conexion, UrlType url) {
        this.conexion = conexion;
        this.url = url;
    }

    @Override
    public Boolean call() throws Exception {
        return null;
    }

    @Override
    public String toString() {
        return "TCPConnectionProcess{" +
                "conexion=" + conexion +
                ", url=" + url +
                "} ";
    }

    @Override
    protected Boolean run() throws Exception {
        return conexion.conexion(url);
    }
}
