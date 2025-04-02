package org.kopingenieria.application.validators.contract.bydefault;

public interface DefaultAuthenticationValidator {

    boolean validateDefaultCredentials(String username, String password);

    boolean validateDefaultCertificate(String certificate);

    boolean isSessionTokenValid(String token);

    boolean enforcePasswordComplexity(String password);

    boolean isSessionValid(String token,Object... args);

    String getValidationResult(String[] data,Object[]sessiondata);
}
