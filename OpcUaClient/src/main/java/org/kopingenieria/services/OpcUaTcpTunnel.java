package org.kopingenieria.services;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.model.UrlType;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpcUaTcpTunnel {

    private static final int PORT = 5050; // Puerto del túnel TCP
    private static final UrlType OPCUA_URL = UrlType.Adress2; // URL del servidor OPC UA
    private static ExecutorService threadPool = Executors.newCachedThreadPool(); // Pool de hilos para clientes

    public void start() throws Exception {
        // Netty: Definimos los grupos de hilos para manejar clientes.
        EventLoopGroup bossGroup = new NioEventLoopGroup(2); // Acepta nuevos clientes
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // Maneja conexiones existentes

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // Canal TCP
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });
            // Iniciar el servidor en el puerto especificado
            ChannelFuture channelFuture = serverBootstrap.bind(PORT).sync();
            System.out.println("Túnel TCP/IP iniciado en el puerto: " + PORT);
            // Esperar a que el canal cierre (bloqueante)
            channelFuture.channel().closeFuture().sync();
        } finally {
            // Cerrar grupos de hilos
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Handler que gestiona la conexión de cada cliente.
     */
    private static class ClientHandler extends SimpleChannelInboundHandler<String> {

        private OpcUaClient opcUaClient;
        private final ConnectionService connectionService=new TcpConnection();
        private final AuthenticationService authenticationService=new TCPAuthentication();
        private final RequestProcessorTcp requestProcessor=new RequestProcessorTcp();

        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            handleConnectionEstablished(ctx);
        }

        private void handleConnectionEstablished(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Conexión establecida con cliente: " + ctx.channel().remoteAddress());
            connectionService.conexion(UrlType.Adress1);
        }

        protected void channelRead0(ChannelHandlerContext ctx, String msg) {
            handleIncomingMessage(ctx, msg);
        }

        private void handleIncomingMessage(ChannelHandlerContext ctx, String msg) {
            threadPool.execute(() -> {
                try {
                    System.out.println("Mensaje recibido del cliente: " + msg);
                    String response = processRequest(msg);
                    sendResponse(ctx, response);
                } catch (Exception e) {
                    sendError(ctx, e.getMessage());
                }
            });
        }

        private void sendResponse(ChannelHandlerContext ctx, String response) {
            ctx.writeAndFlush(response + "\n");
        }

        private void sendError(ChannelHandlerContext ctx, String errorMessage) {
            ctx.writeAndFlush("Error procesando la solicitud: " + errorMessage + "\n");
        }

        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            handleClientDisconnection(ctx);
        }

        private void handleClientDisconnection(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Cliente desconectado: " + ctx.channel().remoteAddress());
            if (opcUaClient != null) {
                opcUaClient.disconnect().get();
                System.out.println("Desconectado del servidor OPC UA.");
            }
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            handleException(ctx, cause);
        }

        private void handleException(ChannelHandlerContext ctx, Throwable cause) {
            System.err.println("Error en conexión con cliente: " + cause.getMessage());
            closeConnection(ctx);
        }

        private void closeConnection(ChannelHandlerContext ctx) {
            ctx.close(); // Cerrar la conexión cuando ocurre un error
        }

        // Handle retries for failed connections.
        private void retryConnection(ChannelHandlerContext ctx) {
            System.out.println("Intentando reconectar con el cliente: " + ctx.channel().remoteAddress());
            ctx.channel().eventLoop().schedule(() -> {
                try {
                    handleConnectionEstablished(ctx);
                }catch (Exception e) {
                    System.err.println("Error en el reintento de conexión: " + e.getMessage());
                }
            }, 5, java.util.concurrent.TimeUnit.SECONDS); // Retry after 5 seconds
        }

        // Configure read timeout for a session.
        private void configureTimeout(SocketChannel channel) {
            channel.pipeline().addLast("readTimeoutHandler", new io.netty.handler.timeout.ReadTimeoutHandler(30));
            System.out.println("Timeout de sesión configurado en 30 segundos.");
        }
        


    }
}
