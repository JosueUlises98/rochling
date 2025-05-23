package org.kopingenieria.config.opcua.bydefault;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.locale.LocaleIds;
import org.kopingenieria.domain.enums.security.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.UUID;

@Configuration
@ConfigurationProperties(prefix = "opcua.default")
@Validated
@Getter
@ToString
public class DefaultConfiguration {

    private final String id = UUID.randomUUID().toString();

    private final Integer maxChunkCount = 4;

    @NotBlank(message = "El nombre del archivo es obligatorio")
    private final String filename = "OPC UA Default Configuration.properties";

    @NotBlank(message = "La descripción es obligatoria")
    private final String description = "Configuración por defecto del cliente OPC UA";

    @NotNull(message = "El estado habilitado/deshabilitado es obligatorio")
    private final Boolean enabled = true;

    @Positive(message = "La versión debe ser positiva")
    private final Long version = 123456789L;

    @NotNull(message = "La conexión es obligatoria")
    private final Connection connection = Connection.builder()
            .name("DefaultConnection")
            .endpointUrl("opc.tcp://localhost:4840")
            .applicationName("KopIngenieria OPC UA Client")
            .applicationUri("urn:kopingenieria:client")
            .productUri("urn:kopingenieria:client:product")
            .type(ConnectionType.OPCUA)
            .status(ConnectionStatus.UNKNOWN)
            .build();

    @NotNull(message = "La sesión es obligatoria")
    private final Session session = Session.builder()
            .sessionName("DefaultSession")
            .serverUri("opc.tcp://localhost:4840")
            .maxResponseMessageSize(10485760L)
            .securityMode("None")
            .securityPolicyUri(SecurityPolicyUri.NONE)
            .clientCertificate(null)
            .serverCertificate(null)
            .localeIds(List.of(LocaleIds.SPANISH,LocaleIds.ENGLISH,LocaleIds.CHINESE,LocaleIds.FRENCH))
            .maxChunkCount(4)
            .build();

    @NotNull(message = "La configuración industrial es obligatoria")
    private final IndustrialConfiguration industrialConfiguration = IndustrialConfiguration.builder()
            .industrialZone("ZONE_001")
            .equipmentId("EQ_001")
            .areaId("AREA_001")
            .processId("PROC_001")
            .operatorName("DEFAULT_OPERATOR")
            .operatorId(UUID.randomUUID().toString())
            .build();

    @Getter
    public static class Connection {
        private final String name;
        private final String endpointUrl;
        private final String applicationName;
        private final String applicationUri;
        private final String productUri;
        private final ConnectionType type;
        private final Timeouts timeout=Timeouts.CONNECTION;
        @Setter
        private ConnectionStatus status;

        @Builder
        public Connection(String name, String endpointUrl, String applicationName, String applicationUri, String productUri, ConnectionType type,ConnectionStatus status) {
            this.name = name;
            this.endpointUrl = endpointUrl;
            this.applicationName = applicationName;
            this.applicationUri = applicationUri;
            this.productUri = productUri;
            this.type = type;
            this.status = status;
        }
    }

    @Getter
    public static class Session {
        private final String sessionName;
        private final String serverUri;
        private final Long maxResponseMessageSize;
        private final String securityMode;
        private final SecurityPolicyUri securityPolicyUri;
        private final String clientCertificate;
        private final String serverCertificate;
        private final List<LocaleIds> localeIds;
        private final Integer maxChunkCount;
        private final Timeouts timeout=Timeouts.SESSION;

        @Builder
        public Session(String sessionName, String serverUri, Long maxResponseMessageSize, String securityMode,
                       SecurityPolicyUri securityPolicyUri, String clientCertificate, String serverCertificate,
                       List<LocaleIds> localeIds, Integer maxChunkCount) {
            this.sessionName = sessionName;
            this.serverUri = serverUri;
            this.maxResponseMessageSize = maxResponseMessageSize;
            this.securityMode = securityMode;
            this.securityPolicyUri = securityPolicyUri;
            this.clientCertificate = clientCertificate;
            this.serverCertificate = serverCertificate;
            this.localeIds = localeIds;
            this.maxChunkCount = maxChunkCount;
        }
    }

    @Getter
    public static class IndustrialConfiguration {
        private final String industrialZone;
        private final String equipmentId;
        private final String areaId;
        private final String processId;
        private final String operatorName;
        private final String operatorId;

        @Builder
        public IndustrialConfiguration(String industrialZone, String equipmentId, String areaId, String processId,
                                       String operatorName, String operatorId) {
            this.industrialZone = industrialZone;
            this.equipmentId = equipmentId;
            this.areaId = areaId;
            this.processId = processId;
            this.operatorName = operatorName;
            this.operatorId = operatorId;
        }
    }

    public DefaultConfiguration getInmutableDefaultConfiguration() {
        return this;
    }
}
