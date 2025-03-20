package org.kopingenieria.domain.enums.communication;


import java.io.Serializable;

public enum SessionStatus implements Serializable {

    ACTIVE(1),
    INACTIVE(0),
    EXPIRED(-1),
    TERMINATED(-2);

    private int sessionstatus;

    SessionStatus(int sessionstatus) {
        this.sessionstatus = sessionstatus;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
    public boolean isInactive() {
        return this == INACTIVE;
    }
    public boolean isExpired() {
        return this == EXPIRED;
    }
    public boolean isTerminated() {return this == TERMINATED;}

    public static SessionStatus fromString(String status) {
        for (SessionStatus s : SessionStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid session status: " + status);
    }
}
