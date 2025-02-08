package org.kopingenieria.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kopingenieria.exceptions.ConnectionException;
import org.kopingenieria.model.SessionObject;
import org.kopingenieria.model.Url;
import org.kopingenieria.validators.ValidatorSession;

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
    public SessionObject login(Url url, String username, String password) throws ConnectionException {
        return null;
    }

    @Override
    public boolean logout() {
        return false;
    }
}
