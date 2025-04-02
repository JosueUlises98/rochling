package org.kopingenieria.application.validators.contract.bydefault;

import org.eclipse.milo.opcua.sdk.client.subscriptions.OpcUaSubscriptionManager;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.kopingenieria.domain.enums.connection.Timeouts;

public interface DefaultSubscriptionValidator {

    boolean validateNodeId(NodeId nodeId);

    boolean validatePublishingInterval(Double publishingInterval);

    boolean validateLifetimeCount(UInteger lifetimeCount);

    boolean validateMaxKeepAliveCount(UInteger maxKeepAliveCount);

    boolean validateMaxNotificationsPerPublish(UInteger maxNotificationsPerPublish);

    boolean validatePublishingEnabled(Boolean publishingEnabled);

    boolean validatePriority(UByte priority);

    boolean validateSamplingInterval(Double samplingInterval);

    boolean validateQueueSize(UInteger queueSize);

    boolean validateDiscardOldest(Boolean discardOldest);

    boolean validateTimestampsToReturn(TimestampsToReturn timestampsToReturn);

    boolean validateTimeout(Timeouts timeout);

    boolean validateLifetimeKeepAliveRelation(UInteger lifetimeCount, UInteger maxKeepAliveCount);

    boolean validateSamplingPublishingRelation(Double samplingInterval, Double publishingInterval);

    boolean validateCompleteSubscription(OpcUaSubscriptionManager subscription);

    String getValidationResult();
}
