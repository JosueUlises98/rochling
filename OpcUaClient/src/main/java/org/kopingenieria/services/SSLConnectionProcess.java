package org.kopingenieria.services;

import org.kopingenieria.model.Url;

public class SSLConnectionProcess extends ConnectionProcess {

    public SSLConnectionProcess(ConnectionService conexion, Url url,OpcuaClient opcuaClient) {
        this.conexion = conexion;
        this.url = url;
        this.opcuaClient = opcuaClient;
    }

    public Boolean call() throws Exception {
        System.out.println("SSLConnectionProcess"+opcuaClient.usuario());
        return conexion.conexion(url);
    }

    public String toString() {
        return "SSLConnectionProcess{" +
                "conexion=" + conexion +
                ", url=" + url +
                "} ";
    }

    @Override
    protected Boolean run() throws Exception {
        return call();
    }

}
