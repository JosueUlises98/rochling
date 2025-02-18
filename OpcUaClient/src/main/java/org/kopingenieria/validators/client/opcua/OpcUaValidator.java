package org.kopingenieria.validators.client.opcua;

import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import java.util.function.Supplier;

public interface OpcUaValidator {
    public boolean validate(Supplier<UaClient> clientSupplier);
}
