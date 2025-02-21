package org.kopingenieria.model.classes;

public abstract class Client {
    protected Configuration<?> configuration;
    protected Connection<?> connection;
    protected AuthenticationRequest authentication;
    protected AuthenticationResponse authenticationResponse;
}
