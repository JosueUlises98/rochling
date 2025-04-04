package org.kopingenieria.application.validators.user;

import org.kopingenieria.config.opcua.user.UserConfiguration;

public interface UserConfigurationValidator {
    boolean validateConnection(UserConfiguration config);
    boolean validateAuthentication(UserConfiguration config);
    boolean validateEncryption(UserConfiguration config);
    boolean validateSession(UserConfiguration config);
    boolean validateIndustrialConfiguration(UserConfiguration config);
    String getValidationResult(UserConfiguration config);
}
