package org.kopingenieria.model.classes;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;


@MappedSuperclass
public abstract class Configuration<T extends Configuration<T>>implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    // Atributos comunes base
    protected String name;
    protected String description;
    protected boolean enabled;

    // Atributos de conexión básicos
    protected Integer timeout;
    protected Integer retryAttempts;
    protected Integer retryDelay;

    // Atributos de logging y monitoreo
    protected boolean debugEnabled;
    protected String logLevel;
    protected boolean metricsEnabled;

    public T setName(String name) {
        this.name = name;
        return self();
    }

    public T setDescription(String description) {
        this.description = description;
        return self();
    }

    public T setEnabled(boolean enabled) {
        this.enabled = enabled;
        return self();
    }

    public T setTimeout(Integer timeout) {
        this.timeout = timeout;
        return self();
    }

    public T setRetryAttempts(Integer retryAttempts) {
        this.retryAttempts = retryAttempts;
        return self();
    }

    public T setRetryDelay(Integer retryDelay) {
        this.retryDelay = retryDelay;
        return self();
    }

    public T setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        return self();
    }

    public T setLogLevel(String logLevel) {
        this.logLevel = logLevel;
        return self();
    }

    public T setMetricsEnabled(boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
        return self();
    }

    protected abstract T self();

    protected abstract Configuration<T> build();
}
