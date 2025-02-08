package org.kopingenieria.model;

import java.io.IOException;
import java.util.List;

public class MainPdf {
    public static void main(String[] args) throws IOException {
        // Ruta del archivo PDF de salida
        String outputPath = "sessions.pdf";
        // Crear generador de PDFs
        ProfessionalPdfGenerator pdfGenerator = new ProfessionalPdfGenerator(outputPath);
        // Sesiones de ejemplo
        List<SessionObject> sessions = List.of(new SessionObject("123","session-1","user-1",SessionStatus.ACTIVE,"2025-01-10T23:59:59","2025-01-10T23:59:59")
        ,new SessionObject("345","session-2","user-2",SessionStatus.EXPIRED,"2025-01-10T23:59:59","2025-01-10T23:59:59")
        ,new SessionObject("789","session-3","user-3",SessionStatus.INACTIVE,"2025-01-10T23:59:59","2025-01-10T23:59:59")
        ,new SessionObject("156","session-4","user-4",SessionStatus.PENDING,"2025-01-10T23:59:59","2025-01-10T23:59:59")
        ,new SessionObject("120","session-5","user-5",SessionStatus.ACTIVE,"2025-01-10T23:59:59","2025-01-10T23:59:59")
        ,new SessionObject("897","session-6","user-6",SessionStatus.INACTIVE,"2025-01-10T23:59:59","2025-01-10T23:59:59")
        ,new SessionObject("369","session-7","user-7",SessionStatus.EXPIRED,"2025-01-10T23:59:59","2025-01-10T23:59:59")
        ,new SessionObject("369","session-7","user-7",SessionStatus.EXPIRED,"2025-01-10T23:59:59","2025-01-10T23:59:59")
        ,new SessionObject("369","session-7","user-7",SessionStatus.EXPIRED,"2025-01-10T23:59:59","2025-01-10T23:59:59")
        ,new SessionObject("369","session-7","user-7",SessionStatus.EXPIRED,"2025-01-10T23:59:59","2025-01-10T23:59:59"));
        // Generar el PDF con las sesiones
        pdfGenerator.generatePdf("Sesiones de Usuarios", sessions);
        System.out.println("PDF generado correctamente en: " + outputPath);
    }
}
