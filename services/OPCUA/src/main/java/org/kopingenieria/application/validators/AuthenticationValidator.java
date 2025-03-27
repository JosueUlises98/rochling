package org.kopingenieria.application.validators;

public interface AuthenticationValidator {

    boolean validateUserCredentials(String username, String password);

    boolean validateClientCertificate(String certificate);

    boolean isSessionTokenValid(String token);

    boolean enforcePasswordComplexity(String password);
}
