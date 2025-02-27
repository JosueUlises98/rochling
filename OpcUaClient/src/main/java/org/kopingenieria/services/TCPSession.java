package org.kopingenieria.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kopingenieria.exceptions.ConnectionException;
import org.kopingenieria.model.enums.network.connection.UrlType;

public class TCPSession extends SessionService{

    private final ValidatorSession validator;
    private final ConnectionService conection;
    private static final Logger logger = LogManager.getLogger(SessionService.class);

    public SessionService(){
        validator = new ValidatorSession();
        conection = ConnectionService.getInstance();
    }

    @Override
    public void terminateSession() {
        logger.info("Terminating session");
    }

    @Override
    public boolean isSessionActive() {
        return false;
    }

    @Override
    public SessionObject login(UrlType url, String username, String password) throws ConnectionException {
        return null;
    }

    @Override
    public boolean logout() {
        return false;
    }
}
