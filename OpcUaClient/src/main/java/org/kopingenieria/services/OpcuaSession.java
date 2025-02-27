package org.kopingenieria.services;

import org.kopingenieria.exceptions.ConnectionException;
import org.kopingenieria.model.enums.network.connection.UrlType;

public class OpcuaSession extends SessionService {

    @Override
    public void terminateSession() {

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
