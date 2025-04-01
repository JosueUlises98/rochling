package org.kopingenieria.config.opcua.bydefault;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
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
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Configuration
@ConfigurationProperties(prefix = "opcua.default")
@Validated
@Getter
public class DefaultConfiguration {

    @NotBlank(message = "El nombre del archivo es obligatorio")
    private final String filename = "OPC UA Default Configuration";

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

    @NotNull(message = "La autenticación es obligatoria")
    private final Authentication authentication = Authentication.builder()
            .identityProvider(IdentityProvider.ANONYMOUS)
            .userName(null)
            .password(null)
            .securityPolicy(null)
            .messageSecurityMode(null)
            .certificatePath(null)
            .privateKeyPath(null)
            .trustListPath(null)
            .issuerListPath(null)
            .revocationListPath(null)
            .securityPolicyUri(null)
            .build();

    @NotNull(message = "La encriptación es obligatoria")
    private final Encryption encryption = Encryption.builder()
            .securityPolicy(null)
            .messageSecurityMode(null)
            .clientCertificate(null)
            .privateKey(null)
            .trustedCertificates(null)
            .keyLength(null)
            .algorithmName(null)
            .protocolVersion(null)
            .type(null)
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
            .localeIds(List.of(LocaleIds.SPANISH))
            .maxChunkCount(4)
            .build();

    @NotNull(message = "Las suscripciones son obligatorias")
    private final List<Subscription> subscriptions = List.of(
            Subscription.builder()
                    .nodeId("NodeId-1")
                    .publishingInterval(1000.0)
                    .lifetimeCount(UInteger.valueOf(120))
                    .maxKeepAliveCount(UInteger.valueOf(10))
                    .maxNotificationsPerPublish(UInteger.valueOf(500))
                    .publishingEnabled(true)
                    .priority(UByte.valueOf(1))
                    .samplingInterval(500.0)
                    .queueSize(UInteger.valueOf(10))
                    .discardOldest(true)
                    .monitoringMode(MonitoringMode.Reporting)
                    .timestampsToReturn(TimestampsToReturn.Both)
                    .build()
    );

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
    public static class Authentication {
        private final IdentityProvider identityProvider;
        private final String userName;
        private final String password;
        private final SecurityPolicy securityPolicy;
        private final MessageSecurityMode messageSecurityMode;
        private final String certificatePath;
        private final String privateKeyPath;
        private final String trustListPath;
        private final String issuerListPath;
        private final String revocationListPath;
        private final SecurityPolicyUri securityPolicyUri;
        private final int expirationWarningDays = 30;

        @Builder
        public Authentication(IdentityProvider identityProvider, String userName, String password, SecurityPolicy securityPolicy,
                              MessageSecurityMode messageSecurityMode, String certificatePath, String privateKeyPath,
                              String trustListPath, String issuerListPath, String revocationListPath, SecurityPolicyUri securityPolicyUri) {
            this.identityProvider = identityProvider;
            this.userName = userName;
            this.password = password;
            this.securityPolicy = securityPolicy;
            this.messageSecurityMode = messageSecurityMode;
            this.certificatePath = certificatePath;
            this.privateKeyPath = privateKeyPath;
            this.trustListPath = trustListPath;
            this.issuerListPath = issuerListPath;
            this.revocationListPath = revocationListPath;
            this.securityPolicyUri = securityPolicyUri;
        }
    }

    @Getter
    public static class Encryption {
        private final SecurityPolicy securityPolicy;
        private final MessageSecurityMode messageSecurityMode;
        private final byte[] clientCertificate;
        private final byte[] privateKey;
        private final List<byte[]> trustedCertificates;
        private final Integer keyLength;
        private final EncryptionAlgorithm algorithmName;
        private final String protocolVersion;
        private final String type;

        @Builder
        public Encryption(SecurityPolicy securityPolicy, MessageSecurityMode messageSecurityMode, byte[] clientCertificate, byte[] privateKey,
                          List<byte[]> trustedCertificates, Integer keyLength, EncryptionAlgorithm algorithmName,
                          String protocolVersion, String type) {
            this.securityPolicy = securityPolicy;
            this.messageSecurityMode = messageSecurityMode;
            this.clientCertificate = clientCertificate;
            this.privateKey = privateKey;
            this.trustedCertificates = trustedCertificates;
            this.keyLength = keyLength;
            this.algorithmName = algorithmName;
            this.protocolVersion = protocolVersion;
            this.type = type;
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
    public static class Subscription {
        private final String nodeId;
        private final Double publishingInterval;
        private final UInteger lifetimeCount;
        private final UInteger maxKeepAliveCount;
        private final UInteger maxNotificationsPerPublish;
        private final Boolean publishingEnabled;
        private final UByte priority;
        private final Double samplingInterval;
        private final UInteger queueSize;
        private final Boolean discardOldest;
        private final MonitoringMode monitoringMode;
        private final TimestampsToReturn timestampsToReturn;
        private final Timeouts timeout=Timeouts.REQUEST;

        @Builder
        public Subscription(String nodeId, Double publishingInterval, UInteger lifetimeCount, UInteger maxKeepAliveCount,
                            UInteger maxNotificationsPerPublish, Boolean publishingEnabled, UByte priority, Double samplingInterval,
                            UInteger queueSize, Boolean discardOldest, MonitoringMode monitoringMode, TimestampsToReturn timestampsToReturn) {
            this.nodeId = nodeId;
            this.publishingInterval = publishingInterval;
            this.lifetimeCount = lifetimeCount;
            this.maxKeepAliveCount = maxKeepAliveCount;
            this.maxNotificationsPerPublish = maxNotificationsPerPublish;
            this.publishingEnabled = publishingEnabled;
            this.priority = priority;
            this.samplingInterval = samplingInterval;
            this.queueSize = queueSize;
            this.discardOldest = discardOldest;
            this.monitoringMode = monitoringMode;
            this.timestampsToReturn = timestampsToReturn;
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
