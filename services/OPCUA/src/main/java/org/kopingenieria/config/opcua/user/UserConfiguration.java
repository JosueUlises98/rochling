package org.kopingenieria.config.opcua.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.locale.LocaleIds;
import org.kopingenieria.domain.enums.monitoring.MonitoringMode;
import org.kopingenieria.domain.enums.security.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.Objects;

@Configuration
@ConfigurationProperties(prefix = "opcua.user")
@Validated
@Data
public class UserConfiguration {

    @NotBlank(message = "El nombre de la configuración es obligatorio")
    private String filename;
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

    @NotNull(message = "La configuración industrial es obligatoria")
    private IndustrialConfiguration industrialConfiguration;

    @Data
    @Builder
    public static class Connection {
        private String name;
        private String endpointUrl;
        private String applicationName;
        private String applicationUri;
        private String productUri;
        private ConnectionType type;
        private final Timeouts timeout=Timeouts.CONNECTION;
        private ConnectionStatus status;
    }

    @Data
    @Builder
    public static class Authentication {
        private IdentityProvider identityProvider;
        private String userName;
        private String password;
        private SecurityPolicy securityPolicy;
        private MessageSecurityMode messageSecurityMode;
        private String certificatePath;
        private String privateKeyPath;
        private String trustListPath;
        private String issuerListPath;
        private String revocationListPath;
        private SecurityPolicyUri securityPolicyUri;
        private final int expirationWarningDays = 30;
    }

    @Data
    @Builder
    public static class Encryption {
        private SecurityPolicy securityPolicy;
        private MessageSecurityMode messageSecurityMode;
        private byte[] clientCertificate;
        private byte[] privateKey;
        private List<byte[]> trustedCertificates;
        private Integer keyLength;
        private EncryptionAlgorithm algorithmName;
        private String protocolVersion;
        private String type;
    }

    @Data
    @Builder
    public static class Session {
        private String sessionName;
        private String serverUri;
        private Long maxResponseMessageSize;
        private MessageSecurityMode securityMode;
        private SecurityPolicyUri securityPolicyUri;
        private String clientCertificate;
        private String serverCertificate;
        private List<LocaleIds> localeIds;
        private Integer maxChunkCount;
        private final Timeouts timeout=Timeouts.SESSION;
    }

    @Data
    @Builder
    public static class IndustrialConfiguration {
        private String industrialZone;
        private String equipmentId;
        private String areaId;
        private String processId;
        private String operatorName;
        private String operatorId;
    }
}
