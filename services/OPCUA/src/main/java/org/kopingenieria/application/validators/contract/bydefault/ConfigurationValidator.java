package org.kopingenieria.application.validators.contract.bydefault;

import org.kopingenieria.config.opcua.user.UserConfiguration;

public interface ConfigurationValidator {
    boolean validateConnection(UserConfiguration config);

    boolean validateAuthentication(UserConfiguration config);

    boolean validateEncryption(UserConfiguration config);

    boolean validateSession(UserConfiguration config);

    boolean validateSubscription(UserConfiguration config);

    boolean validateIndustrialConfiguration(UserConfiguration config);

    String getValidationResult();
}
