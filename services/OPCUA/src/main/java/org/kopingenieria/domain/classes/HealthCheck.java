package org.kopingenieria.domain.classes;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.client.network.connection.ConnectionStatus;
import java.time.LocalDateTime;

@Data
@Builder
public class HealthCheck {

    // Estado y diagnóstico común
    @NotNull(message = "El estado no puede ser nulo")
    private ConnectionStatus status;

    @NotNull(message = "La fecha no puede ser nula")
    private LocalDateTime lastConnected;

    @NotNull(message = "La fecha no puede ser nula")
    private LocalDateTime lastConnecting;

    @NotNull(message = "La fecha no puede ser nula")
    private LocalDateTime lastDisconnected;

    @NotNull(message = "La fecha no puede ser nula")
    private LocalDateTime lastDisconnecting;

    @NotNull(message = "La fecha no puede ser nula")
    private LocalDateTime lastReconnecting;

    @NotNull(message = "La fecha no puede ser nula")
    private LocalDateTime lastReconnected;

    @NotNull(message = "La fecha no puede ser nula")
    private LocalDateTime lastFailed;

    @NotNull(message = "La fecha no puede ser nula")
    private Integer connectionCount;

    @NotNull(message = "La fecha no puede ser nula")
    private Integer connectingCount;

    @NotNull(message = "La fecha no puede ser nula")
    private Integer errorCount;

    @NotNull(message = "La fecha no puede ser nula")
    private Integer unknownStatusCount;

    @NotNull(message = "La fecha no puede ser nula")
    private Integer reconnectCount;

    @NotNull(message = "La fecha no puede ser nula")
    private Integer reconnectingCount;

    @NotNull(message = "La fecha no puede ser nula")
    private Integer failedcount;

    @NotNull(message = "La fecha no puede ser nula")
    private Integer disconnectedcount;
}
