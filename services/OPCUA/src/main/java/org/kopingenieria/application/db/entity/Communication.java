package org.kopingenieria.application.db.entity;

import jakarta.persistence.EntityListeners;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kopingenieria.domain.enums.communication.CompressedPayload;
import org.kopingenieria.domain.enums.communication.Direction;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.annotation.Version;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "communication_logs")
@TypeAlias("communicationLog")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Communication implements Serializable {

    @Serial
    private static final long serialVersionUID = 600L;

    @Id
    private String id;

    @Indexed
    @Field("session_id")
    private String sessionId;

    @Indexed
    @Field("client_id")
    private String clientId;

    @Field("protocol_type")
    @Indexed
    private ConnectionType connectionType;

    @Field("timestamp")
    @Indexed
    private LocalDateTime timestamp;

    @Field("direction")
    private Direction direction;

    @Field("payload")
    private CompressedPayload payload;

    @Field("metadata")
    private Map<String, Object> metadata;

    @Version
    private Long version;

}
