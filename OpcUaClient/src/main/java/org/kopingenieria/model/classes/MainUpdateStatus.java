package org.kopingenieria.model.classes;

import org.kopingenieria.model.enums.network.ConnectionStatus;

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
