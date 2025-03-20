package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpcUaConfigRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "El estado habilitado/deshabilitado es obligatorio")
    private Boolean enabled = true;

    @NotNull(message = "La configuración de conexión es obligatoria")
    private OpcUaConnectionRequest connection;

    @NotNull(message = "La configuración de autenticación es obligatoria")
    private OpcUaAuthenticationRequest authentication;

    @NotNull(message = "La configuración de encriptación es obligatoria")
    private OpcUaEncryptionRequest encryption;

    @NotNull(message = "La configuración de sesión es obligatoria")
    private OpcUaSessionRequest session;

    @NotNull(message = "La lista de suscripciones no puede ser nula")
    @Size(min = 1, message = "Debe haber al menos una suscripción")
    private List<@NotNull(message = "La suscripción no puede ser nula") SubscriptionRequest> subscriptions;

    @NotNull(message = "La lista de eventos de monitoreo no puede ser nula")
    @Size(min = 1, message = "Debe haber al menos un evento de monitoreo")
    private List<@NotNull(message = "El evento de monitoreo no puede ser nulo") MonitoringEventRequest> monitoringEvents;

    @NotNull(message = "La configuración industrial es obligatoria")
    private IndustrialConfigurationRequest industrialConfiguration;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndustrialConfigurationRequest {
        @NotNull(message = "Industrial zone cannot be null")
        private String industrialZone;
        @NotNull(message = "Equipment ID cannot be null")
        private String equipmentId;
        @NotNull(message = "Area ID cannot be null")
        private String areaId;
        @NotNull(message = "Process ID cannot be null")
        private String processId;
    }
}
