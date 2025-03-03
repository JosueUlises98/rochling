package org.kopingenieria.domain.classes;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public abstract class Client {
   protected Configuration config;
   protected Suscription suscription;
   protected MonitoreoEventos monitoreoEventos;
}
