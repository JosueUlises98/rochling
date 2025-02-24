package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import org.kopingenieria.model.enums.network.SessionStatus;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opcua_sessions")
@EntityListeners(AuditingEntityListener.class)
public class OpcUaSession extends Session<OpcUaSession> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_name")
    private String sessionName;
    @Column(name = "server_uri")
    private String serverUri;
    @Column(name = "max_response_message_size")
    private Long maxResponseMessageSize;
    @Column(name = "security_mode")
    private String securityMode;
    @Column(name = "security_policy_uri")
    private String securityPolicyUri;
    @Column(name = "client_certificate")
    private String clientCertificate;
    @Column(name = "server_certificate")
    private String serverCertificate;
    @Column(name = "authentication_token")
    private String authenticationToken;
    @ElementCollection
    @CollectionTable(
            name = "opcua_locale_ids",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    private List<String> localeIds;

    @Column(name = "max_chunk_count")
    private Integer maxChunkCount;
    @Column(name = "error")
    private String error;

    @Override
    protected OpcUaSession self() {
        return this;
    }

    @Override
    public OpcUaSession build() {
        OpcUaSession opcUaSession = new OpcUaSession();
        opcUaSession.sessionId = this.sessionId;
        opcUaSession.clientAddress = this.clientAddress;
        opcUaSession.port = this.port;
        opcUaSession.isSecure = this.isSecure;
        opcUaSession.protocolType = this.protocolType;
        opcUaSession.status = this.status;
        opcUaSession.connectionDetails = this.connectionDetails;
        opcUaSession.creationTime = this.creationTime;
        opcUaSession.lastActivity= this.lastActivity;
        opcUaSession.expirationTime = this.expirationTime;
        opcUaSession.timeout = this.timeout;
        opcUaSession.sessionName = this.sessionName;
        opcUaSession.serverUri = this.serverUri;
        opcUaSession.maxResponseMessageSize = this.maxResponseMessageSize;
        opcUaSession.securityMode = this.securityMode;
        opcUaSession.securityPolicyUri = this.securityPolicyUri;
        opcUaSession.clientCertificate = this.clientCertificate;
        opcUaSession.serverCertificate = this.serverCertificate;
        opcUaSession.authenticationToken = this.authenticationToken;
        opcUaSession.localeIds = this.localeIds;
        opcUaSession.maxChunkCount = this.maxChunkCount;
        opcUaSession.error = this.error;
        return opcUaSession;
    }

    @Override
    public String toString() {
        return "OpcUaSession{" +
                "sessionId='" + sessionId + '\'' +
                ", clientAddress='" + clientAddress + '\'' +
                ", port=" + port +
                ", isSecure=" + isSecure +
                ", protocolType=" + protocolType +
                ", status=" + status +
                ", connectionDetails=" + connectionDetails +
                ", lastActivity=" + lastActivity +
                ", creationTime=" + creationTime +
                ", expirationTime=" + expirationTime +
                ", timeout=" + timeout +
                ", sessionName='" + sessionName + '\'' +
                ", serverUri='" + serverUri + '\'' +
                ", maxResponseMessageSize=" + maxResponseMessageSize +
                ", securityMode='" + securityMode + '\'' +
                ", securityPolicyUri='" + securityPolicyUri + '\'' +
                ", clientCertificate='" + clientCertificate + '\'' +
                ", serverCertificate='" + serverCertificate + '\'' +
                ", authenticationToken='" + authenticationToken + '\'' +
                ", localeIds=" + localeIds +
                ", maxChunkCount=" + maxChunkCount +
                ", error='" + error + '\'' +
                '}';
    }
}
