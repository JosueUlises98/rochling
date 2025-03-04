package org.kopingenieria.util;

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

public class ProfessionalPdfGenerator {

    private String outputFilePath;

    public ProfessionalPdfGenerator(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    /**
     * Generates a professional PDF document with the specified title and session details.
     * This PDF includes a well-formatted table and a professional footer.
     *
     * @param title   the title of the PDF document
     * @param session the session object containing the details to be included in the table
     * @throws IOException if an I/O error occurs during PDF generation
     */
    public void generatePdf(String title, SessionObject session) throws IOException {
        try (PdfWriter writer = new PdfWriter(outputFilePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            addTitle(document, title);
            addSessionTable(document, session);
            addFooter(document);
        }
    }

    private void addTitle(Document document, String title) {
        document.add(new Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(18)
                .setBold()
                .setMarginBottom(20));
    }

    private void addSessionTable(Document document, SessionObject session) {
        Table table = createSessionTable();
        populateSessionTable(table, session);
        document.add(table);
    }

    private Table createSessionTable() {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1, 1}))
                .useAllAvailableWidth();
        String[] headers = {"ID", "Name", "UserId", "Status", "StartDate", "EndDate"};
        for (String header : headers) {
            Cell headerCell = new Cell()
                    .add(new Paragraph(header).setBold())
                    .setBackgroundColor(DeviceGray.GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(headerCell);
        }
        return table;
    }

    private void populateSessionTable(Table table, SessionObject session) {
        table.addCell(new Cell().add(new Paragraph(session.getId())));
        table.addCell(new Cell().add(new Paragraph(session.getName())));
        table.addCell(new Cell().add(new Paragraph(session.getUserId())));
        table.addCell(new Cell().add(new Paragraph(session.getStatus().name())));
        table.addCell(new Cell().add(new Paragraph(session.getStartDate().toString())));
        table.addCell(new Cell().add(new Paragraph(session.getEndDate().toString())));
    }

    private void addFooter(Document document) {
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
    }
}
