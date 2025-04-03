package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.monitoring.MonitoringMode;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Builder
public class DefaultSubscriptionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 5L;

    private final String nodeId;
    private final Double publishingInterval;
    private final UInteger lifetimeCount;
    private final UInteger maxKeepAliveCount;
    private final UInteger maxNotificationsPerPublish;
    private final Boolean publishingEnabled;
    private final UByte priority;
    private final Double samplingInterval;
    private final UInteger queueSize;
    private final Boolean discardOldest;
    private final MonitoringMode monitoringMode;
    private final TimestampsToReturn timestampsToReturn;
    private final Timeouts timeout=Timeouts.REQUEST;
}
