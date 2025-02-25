package org.kopingenieria.model.classes;

public abstract sealed class Encryption permits TCPEncryption,TLSEncryption,SSHEncryption,OpcUaConnection  {
}
