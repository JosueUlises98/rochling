package org.kopingenieria.domain.dto;

import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;

public record SubscriptionDTO(String nodeId,
                              Double publishingInterval,
                              UInteger lifetimeCount,
                              UInteger maxKeepAliveCount,
                              UInteger maxNotificationsPerPublish,
                              Boolean publishingEnabled,
                              UByte priority,
                              Double samplingInterval,
                              UInteger queueSize,
                              Boolean discardOldest,
                              MonitoringMode monitoringMode,
                              TimestampsToReturn timestampsToReturn) {
}
