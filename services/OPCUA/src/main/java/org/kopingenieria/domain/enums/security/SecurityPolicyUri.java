package org.kopingenieria.domain.enums.security;

public enum SecurityPolicyUri {

    NONE("http://opcfoundation.org/UA/SecurityPolicy#None"),
    BASIC128RSA15("http://opcfoundation.org/UA/SecurityPolicy#Basic128Rsa15"),
    BASIC256("http://opcfoundation.org/UA/SecurityPolicy#Basic256"),
    BASIC256SHA256("http://opcfoundation.org/UA/SecurityPolicy#Basic256Sha256"),
    AES128_SHA256_RSAOAEP("http://opcfoundation.org/UA/SecurityPolicy#Aes128_Sha256_RsaOaep"),
    AES256_SHA256_RSAPSS("http://opcfoundation.org/UA/SecurityPolicy#Aes256_Sha256_RsaPss");

    private final String uri;

    SecurityPolicyUri(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

    public static SecurityPolicyUri fromUri(String uri) {
        for (SecurityPolicyUri policy : values()) {
            if (policy.uri.equals(uri)) {
                return policy;
            }
        }
        throw new IllegalArgumentException("Unknown SecurityPolicyUri: " + uri);
    }
}
