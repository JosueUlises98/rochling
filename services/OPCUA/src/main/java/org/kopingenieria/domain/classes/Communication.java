package org.kopingenieria.domain.classes;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kopingenieria.domain.enums.client.network.communication.CompressedPayload;
import org.kopingenieria.domain.enums.client.network.communication.Direction;
import org.kopingenieria.domain.enums.client.network.connection.ProtocolType;
import org.springframework.data.annotation.*;
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
public class Communication implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Indexed
    private String sessionId;

    @Indexed
    private String clientId;

    @Field("protocol_type")
    @Indexed
    private ProtocolType protocolType;

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

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String lastModifiedBy;

    @CreatedDate
    private LocalDateTime createdDate;

    @LastModifiedDate
    private LocalDateTime lastModifiedDate;


}
