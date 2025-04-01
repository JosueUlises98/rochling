package org.kopingenieria.application.validators.contract.user;

public interface UserAuthenticationValidator {

    boolean validateUserCredentials(String username, String password);

    boolean validateClientCertificate(String certificate);

    boolean isSessionTokenValid(String token);

    boolean enforcePasswordComplexity(String password);

    boolean isSessionValid(String token,Object... args);

    String getValidationResult();
}
