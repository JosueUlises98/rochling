package org.kopingenieria.model.classes;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;


@MappedSuperclass
public abstract class Configuration implements Serializable {

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

}
