package org.kopingenieria.application.service;

import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;

/**
 * The Autentication interface defines methods for various authentication mechanisms.
 * These methods provide the ability to authenticate using different approaches,
 * based on the required credentials or configurations.
 */
public interface Autentication {
    /**
     * Authenticates anonymously and provides an {@link IdentityProvider} instance.
     * This method does not require any credentials and allows access to resources
     * or systems that support anonymous authentication.
     *
     * @return An {@link IdentityProvider} instance for anonymous authentication.
     */
    IdentityProvider authenticateAnonymously()throws Exception;
    /**
     * Authenticates a user by validating the provided username and password credentials.
     * This method returns an instance of {@link IdentityProvider} if authentication is successful.
     *
     * @param username The username of the user attempting to authenticate.
     * @param password The password associated with the provided username.
     * @return An {@link IdentityProvider} instance representing the authenticated user.
     * @throws IllegalArgumentException if the provided username or password is invalid or null.
     */
    IdentityProvider authenticateWithUsernameAndPassword(String username, String password) throws Exception;
    /**
     * Authenticates using a certificate and private key to obtain an {@link IdentityProvider}.
     * This method is designed for systems that require certificate-based authentication,
     * ensuring secure communication through credentials provided in the form of a certificate
     * and its associated private key.
     *
     * @param certificatePath The file path to the certificate used for authentication.
     * @param privateKeyPath The file path to the private key corresponding to the certificate.
     * @return An {@link IdentityProvider} instance representing the authenticated identity.
     * @throws Exception If the certificate or private key is invalid, inaccessible, or if
     *         there is an issue during the authentication process.
     */
    IdentityProvider authenticateWithCertificate(String certificatePath, String privateKeyPath) throws Exception;
    /**
     * Authenticates using custom data to obtain an {@link IdentityProvider}.
     * This method is designed to accommodate authentication mechanisms that require
     * user-defined or non-standard data structures. The specific interpretation of the
     * customData parameter depends on the implementation.
     *
     * @param customData The custom data object provided for authentication. This data
     *                   may include user-defined configurations, tokens, or credentials
     *                   specific to the authentication mechanism.
     * @return An {@link IdentityProvider} instance representing the authenticated identity.
     * @throws IllegalArgumentException if the customData is invalid or null.
     */
    IdentityProvider authenticateCustom(Object customData)throws Exception;
}
