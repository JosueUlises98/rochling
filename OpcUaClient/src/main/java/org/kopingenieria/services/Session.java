package org.kopingenieria.services;

import org.kopingenieria.exceptions.ConnectionException;
import org.kopingenieria.model.UrlType;

public interface Session {
    /**
     * Terminates the current session.
     * Typically used to release resources or finalize operations.
     */
    void terminateSession();
    /**
     * Verifies if the session is currently active.
     *
     * @return {@code true} if the session is active, {@code false} otherwise
     */
    boolean isSessionActive();
    /**
     * Initiates a login using the provided client connection service.
     *
     * @param clientService the client connection service used for authentication
     * @return {@code true} if the login was successful, {@code false} otherwise
     */
     SessionObject login(UrlType url, String username, String password) throws ConnectionException;
    /**
     * Logs out from the current session.
     *
     * @return {@code true} if the session was logged out successfully, {@code false} otherwise
     */
    boolean logout();
}
