package org.kopingenieria.model.classes;

import java.io.Serializable;

public abstract class Authentication<T extends Authentication<T>> implements Serializable {

    protected String none;
    protected String username;
    protected String password;
    protected String certificate;

    public T withNone(String none) {
        this.none = none;
        return self();
    }

    public T withUserName(String username) {
        this.username = username;
        return self();
    }

    public T withPassword(String password) {
        this.password = password;
        return self();
    }

    public T withCertificate(String certificate) {
        this.certificate = certificate;
        return self();
    }

    protected abstract T self();

    protected abstract Authentication<T> build();
}
