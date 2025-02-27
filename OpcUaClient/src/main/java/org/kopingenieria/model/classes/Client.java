package org.kopingenieria.model.classes;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class Client {
   protected Configuration config;
   protected Suscription suscription;
   protected MonitoreoEventos monitoreoEventos;
}
