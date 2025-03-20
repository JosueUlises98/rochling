package org.kopingenieria.domain.serialization.out.serializables;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.kopingenieria.application.db.entity.OpcUaConnection;

import javax.xml.bind.annotation.*;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonRootName(value = "opcua-connection-data")
@XmlRootElement(name = "opcua-connection-data")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({"connection", "timestamp","metadata"})
public class OpcUaConnectionSerializable extends OpcUaSerializable {

    @Serial
    private static final long serialVersionUID = 2L;

    @JsonProperty("connection")
    @XmlElement(name = "connection")
    @NotNull(message = "La conexion OPC UA no puede ser nula")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OpcUaConnection connection;

    @JsonProperty("timestamp")
    @XmlElement(name = "timestamp")
    @NotNull(message = "TimeStamp no puede ser nulo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime timestamp;

    @JsonProperty("metadata")
    @XmlElement(name = "metadata")
    @NotNull(message = "Los metadatos no pueden ser nulos")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Map<String, String> metadata;

    @JsonIgnore
    @XmlTransient
    private transient volatile Object lockObject;

    public OpcUaConnectionSerializable(OpcUaConnectionSerializable source) {
        this.connection = source.getConnection();
        this.timestamp = source.getTimestamp();
        this.metadata = new HashMap<>(source.getMetadata());
    }

    // Métodos de validación
    @JsonIgnore
    public boolean isValid() {
        return connection != null && timestamp != null && metadata != null;
    }

    // Métodos de utilidad para gestionar metadata
    @JsonIgnore
    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    @JsonIgnore
    public String getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    public OpcUaConnectionSerializable clone() {
        try {
            OpcUaConnectionSerializable clone = (OpcUaConnectionSerializable) super.clone();
            clone.connection = this.connection;
            clone.timestamp = this.timestamp;
            clone.metadata = new HashMap<>(this.metadata);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar OpcUaConnectionSerializable", e);
        }
    }

    public String toString() {
        return "OpcUaConnectionSerializable{" +
                "sessionId='" + (connection != null ? connection.toString() : "null") + '\'' +
                ", timestamp=" + timestamp +
                ", metadataSize=" + (metadata != null ? metadata.size() : 0) +
                '}';
    }
}
