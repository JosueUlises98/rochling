package org.kopingenieria.domain.serialization.bydefault;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.kopingenieria.domain.model.bydefault.DefaultSubscriptionConfiguration;

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
@JsonRootName(value = "opcua-subscription-data")
@XmlRootElement(name = "opcua-subscription-data")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonPropertyOrder({"subscription", "timestamp", "metadata"})
public class DefaultSubscriptionSerializable extends DefaultOpcUaSerializable {

    @Serial
    private static final long serialVersionUID = 5000L;

    @JsonProperty("subscription")
    @XmlElement(name = "subscription")
    @NotNull(message = "La suscripcion OPC UA no puede ser nula")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private DefaultSubscriptionConfiguration subscription;

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
    public DefaultSubscriptionSerializable(DefaultSubscriptionSerializable source) {
        this.subscription = source.getSubscription();
        this.timestamp = source.getTimestamp();
        this.metadata = new HashMap<>(source.getMetadata());
    }

    // Métodos de validación
    @JsonIgnore
    public boolean isValid() {
        return subscription != null && timestamp != null && metadata != null;
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

    public DefaultSubscriptionSerializable clone() {
        try {
            DefaultSubscriptionSerializable clone = (DefaultSubscriptionSerializable) super.clone();
            clone.subscription = this.subscription; // Assuming SubscriptionConfiguration is immutable
            clone.timestamp = this.timestamp;
            clone.metadata = new HashMap<>(this.metadata);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error al clonar OpcUaSubscriptionSerializable", e);
        }
    }

    public String toString() {
        return "OpcUaSubscriptionSerializable{" +
                "subscriptionId='" + (subscription != null ? subscription.toString() : "null") + '\'' +
                ", timestamp=" + timestamp +
                ", metadataSize=" + (metadata != null ? metadata.size() : 0) +
                '}';
    }
}
