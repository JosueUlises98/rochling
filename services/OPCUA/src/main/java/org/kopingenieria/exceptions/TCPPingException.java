package org.kopingenieria.exceptions;

public class TCPPingException extends TCPConnectionException {
    public TCPPingException(String mensaje) {
        super(mensaje);
    }
    public TCPPingException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
