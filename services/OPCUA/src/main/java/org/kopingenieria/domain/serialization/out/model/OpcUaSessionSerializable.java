package org.kopingenieria.domain.serialization.out.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.kopingenieria.domain.model.SessionConfiguration;
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
@JsonRootName(value = "opcua-session-data")
@XmlRootElement(name = "opcua-session-data")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({"session", "timestamp","metadata"})
public class OpcUaSessionSerializable extends OpcUaSerializable {

    @Serial
    private static final long serialVersionUID = 500L;

    @JsonProperty("session")
    @XmlElement(name = "session")
    @NotNull(message = "La sesión OPC UA no puede ser nula")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private SessionConfiguration session;

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

    // Constructor de copia para garantizar la inmutabilidad
    public OpcUaSessionSerializable(OpcUaSessionSerializable source) {
        super();
        this.session = source.getSession();
        this.timestamp = source.getTimestamp();
        this.metadata = new HashMap<>(source.getMetadata());
    }

    // Métodos de validación
    @JsonIgnore
    public boolean isValid() {
        return session != null && timestamp != null && metadata != null;
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

    public OpcUaSessionSerializable clone() {
        try {
            OpcUaSessionSerializable clone = (OpcUaSessionSerializable) super.clone();
            clone.session = this.session; // Asumiendo que OpcUaSession es inmutable
            clone.timestamp = this.timestamp;
            clone.metadata = new HashMap<>(this.metadata);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar OpcUaSessionSerializable", e);
        }
    }

    public String toString() {
        return "OpcUaSessionSerializable{" +
                "sessionId='" + (session != null ? session.toString() : "null") + '\'' +
                ", timestamp=" + timestamp +
                ", metadataSize=" + (metadata != null ? metadata.size() : 0) +
                '}';
    }

}
