package org.kopingenieria.services;

import org.kopingenieria.exceptions.ConnectionException;
import org.kopingenieria.model.SessionObject;
import org.kopingenieria.model.Url;

public class SSLSession extends SessionService{

    @Override
    public void terminateSession() {

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
