package org.kopingenieria.exceptions;

public class TCPConnectionException extends ConnectionException{
    public TCPConnectionException(String mensaje) {
        super(mensaje);
    }
    public TCPConnectionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
