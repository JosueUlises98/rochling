package org.kopingenieria.services;

import org.kopingenieria.model.Url;

public class OpcuaConnectionProcess extends ConnectionProcess {

    public OpcuaConnectionProcess(ConnectionService conexion, Url url) {
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
