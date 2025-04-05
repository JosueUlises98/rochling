package org.kopingenieria.domain.serialization.bydefault;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.kopingenieria.domain.model.bydefault.DefaultOpcUa;

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
@JsonRootName(value = "opcua-configuration-data")
@XmlRootElement(name = "opcua-configuration-data")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({"configuration", "timestamp","metadata"})
public class DefaultConfigurationSerializable extends DefaultOpcUaSerializable {

    @Serial
    private static final long serialVersionUID = 6000L;

    @JsonProperty("configuration")
    @XmlElement(name = "configuration")
    @NotNull(message = "La configuracion no puede ser nulo")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private DefaultOpcUa opcuaConfiguration;

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

    public DefaultConfigurationSerializable(DefaultConfigurationSerializable source) {
        this.opcuaConfiguration = source.getOpcuaConfiguration();
        this.timestamp = source.getTimestamp();
        this.metadata = new HashMap<>(source.getMetadata());
    }

    // Métodos de validación
    @JsonIgnore
    public boolean isValid() {
        return opcuaConfiguration != null && timestamp != null && metadata != null;
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

    public DefaultConfigurationSerializable clone() {
        try {
            DefaultConfigurationSerializable clone = (DefaultConfigurationSerializable) super.clone();
            clone.opcuaConfiguration = this.opcuaConfiguration;
            clone.timestamp = this.timestamp;
            clone.metadata = new HashMap<>(this.metadata);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar OpcUaConnectionSerializable", e);
        }
    }

    public String toString() {
        return "OpcUaConnectionSerializable{" +
                "configuration=" + opcuaConfiguration +
                ", timestamp=" + timestamp +
                ", metadataSize=" + (metadata != null ? metadata.size() : 0) +
                '}';
    }
}
