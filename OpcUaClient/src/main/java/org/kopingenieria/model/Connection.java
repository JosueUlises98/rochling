package org.kopingenieria.model;

import java.io.Serializable;

public abstract class Connection<T extends Connection<T>> implements Serializable {

    protected String name;
    protected String hostname;
    protected int port;
    protected String username;
    protected String password;
    protected String method;
    protected String type;

    public T withName(String name) {
        this.name = name;
        return self();
    }

    public T withHostname(String hostname) {
        this.hostname = hostname;
        return self();
    }

    public T withPort(int port) {
        this.port = port;
        return self();
    }

    public T withUsername(String username) {
        this.username = username;
        return self();
    }

    public T withPassword(String password) {
        this.password = password;
        return self();
    }

    public T withMethod(String method) {
        this.method = method;
        return self();
    }

    public T withType(String type) {
        this.type = type;
        return self();
    }

    protected abstract T self();

    protected abstract Connection<T> build();

}
