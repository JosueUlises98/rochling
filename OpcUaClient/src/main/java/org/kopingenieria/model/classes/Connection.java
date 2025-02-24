package org.kopingenieria.model.classes;

import jakarta.persistence.MappedSuperclass;
import org.kopingenieria.model.enums.network.ConnectionStatus;
import org.kopingenieria.model.enums.network.ConnectionType;

import java.io.Serializable;
import java.time.LocalDateTime;


@MappedSuperclass
public abstract class Connection<T extends Connection<T>> implements Serializable {

    protected String name;
    protected String hostname;
    protected int port;
    protected String method;
    protected ConnectionType type;
    protected ConnectionStatus status;
    protected QualityConnection qualityConnection;

    public T withName(String name) {
        this.name = name;
        return self();
    }

    public T withHostname(String hostname) {
        this.hostname = hostname;
        return self();
    }

    public T withPort(int port) {
        this.port = port;
        return self();
    }

    public T withMethod(String method) {
        this.method = method;
        return self();
    }

    public T withType(ConnectionType type) {
        this.type = type;
        return self();
    }

    public T withStatus(ConnectionStatus status) {
        this.status = status;
        return self();
    }

    public T withQualityConnection(QualityConnection qualityConnection) {
        this.qualityConnection = qualityConnection;
        return self();
    }

    protected abstract T self();

    protected abstract Connection<T> build();

    //Metodo auxiliar
    protected void updateStatus(ConnectionStatus newStatus) {
        switch (newStatus) {
            case CONNECTED:
                this.status = ConnectionStatus.CONNECTED;
                this.qualityConnection.setLastConnected(LocalDateTime.now());
                this.qualityConnection.setConnectionCount(
                        qualityConnection.getConnectionCount() == null ? 1 : qualityConnection.getConnectionCount() + 1
                );
                break;
            case DISCONNECTED:
                this.status = ConnectionStatus.DISCONNECTED;
                this.qualityConnection.setLastDisconnected(LocalDateTime.now());
                break;
            case ERROR:
                this.status = ConnectionStatus.ERROR;
                this.qualityConnection.setErrorCount(
                        this.qualityConnection.getErrorCount() == null ? 1 : this.qualityConnection.getErrorCount() + 1
                );
                break;
            case RECONNECTING:
                this.status = ConnectionStatus.RECONNECTING;
                this.qualityConnection.setLastReconnecting(LocalDateTime.now());
                break;
            case UNKNOWN:
                this.status = ConnectionStatus.UNKNOWN;
                this.qualityConnection.setUnknownStatusCount(
                        this.qualityConnection.getUnknownStatusCount() == null ? 1 : this.qualityConnection.getUnknownStatusCount() + 1
                );
                break;
            case CONNECTING:
                this.status = ConnectionStatus.CONNECTING;
                this.qualityConnection.setLastConnecting(LocalDateTime.now());
                this.qualityConnection.setConnectingCount(
                        this.qualityConnection.getConnectingCount() == null ? 1 : this.qualityConnection.getConnectingCount() + 1
                );
                break;
            case DISCONNECTING:
                this.status = ConnectionStatus.DISCONNECTING;
                this.qualityConnection.setLastDisconnecting(LocalDateTime.now());
                break;
            case FAILED:
                this.status = ConnectionStatus.FAILED;
                this.qualityConnection.setLastFailed(LocalDateTime.now());
                this.qualityConnection.setFailedcount(
                        this.qualityConnection.getFailedcount() == null ? 1 : this.qualityConnection.getFailedcount() + 1
                );
                break;
            default:
                this.status = newStatus;
                break;
        }
    }

}
