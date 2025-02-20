package org.kopingenieria.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
@JsonPropertyOrder({"opcua-connection", "connection-timestamp","connection-metadata"})
public class OpcUaConnectionSerializable extends OpcUaSerializable{

    @Serial
    private static final long serialVersionUID = 2L;

    @JsonProperty("opcua-connection")
    @XmlElement(name = "opcua-connection")
    @NotNull(message = "La conexion OPC UA no puede ser nula")
    private Connection<?> connection;

    @JsonProperty("connection-timestamp")
    @XmlElement(name = "connection-timestamp")
    @NotNull(message = "TimeStamp no puede ser nulo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private LocalDateTime timestamp;

    @JsonProperty("connection-metadata")
    @XmlElement(name = "connection-metadata")
    @NotNull(message = "Los metadatos no pueden ser nulos")
    private Map<String, String> metadata;

    @JsonIgnore
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
