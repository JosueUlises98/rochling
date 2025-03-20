package org.kopingenieria.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "opcua")
@Validated
@Data
public class OpcUaConfiguration {

    @NotBlank(message = "El nombre de la configuración es obligatorio")
    private String name;
    @NotBlank(message = "La descripcion es obligatoria")
    private String description;
    @NotNull(message = "El estado habilitado/deshabilitado es obligatorio")
    private Boolean enabled;
    private Long version;

    @NotNull(message = "La conexión es obligatoria")
    private Connection connection;

    @NotNull(message = "La autenticación es obligatoria")
    private Authentication authentication;

    @NotNull(message = "La configuración de encriptación es obligatoria")
    private Encryption encryption;

    @NotNull(message = "La sesión es obligatoria")
    private Session session;

    private List<Subscription> subscriptions;
    private List<MonitoringEvent> monitoringEvents;

    @NotNull(message = "La configuración industrial es obligatoria")
    private IndustrialConfiguration industrialConfiguration;

    @Data
    public static class Connection {
        private String endpointUrl;
        private String applicationName;
        private String applicationUri;
        private String productUri;
        private Integer requestTimeout;
        private Integer channelLifetime;
    }

    @Data
    public static class Authentication {
        private String username;
        private String password;
        private String securityPolicy;
        private String securityMode;
        private String certificatePath;
        private String privateKeyPath;
        private Boolean anonymous;
    }

    @Data
    public static class Encryption {
        private String securityPolicy;
        private String messageMode;
        private String algorithm;
        private Integer keySize;
        private String certificateType;
        private Boolean validateCertificate;
    }

    @Data
    public static class Session {
        private String sessionName;
        private Integer sessionTimeout;
        private Integer maxResponseMessageSize;
        private Integer maxRequestMessageSize;
        private Boolean publishingEnabled;
    }

    @Data
    public static class Subscription {
        private String name;
        private Double publishingInterval;
        private Integer lifetimeCount;
        private Integer maxKeepAliveCount;
        private Integer maxNotificationsPerPublish;
        private Integer priority;
        private Boolean publishingEnabled;
    }

    @Data
    public static class MonitoringEvent {
        private String nodeId;
        private String browsePath;
        private String displayName;
        private Double samplingInterval;
        private Integer queueSize;
        private Boolean discardOldest;
        private String monitoringMode;
        private String dataType;
        private String triggerType;
    }

    @Data
    public static class IndustrialConfiguration {
        private String industrialZone;
        private String equipmentId;
        private String areaId;
        private String processId;
    }
}
