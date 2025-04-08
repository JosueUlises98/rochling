package org.kopingenieria.domain.enums.communication;

public enum Operation {
    READ,
    WRITE,
    UPDATE,
    DELETE,
    SUBSCRIBE,
    UNSUBSCRIBE,
    BROWSE,
    CALL;

    public static Operation fromString(String operationName) {
        for (Operation operation : Operation.values()) {
            if (operation.name().equalsIgnoreCase(operationName)) {
                return operation;
            }
        }
        throw new IllegalArgumentException("Invalid operation: " + operationName);
    }

    public boolean isRead() {
        return this == READ;
    }

    public boolean isWrite() {
        return this == WRITE;
    }

    public boolean isSubscribe() {
        return this == SUBSCRIBE;
    }

    public boolean isUnsubscribe() {
        return this == UNSUBSCRIBE;
    }

    public boolean isBrowse() {
        return this == BROWSE;
    }

    public boolean isCall() {
        return this == CALL;
    }
}
