package org.kopingenieria.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class OpcUaConnection extends Connection<OpcUaConnection>{

    private String securityPolicy;
    private String securityMode;
    private String securityLevel;
    private String securityKey;
    private String securityCertificate;
    private String securityPrivateKey;

    public OpcUaConnection setSecurityPolicy(String securityPolicy){
        this.securityPolicy = securityPolicy;
        return this;
    }
    public OpcUaConnection setSecurityMode(String securityMode){
        this.securityMode = securityMode;
        return this;
    }
    public OpcUaConnection setSecurityLevel(String securityLevel){
        this.securityLevel = securityLevel;
        return this;
    }
    public OpcUaConnection setSecurityKey(String securityKey){
        this.securityKey = securityKey;
        return this;
    }
    public OpcUaConnection setSecurityCertificate(String securityCertificate){
        this.securityCertificate = securityCertificate;
        return this;
    }
    public OpcUaConnection setSecurityPrivateKey(String securityPrivateKey){
        this.securityPrivateKey = securityPrivateKey;
        return this;
    }

    @Override
    protected OpcUaConnection self() {
        return this;
    }

    @Override
    public OpcUaConnection build() {
        OpcUaConnection connection = new OpcUaConnection();
        connection.name = this.name;
        connection.hostname = this.hostname;
        connection.port = this.port;
        connection.username = this.username;
        connection.password = this.password;
        connection.method = this.method;
        connection.securityPolicy = this.securityPolicy;
        connection.securityMode = this.securityMode;
        connection.securityLevel = this.securityLevel;
        connection.securityKey = this.securityKey;
        connection.securityCertificate = this.securityCertificate;
        connection.securityPrivateKey = this.securityPrivateKey;
        return connection;
    }

    @Override
    public String toString() {
        return "AdvancedConnection{" +
                "securityPolicy='" + securityPolicy + '\'' +
                ", securityMode='" + securityMode + '\'' +
                ", securityLevel='" + securityLevel + '\'' +
                ", securityKey='" + securityKey + '\'' +
                ", securityCertificate='" + securityCertificate + '\'' +
                ", securityPrivateKey='" + securityPrivateKey + '\'' +
                ", name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", method='" + method + '\'' +
                "}";
    }
}
