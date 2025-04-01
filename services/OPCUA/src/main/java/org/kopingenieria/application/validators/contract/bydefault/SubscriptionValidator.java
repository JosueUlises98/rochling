package org.kopingenieria.application.validators.contract.bydefault;

import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.kopingenieria.domain.enums.connection.Timeouts;

public interface SubscriptionValidator {

    boolean validateNodeId(String nodeId);

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

    boolean validateCompleteSubscription(DefaultConfiguration.Subscription subscription);

    String getValidationResult();
}
