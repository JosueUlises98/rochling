package org.kopingenieria.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SessionObject implements Serializable{

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("status")
    private SessionStatus status;

    @JsonProperty("startDate")
    private LocalDateTime startDate;

    @JsonProperty("endDate")
    private LocalDateTime endDate;

    public SessionObject(String id, String name, String userId, SessionStatus status, String startDate, String endDate) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.status = status;
        try {
            // Analiza las fechas directamente en el formato ISO (ej. `2025-01-10T23:59:59`)
            this.startDate = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El formato de la fecha de inicio no es válido: " + startDate, e);
        }
        try {
            this.endDate = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El formato de la fecha de fin no es válido: " + endDate, e);
        }
    }
    
    public SessionObject() {
    }

    public SessionObject(String id, String name, String userId, String status, String startDate, String endDate) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.status = SessionStatus.valueOf(status);
        try {
            // Analiza las fechas directamente en el formato ISO (ej. `2025-01-10T23:59:59`)
            this.startDate = LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El formato de la fecha de inicio no es válido: " + startDate, e);
        }
        try {
            this.endDate = LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("El formato de la fecha de fin no es válido: " + endDate, e);
        }
    }

    public static SessionObject fromSessionName(String sessionName) {
        String[] parts = sessionName.split(",");
        if (parts.length < 5) {
            throw new IllegalArgumentException("El sessionName no tiene el formato esperado: " + sessionName);
        }
        String id = extractValueAfterEquals(parts[0]);
        String name =extractValueAfterEquals(parts[1]);
        String userId =extractValueAfterEquals(parts[2]);
        String status = extractValueAfterEquals(parts[3]);
        String startdate = extractValueAfterEquals(parts[4]);
        String enddate0 = extractValueAfterEquals(parts[5]);
        String enddate1 = cleanDateValue(startdate);
        return new SessionObject(id, name, userId, status, startdate, enddate1);
    }

    public static String cleanDateValue(String date) {
        // Elimina cualquier carácter no esperado (como un carácter `}` al final)
        if (date != null) {
            return date.replaceAll("[^\\dT:.-]", "");
        }
        return null;
    }
    
    // Getters y setters necesarios para la serialización
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "SessionObject{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", userId='" + userId + '\'' +
                ", status=" + status +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
    /**
     * Extracts the value after the equals sign from a given string.
     * Example: "key=value" -> "value"
     *
     * @param input the string containing the key-value pair separated by '='
     * @return the value after the equals sign, or null if no '=' is present
     */
    public static String extractValueAfterEquals(String input) {
        if (input == null || !input.contains("=")) {
            return null;
        }
        return input.split("=", 2)[1];
    }
}
