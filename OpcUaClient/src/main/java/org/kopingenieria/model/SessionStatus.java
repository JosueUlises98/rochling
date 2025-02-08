package org.kopingenieria.model;


import java.io.Serializable;

public enum SessionStatus implements Serializable {

    ACTIVE(1),
    INACTIVE(0),
    EXPIRED(-1),
    PENDING(-2);

    private int sessionstatus;

    SessionStatus(int sessionstatus) {
        this.sessionstatus = sessionstatus;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }
    public boolean isPending() {
        return this == PENDING;
    }
    public boolean isInactive() {
        return this == INACTIVE;
    }
    public boolean isExpired() {
        return this == EXPIRED;
    }

    public static SessionStatus fromString(String status) {
        for (SessionStatus s : SessionStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid session status: " + status);
    }
}
