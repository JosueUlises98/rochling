package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Data;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class DefaultSubscriptionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 15L;

    private String nodeId;
    private Double publishingInterval;
    private UInteger lifetimeCount;
    private UInteger maxKeepAliveCount;
    private UInteger maxNotificationsPerPublish;
    private Boolean publishingEnabled;
    private UByte priority;
    private Double samplingInterval;
    private UInteger queueSize;
    private Boolean discardOldest;
    private MonitoringMode monitoringMode;
    private TimestampsToReturn timestampsToReturn;
}
