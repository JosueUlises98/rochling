package org.kopingenieria.domain.classes;

import org.kopingenieria.domain.enums.client.network.connection.ConnectionStatus;

public class MainUpdateStatus {
    public static void main(String[] args) {
        OpcUaConnection opcUaConnection = OpcUaConnection.builder()
                .hostname("siemens-s7")
                .port(4840)
                .applicationName("urn:siemens-s7:4840")
                .status(ConnectionStatus.CONNECTED)
                .build();

        opcUaConnection.updateStatus(ConnectionStatus.DISCONNECTED);
    }
}
