package org.kopingenieria.model;

public enum AuditEntryType {
    METHOD_ENTRY,
    METHOD_EXIT,
    REST_REQUEST,
    REST_RESPONSE,
    DATABASE_OPERATION,
    PERFORMANCE_METRIC,
    ERROR,
    SYSTEM_EVENT
}
