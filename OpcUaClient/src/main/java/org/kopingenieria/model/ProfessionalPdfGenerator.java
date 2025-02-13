package org.kopingenieria.model;

import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.IOException;
import java.util.List;

public class ProfessionalPdfGenerator {

    private String outputFilePath;

    public ProfessionalPdfGenerator(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }
    /**
     * Generates a professional PDF document with the specified title and session details.
     * This PDF includes a well-formatted table and a professional footer.
     *
     * @param title the title of the PDF document
     * @param session the session object containing the details to be included in the table
     * @throws IOException if an I/O error occurs during PDF generation
     */
    public void generatePdf(String title, SessionObject session) throws IOException {
        // Crear el documento PDF
        PdfWriter writer = new PdfWriter(outputFilePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        // Configurar diseño profesional
        document.add(new Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold()
                .setMarginBottom(20));
        // Agregar tabla para mostrar datos de las sesiones
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1, 1}))
                .useAllAvailableWidth();
        // Encabezados de la tabla
        String[] headers = {"ID", "Name", "UserId", "Status", "StartDate" , "EndDate"};
        for (String header : headers) {
            Cell headerCell = new Cell().add(new Paragraph(header).setBold())
                    .setBackgroundColor(DeviceGray.GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(headerCell);
        }   //Agregar las celdas con los valores correspondientes
            table.addCell(new Cell().add(new Paragraph(session.getId())));
            table.addCell(new Cell().add(new Paragraph(session.getName())));
            table.addCell(new Cell().add(new Paragraph(session.getUserId())));
            table.addCell(new Cell().add(new Paragraph(session.getStatus().name())));
            table.addCell(new Cell().add(new Paragraph(session.getStartDate().toString())));
            table.addCell(new Cell().add(new Paragraph(session.getEndDate().toString())));
        // Agregar la tabla al documento
        document.add(table);
        // Pie de página profesional
        document.add(new Paragraph("""
                © KOP INGENIERIA S.A de C.V 2025
                coordinacion.comercial@kopingenieria.com
                direccion@kopingenieria.com
            
                Calle Kappa 412 Col, Industrial Delta,
                37545 León, Gto.
                """)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setMarginTop(90)
                .setItalic());
        // Cerrar el documento
        document.close();
    }
}
