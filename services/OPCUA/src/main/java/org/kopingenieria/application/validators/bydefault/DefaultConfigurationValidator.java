package org.kopingenieria.application.validators.bydefault;

import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;

public interface DefaultConfigurationValidator {
    boolean validateConnection(DefaultConfiguration config);
    boolean validateAuthentication(DefaultConfiguration config);
    boolean validateEncryption(DefaultConfiguration config);
    boolean validateSession(DefaultConfiguration config);
    boolean validateIndustrialConfiguration(DefaultConfiguration config);
    String getValidationResult(DefaultConfiguration config);
}
