package org.kopingenieria.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kopingenieria.model.enums.network.UrlType;
import org.kopingenieria.services.OpcuaClient;


public class MainClient {

    private static final Logger logger = LogManager.getLogger(MainClient.class);

    public static void main(String[] args) throws Exception {
        OpcuaClient opcuaClient = new OpcuaClient("192.168.50.2",8976,"user","password");
        opcuaClient.conectar(UrlType.Adress2);
    }
}
