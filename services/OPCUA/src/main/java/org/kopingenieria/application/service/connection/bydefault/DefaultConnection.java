package org.kopingenieria.application.service.connection.bydefault;

import org.kopingenieria.api.response.connection.OpcUaConnectionResponse;
import org.kopingenieria.domain.enums.connection.UrlType;

import java.util.concurrent.CompletableFuture;


public interface DefaultConnection extends AutoCloseable {

    CompletableFuture<OpcUaConnectionResponse> connect()throws Exception;

    CompletableFuture<OpcUaConnectionResponse> connect(UrlType url)throws Exception;

    CompletableFuture<OpcUaConnectionResponse> disconnect()throws Exception;

    CompletableFuture<OpcUaConnectionResponse> backoffreconnection(UrlType url)throws Exception;

    CompletableFuture<OpcUaConnectionResponse> backoffreconnection()throws Exception;

    CompletableFuture<OpcUaConnectionResponse>linearreconnection(UrlType url)throws Exception;

    CompletableFuture<OpcUaConnectionResponse>linearreconnection()throws Exception;

    CompletableFuture<OpcUaConnectionResponse> ping()throws Exception;
}
