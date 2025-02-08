package org.kopingenieria.validators;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidatorSession {

    public static final Logger logger = LoggerFactory.getLogger(ValidatorSession.class);

    public boolean activeSession(OpcUaClient opcUaClient) {
        if (opcUaClient != null && !opcUaClient.disconnect().isDone()) {
            logger.warn("Session activa");
            return false;
        }
        return true;
    }

}
