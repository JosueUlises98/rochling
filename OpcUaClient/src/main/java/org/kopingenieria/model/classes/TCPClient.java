package org.kopingenieria.model.classes;

import io.netty.channel.unix.Socket;
import java.io.InputStream;
import java.io.OutputStream;

public class TCPClient {

    private String serverAddress; // Stores the server's IP address or hostname
    private int serverPort; // Stores the server's port number
    private Socket socket; // Manages the socket connection
    private InputStream inputStream; // Handles incoming data
    private OutputStream outputStream; // Handles outgoing data

}
