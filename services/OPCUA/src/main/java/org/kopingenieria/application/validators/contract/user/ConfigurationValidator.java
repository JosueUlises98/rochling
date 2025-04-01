package org.kopingenieria.application.validators.contract.user;

import org.kopingenieria.config.opcua.user.UserConfiguration;

public interface ConfigurationValidator {
    void validateConnection(UserConfiguration config);
    void validateAuthentication(UserConfiguration config);
    void validateEncryption(UserConfiguration config);
    void validateSession(UserConfiguration config);
    void validateSubscription(UserConfiguration config);
    void validateIndustrialConfiguration(UserConfiguration config);

    String getValidationResult();
}
