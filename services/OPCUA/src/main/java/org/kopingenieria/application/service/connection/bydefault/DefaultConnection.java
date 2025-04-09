package org.kopingenieria.application.service.connection.bydefault;

import org.kopingenieria.api.response.connection.ConnectionResponse;
import org.kopingenieria.domain.enums.connection.UrlType;

import java.util.concurrent.CompletableFuture;


public interface DefaultConnection extends AutoCloseable {

    CompletableFuture<ConnectionResponse> connect()throws Exception;

    CompletableFuture<ConnectionResponse> connect(UrlType url)throws Exception;

    CompletableFuture<ConnectionResponse> disconnect()throws Exception;

    CompletableFuture<ConnectionResponse> backoffreconnection(UrlType url)throws Exception;

    CompletableFuture<ConnectionResponse> backoffreconnection()throws Exception;

    CompletableFuture<ConnectionResponse>linearreconnection(UrlType url)throws Exception;

    CompletableFuture<ConnectionResponse>linearreconnection()throws Exception;

    CompletableFuture<ConnectionResponse> ping()throws Exception;
}
