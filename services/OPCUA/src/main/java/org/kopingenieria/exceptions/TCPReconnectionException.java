package org.kopingenieria.exceptions;


public class TCPReconnectionException extends TCPConnectionException {
    public TCPReconnectionException(String mensaje) {
        super(mensaje);
    }
    public TCPReconnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
