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
public final class OpcUaSession extends Session {

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
