package org.kopingenieria.domain.enums.client.network.connection;

public enum QualityLevel {

    EXCELLENT("Excelente", "#00FF00"),
    GOOD("Buena", "#90EE90"),
    FAIR("Regular", "#FFD700"),
    POOR("Mala", "#FF0000");

    private final String descripcion;
    private final String colorHex;

    QualityLevel(String descripcion, String colorHex) {
        this.descripcion = descripcion;
        this.colorHex = colorHex;
    }
}
