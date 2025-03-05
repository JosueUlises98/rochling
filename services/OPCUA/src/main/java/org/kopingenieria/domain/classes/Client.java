package org.kopingenieria.domain.classes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public abstract class Client {
   protected Configuration config;
   protected Suscription suscription;
   protected MonitoreoEventos monitoreoEventos;
}
