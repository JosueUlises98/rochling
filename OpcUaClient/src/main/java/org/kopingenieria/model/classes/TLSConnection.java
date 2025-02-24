package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import org.kopingenieria.model.enums.network.ConnectionStatus;
import org.kopingenieria.model.enums.tls.TlsVersion;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tls_connections")
@EntityListeners(AuditingEntityListener.class)
public class TLSConnection extends Connection<TLSConnection> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos básicos
    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    @Range(min = 1, max = 65535)
    private Integer port;

    // Configuración TLS
    @Enumerated(EnumType.STRING)
    private TlsVersion tlsVersion;

    @Column(name = "keystore_path")
    private String keystorePath;

    @Column(name = "keystore_password")
    private String keystorePassword;

    @Column(name = "keystore_type")
    private String keystoreType;

    @Column(name = "truststore_path")
    private String truststorePath;

    @Column(name = "truststore_password")
    private String truststorePassword;

    @Column(name = "truststore_type")
    private String truststoreType;

    // Configuración de cifrado
    @ElementCollection
    @CollectionTable(
            name = "tls_enabled_protocols",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    private Set<String> enabledProtocols;

    @ElementCollection
    @CollectionTable(
            name = "tls_enabled_cipher_suites",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    private Set<String> enabledCipherSuites;

    // Configuración de seguridad
    @Column(name = "client_auth_required")
    private Boolean clientAuthRequired;

    @Column(name = "hostname_verification")
    private Boolean hostnameVerification;

    @Column(name = "ocsp_enabled")
    private Boolean ocspEnabled;

    @Column(name = "crl_enabled")
    private Boolean crlEnabled;

    // Configuración de sesión
    @Column(name = "session_timeout")
    private Integer sessionTimeout;

    @Column(name = "session_cache_size")
    private Integer sessionCacheSize;

    // Control de certificados
    @Column(name = "certificate_expiration_warning")
    private Integer certificateExpirationWarning;

    @Column(name = "auto_cert_renewal")
    private Boolean autoCertRenewal;

    // Configuración industrial
    @Column(name = "industrial_zone")
    private String industrialZone;

    @Column(name = "equipment_id")
    private String equipmentId;

    @Column(name = "security_level")
    @Range(min = 1, max = 5)
    private Integer securityLevel;

    // Estado y monitoreo
    @Column(name = "last_connected")
    private LocalDateTime lastConnected;

    @Column(name = "last_disconnected")
    private LocalDateTime lastDisconnected;

    @Column(name = "connection_count")
    private Integer connectionCount;

    @Column(name = "error_count")
    private Integer errorCount;

    @Enumerated(EnumType.STRING)
    private ConnectionStatus status;

    @Column(name = "last_handshake")
    private LocalDateTime lastHandshake;

    @Column(name = "cipher_suite_in_use")
    private String cipherSuiteInUse;

    @Column(name = "protocol_in_use")
    private String protocolInUse;

    // Campos de auditoría
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    protected TLSConnection self() {
        return this;
    }

    @Override
    public TLSConnection build() {
        TLSConnection tlsConnection = new TLSConnection();
        tlsConnection.name = this.name;
        tlsConnection.host = this.host;
        tlsConnection.port = this.port;
        tlsConnection.username = this.username;
        tlsConnection.password = this.password;
        tlsConnection.method = this.method;
        tlsConnection.type = this.type;
        tlsConnection.tlsVersion = this.tlsVersion;
        tlsConnection.keystorePath = this.keystorePath;
        tlsConnection.keystorePassword = this.keystorePassword;
        tlsConnection.keystoreType = this.keystoreType;
        tlsConnection.truststorePath = this.truststorePath;
        tlsConnection.truststorePassword = this.truststorePassword;
        tlsConnection.truststoreType = this.truststoreType;
        tlsConnection.enabledProtocols = this.enabledProtocols;
        tlsConnection.enabledCipherSuites = this.enabledCipherSuites;
        tlsConnection.clientAuthRequired = this.clientAuthRequired;
        tlsConnection.hostnameVerification = this.hostnameVerification;
        tlsConnection.ocspEnabled = this.ocspEnabled;
        tlsConnection.crlEnabled = this.crlEnabled;
        tlsConnection.sessionTimeout = this.sessionTimeout;
        tlsConnection.sessionCacheSize = this.sessionCacheSize;
        tlsConnection.certificateExpirationWarning = this.certificateExpirationWarning;
        tlsConnection.autoCertRenewal = this.autoCertRenewal;
        tlsConnection.industrialZone = this.industrialZone;
        tlsConnection.equipmentId = this.equipmentId;
        tlsConnection.securityLevel = this.securityLevel;
        tlsConnection.lastConnected = this.lastConnected;
        tlsConnection.lastDisconnected = this.lastDisconnected;
        tlsConnection.connectionCount = this.connectionCount;
        tlsConnection.errorCount = this.errorCount;
        tlsConnection.status = this.status;
        tlsConnection.lastHandshake = this.lastHandshake;
        tlsConnection.cipherSuiteInUse = this.cipherSuiteInUse;
        tlsConnection.protocolInUse = this.protocolInUse;
        tlsConnection.createdAt = this.createdAt;
        tlsConnection.updatedAt = this.updatedAt;
        return tlsConnection;
    }

    @Override
    public String toString() {
        return "TLSConnection{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", method='" + method + '\'' +
                ", type='" + type + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", tlsVersion=" + tlsVersion +
                ", keystorePath='" + keystorePath + '\'' +
                ", keystorePassword='" + keystorePassword + '\'' +
                ", keystoreType='" + keystoreType + '\'' +
                ", truststorePath='" + truststorePath + '\'' +
                ", truststorePassword='" + truststorePassword + '\'' +
                ", truststoreType='" + truststoreType + '\'' +
                ", enabledProtocols=" + enabledProtocols +
                ", enabledCipherSuites=" + enabledCipherSuites +
                ", clientAuthRequired=" + clientAuthRequired +
                ", hostnameVerification=" + hostnameVerification +
                ", ocspEnabled=" + ocspEnabled +
                ", crlEnabled=" + crlEnabled +
                ", sessionTimeout=" + sessionTimeout +
                ", sessionCacheSize=" + sessionCacheSize +
                ", certificateExpirationWarning=" + certificateExpirationWarning +
                ", autoCertRenewal=" + autoCertRenewal +
                ", industrialZone='" + industrialZone + '\'' +
                ", equipmentId='" + equipmentId + '\'' +
                ", securityLevel=" + securityLevel +
                ", lastConnected=" + lastConnected +
                ", lastDisconnected=" + lastDisconnected +
                ", connectionCount=" + connectionCount +
                ", errorCount=" + errorCount +
                ", status=" + status +
                ", lastHandshake=" + lastHandshake +
                ", cipherSuiteInUse='" + cipherSuiteInUse + '\'' +
                ", protocolInUse='" + protocolInUse + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}
