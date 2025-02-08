package org.kopingenieria.model;

public class MainConnection {
    public static void main(String[] args) {
        //Creacion de tipos de conexiones
        SSLConnection sslConnection = new SSLConnection()
                .withName("SSL Connection")
                .withHostname("192.168.1.1")
                .withPort(4840)
                .withUsername("admin")
                .withPassword("password")
                .withMethod("TCP/IP")
                .setCertificate("certificate")
                .setPrivateKey("private key")
                .setVerified(true)
                .build();
        TCPConnection standardConnection = new TCPConnection()
                .withHostname("127.0.0.1")
                .withName("standard-connection")
                .withPort(3306)
                .withUsername("user")
                .withPassword("kop")
                .withMethod("LDAP/kerberos")
                .withType("standard")
                .build();
        OpcUaConnection advancedConnection = new OpcUaConnection()
                .withName("advancedconnection")
                .withHostname("192.168.54.89")
                .withPort(8080)
                .withUsername("fer")
                .withPassword("hola")
                .withMethod("TCP/IP")
                .withType("advanced")
                .setSecurityKey("RSA-1234-_:;")
                .build();

        //Impresion de conexiones
        System.out.println(sslConnection.toString());
        System.out.println(standardConnection.toString());
        System.out.println(advancedConnection.toString());

    }
}
