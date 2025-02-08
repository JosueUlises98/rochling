package org.kopingenieria.model;

public class TCPConnection extends Connection<TCPConnection>{

    @Override
    protected TCPConnection self() {
        return this;
    }

    @Override
    protected TCPConnection build() {
        TCPConnection sc = new TCPConnection();
        sc.name = this.name;
        sc.hostname = this.hostname;
        sc.port=this.port;
        sc.username=this.username;
        sc.password=this.password;
        sc.method=this.method;
        sc.type=this.type;
        return sc;
    }

    @Override
    public String toString() {
        return "StandardConnection{" +
                "name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", method='" + method + '\'' +
                ", type='" + type + '\'' +
                "} ";
    }
}
