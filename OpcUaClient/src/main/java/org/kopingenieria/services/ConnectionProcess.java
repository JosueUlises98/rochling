package org.kopingenieria.services;

import org.kopingenieria.model.Url;
import java.util.concurrent.Callable;


public abstract class ConnectionProcess extends Process implements Callable<Boolean> {

    protected ConnectionService conexion;
    protected Url url;
    protected OpcuaClient opcuaClient;

    public ConnectionProcess() {}

}
