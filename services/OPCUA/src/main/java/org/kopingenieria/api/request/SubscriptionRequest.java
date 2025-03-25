package org.kopingenieria.api.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.domain.enums.monitoring.MonitoringMode;

@Data
@Builder
public class SubscriptionRequest {

    @NotBlank(message = "El nodeId es obligatorio")
    private String nodeId;

    @NotNull(message = "El publishingInterval es obligatorio")
    @Positive(message = "El publishingInterval debe ser un valor positivo")
    private Double publishingInterval;

    @NotNull(message = "El lifetimeCount es obligatorio")
    @Min(value = 1, message = "El lifetimeCount debe ser al menos 1")
    private UInteger lifetimeCount;

    @NotNull(message = "El maxKeepAliveCount es obligatorio")
    @Min(value = 1, message = "El maxKeepAliveCount debe ser al menos 1")
    private UInteger maxKeepAliveCount;

    @NotNull(message = "El maxNotificationsPerPublish es obligatorio")
    @Min(value = 1, message = "El maxNotificationsPerPublish debe ser al menos 1")
    private UInteger maxNotificationsPerPublish;

    @NotNull(message = "El publishingEnabled es obligatorio")
    private Boolean publishingEnabled;

    @NotNull(message = "El priority es obligatorio")
    private UByte priority;

    // Parámetros para MonitoredItem
    @NotNull(message = "El samplingInterval es obligatorio")
    @Positive(message = "El samplingInterval debe ser un valor positivo")
    private Double samplingInterval;

    @NotNull(message = "El queueSize es obligatorio")
    @Min(value = 1, message = "El queueSize debe ser al menos 1")
    private UInteger queueSize;

    @NotNull(message = "El discardOldest es obligatorio")
    private Boolean discardOldest;

    @NotNull(message = "El monitoringMode es obligatorio")
    private MonitoringMode monitoringMode;

    @NotNull(message = "El timestampsToReturn es obligatorio")
    private TimestampsToReturn timestampsToReturn;


}
