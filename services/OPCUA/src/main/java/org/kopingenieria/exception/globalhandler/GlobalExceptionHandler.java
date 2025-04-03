package org.kopingenieria.exception.globalhandler;

import org.kopingenieria.api.response.ErrorResponse;
import org.kopingenieria.exception.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.CommunicationException;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ConfigurationException.class})
    public ResponseEntity<ErrorResponse> handleConfigurationException(ConfigurationException config, List<String> details) {
        return buildErrorResponse(config.getMessage(),"ConfigurationException",HttpStatus.BAD_REQUEST,details);
    }

    @ExceptionHandler({ConnectionException.class})
    public ResponseEntity<ErrorResponse> handleConnectionException(ConnectionException connex, List<String> details) {
        return buildErrorResponse(connex.getMessage(),"ConnectionException",HttpStatus.SERVICE_UNAVAILABLE,details);
    }

    @ExceptionHandler({AuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException auth, List<String> details) {
        return buildErrorResponse(auth.getMessage(),"AuthenticationException",HttpStatus.UNAUTHORIZED,details);
    }

    @ExceptionHandler({EncryptionException.class})
    public ResponseEntity<ErrorResponse> handleEncryptionException(EncryptionException encryp, List<String> details) {
       return buildErrorResponse(encryp.getMessage(),"EncryptionException",HttpStatus.BAD_REQUEST,details);
    }

    @ExceptionHandler({SessionException.class})
    public ResponseEntity<ErrorResponse> handleSessionException(SessionException session, List<String> details) {
        return buildErrorResponse(session.getMessage(),"SessionException",HttpStatus.UNAUTHORIZED,details);
    }

    @ExceptionHandler({CommunicationException.class})
    public ResponseEntity<ErrorResponse> handleCommunicationException(CommunicationException comun, List<String> details) {
        return buildErrorResponse(comun.getMessage(),"CommunicationException",HttpStatus.GATEWAY_TIMEOUT,details);
    }

    @ExceptionHandler({SubscriptionException.class})
    public ResponseEntity<ErrorResponse> handleSubscriptionException(SubscriptionException sub, List<String> details) {
        return buildErrorResponse(sub.getMessage(), "SubscriptionException", HttpStatus.CONFLICT, details);
    }

    @ExceptionHandler({SerializationException.class})
    public ResponseEntity<ErrorResponse> handleSerializationException(SerializationException ser, List<String> details) {
        return buildErrorResponse(ser.getMessage(),"SerializationException",HttpStatus.UNPROCESSABLE_ENTITY,details);
    }

    @ExceptionHandler({PoolException.class})
    public ResponseEntity<ErrorResponse> handlePoolException(PoolException pool, List<String> details) {
        return buildErrorResponse(pool.getMessage(),"PoolException",HttpStatus.SERVICE_UNAVAILABLE,details);
    }

    @ExceptionHandler({ConfigurationMappingException.class})
    public ResponseEntity<ErrorResponse> handleConfigurationMappingException(ConfigurationMappingException ex,List<String> details) {
        return buildErrorResponse(ex.getMessage(),"ConfigurationMappingException",HttpStatus.BAD_REQUEST,details);
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, List<String> details) {
        return buildErrorResponse(ex.getMessage(),"RuntimeException",HttpStatus.INTERNAL_SERVER_ERROR,details);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> handleException(Exception ex, List<String> details) {
        return buildErrorResponse("excepcion lanzada","Error en el sistema",HttpStatus.INTERNAL_SERVER_ERROR,details);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String message,
            String error,
            HttpStatus status,
            List<String> details
    ) {
        ErrorResponse response = ErrorResponse.builder()
                .message(message)
                .timestamp(LocalDateTime.now())
                .error(error)
                .details(details)
                .status(status)
                .build();
        return new ResponseEntity<>(response, status);
    }

}
