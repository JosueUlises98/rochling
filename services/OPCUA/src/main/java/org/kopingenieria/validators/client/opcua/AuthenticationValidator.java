package org.kopingenieria.validators.client.opcua;

public interface AuthenticationValidator {
    /**
     * Validates the user credentials, such as username and password.
     * @param username the username to authenticate
     * @param password the password to authenticate
     * @return true if the credentials are valid, false otherwise
     */
    boolean validateUserCredentials(String username, String password);
    /**
     * Validates the client's authentication by verifying the provided certificate.
     * @param certificate the certificate to validate
     * @return true if the certificate is valid, false otherwise
     */
    boolean validateClientCertificate(String certificate);
    /**
     * Checks if the session token provided is still valid and not expired.
     * @param token the session token to verify
     * @return true if the token is valid, false otherwise
     */
    boolean isSessionTokenValid(String token);
    /**
     * Enforces password complexity rules during password creation or reset.
     * @param password the password to validate against complexity rules
     * @return true if the password meets the complexity requirements, false otherwise
     */
    boolean enforcePasswordComplexity(String password);
    
}
