package org.kopingenieria.model;

public class SSLConnection extends Connection<SSLConnection>{

    private String certificate;
    private String privateKey;
    private boolean isVerified;

    public SSLConnection setCertificate(String certificate){
        this.certificate = certificate;
        return this;
    }

    public SSLConnection setPrivateKey(String privateKey){
        this.privateKey = privateKey;
        return this;
    }

    public SSLConnection setVerified(boolean isVerified){
        this.isVerified = isVerified;
        return this;
    }

    @Override
    protected SSLConnection self() {
        return this;
    }

    @Override
    public SSLConnection build() {
        SSLConnection connection = new SSLConnection();
        connection.name = this.name;
        connection.hostname = this.hostname;
        connection.port = this.port;
        connection.username = this.username;
        connection.password = this.password;
        connection.method = this.method;
        connection.certificate = this.certificate;
        connection.privateKey = this.privateKey;
        connection.isVerified = this.isVerified;
        return connection;
    }

    @Override
    public String toString() {
        return "SSLConnection{" +
                "certificate='" + certificate + '\'' +
                ", privateKey='" + privateKey + '\'' +
                ", isVerified=" + isVerified +
                ", name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", method='" + method + '\'' +
                "}";
    }

}
