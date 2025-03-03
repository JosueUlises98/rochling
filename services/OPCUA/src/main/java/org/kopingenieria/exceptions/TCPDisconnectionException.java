package org.kopingenieria.exceptions;

public class TCPDisconnectionException extends TCPConnectionException {
    public TCPDisconnectionException(String mensaje) {
        super(mensaje);
    }
    public TCPDisconnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
