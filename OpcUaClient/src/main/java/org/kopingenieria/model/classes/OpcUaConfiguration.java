package org.kopingenieria.model.classes;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.kopingenieria.model.enums.opcua.MessageSecurityMode;
import org.kopingenieria.model.enums.opcua.SecurityPolicy;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
public class OpcUaConfiguration extends Configuration<OpcUaConfiguration> {
    // Atributos de conexión OPC UA
    private String endpointUrl;
    private String applicationName;
    private String applicationUri;
    private String productUri;

    // Atributos de seguridad
    @Enumerated(EnumType.STRING)
    private SecurityPolicy securityPolicy;

    @Enumerated(EnumType.STRING)
    private MessageSecurityMode messageSecurityMode;

    private String certificatePath;
    private String privateKeyPath;

    // Atributos de sesión
    private Double sessionTimeout;
    private String[] localeIds;
    private Integer maxResponseMessageSize;

    // Atributos de suscripción
    private Double publishingInterval;
    private Integer lifetimeCount;
    private Integer maxKeepAliveCount;
    private Integer maxNotificationsPerPublish;
    private boolean publishingEnabled;

    // Atributos de monitoreo
    private Double samplingInterval;
    private Integer queueSize;
    private boolean discardOldest;

    @Embedded
    private OpcUaSecurityConfiguration securityConfig;

    protected OpcUaConfiguration self() {
        return this;
    }

    public OpcUaConfiguration build() {
        OpcUaConfiguration opcUaConfiguration = new OpcUaConfiguration();
        opcUaConfiguration.endpointUrl = this.endpointUrl;
        opcUaConfiguration.applicationName = this.applicationName;
        opcUaConfiguration.applicationUri = this.applicationUri;
        opcUaConfiguration.productUri = this.productUri;
        opcUaConfiguration.securityPolicy = this.securityPolicy;
        opcUaConfiguration.messageSecurityMode = this.messageSecurityMode;
        opcUaConfiguration.certificatePath = this.certificatePath;
        opcUaConfiguration.privateKeyPath = this.privateKeyPath;
        opcUaConfiguration.sessionTimeout = this.sessionTimeout;
    }

}
