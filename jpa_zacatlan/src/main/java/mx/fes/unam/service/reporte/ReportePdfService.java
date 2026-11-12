package mx.fes.unam.service.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import mx.fes.unam.model.dto.ReporteDTO;
import mx.fes.unam.model.dto.VentaDTO;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

//Genera el PDF del reporte de ventas de un vendedor
@Service
public class ReportePdfService {
    private static final Color AZUL = new Color(0x0B, 0x2A, 0x6F);      // --zac-navy
    private static final Color GRIS = new Color(0xF2, 0xF7, 0xF6);      // --zac-mist
    private static final Color TEXTO = new Color(0x17, 0x20, 0x33);     // --zac-ink

    private static final Font F_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, AZUL);
    private static final Font F_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA, 10, TEXTO);
    private static final Font F_ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXTO);
    private static final Font F_ENCABEZADO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font F_CELDA = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXTO);
    private static final Font F_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TEXTO);
    private static final Font F_PIE = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

    private static final String[] COLUMNAS = {"Fecha y hora", "Producto", "Cantidad", "Precio unitario", "Total"};
    private static final float[] ANCHOS = {2.4f, 4.2f, 1.6f, 2.2f, 2.2f};

    public byte[] generar(ReporteDTO reporte) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 54);
        try {
            PdfWriter writer = PdfWriter.getInstance(doc, salida);
            writer.setPageEvent(new NumeroDePagina());
            doc.open();

            agregarEncabezado(doc, reporte);
            if (reporte.getFilas() == null || reporte.getFilas().isEmpty()) {
                Paragraph vacio = new Paragraph("No hay ventas registradas en este periodo.", F_SUBTITULO);
                vacio.setSpacingBefore(12);
                doc.add(vacio);
            } else {
                doc.add(tabla(reporte));
            }
            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        }
        return salida.toByteArray();
    }

    private void agregarEncabezado(Document doc, ReporteDTO reporte) throws DocumentException {
        doc.add(new Paragraph("Gobierno del Estado de Zacatlán", F_SUBTITULO));
        Paragraph titulo = new Paragraph("Reporte de ventas", F_TITULO);
        titulo.setSpacingAfter(10);
        doc.add(titulo);

        doc.add(linea("Vendedor: ", reporte.getVendedorNombre()));
        doc.add(linea("Bloque: ", reporte.getBloqueNombre()));
        doc.add(linea("Periodo: ", reporte.getPeriodoTexto()));
        Paragraph generado = linea("Generado: ",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        generado.setSpacingAfter(14);
        doc.add(generado);
    }

    private Paragraph linea(String etiqueta, String valor) {
        Paragraph p = new Paragraph();
        p.add(new Phrase(etiqueta, F_ETIQUETA));
        p.add(new Phrase(valor == null ? "-" : valor, F_SUBTITULO));
        return p;
    }

    private PdfPTable tabla(ReporteDTO reporte) throws DocumentException {
        PdfPTable tabla = new PdfPTable(COLUMNAS.length);
        tabla.setWidthPercentage(100);
        tabla.setWidths(ANCHOS);
        tabla.setHeaderRows(1);            // el encabezado se repite en cada página

        for (String col : COLUMNAS) {
            PdfPCell c = celda(col, F_ENCABEZADO, Element.ALIGN_LEFT, AZUL);
            tabla.addCell(c);
        }

        boolean sombrear = false;
        for (VentaDTO v : reporte.getFilas()) {
            Color fondo = sombrear ? GRIS : Color.WHITE;
            tabla.addCell(celda(v.getFechaHoraTexto(), F_CELDA, Element.ALIGN_LEFT, fondo));
            tabla.addCell(celda(v.getProductoNombre(), F_CELDA, Element.ALIGN_LEFT, fondo));
            tabla.addCell(celda(v.getCantidadTexto(), F_CELDA, Element.ALIGN_RIGHT, fondo));
            tabla.addCell(celda(dinero(v.getPrecioUnitario()), F_CELDA, Element.ALIGN_RIGHT, fondo));
            tabla.addCell(celda(dinero(v.getTotal()), F_CELDA, Element.ALIGN_RIGHT, fondo));
            sombrear = !sombrear;
        }

        String ventas = reporte.getTotalVentas() + (reporte.getTotalVentas() == 1 ? " venta" : " ventas");
        PdfPCell etiqueta = celda("Total del periodo (" + ventas + ")", F_TOTAL, Element.ALIGN_LEFT, GRIS);
        etiqueta.setColspan(COLUMNAS.length - 1);
        tabla.addCell(etiqueta);
        tabla.addCell(celda(dinero(reporte.getTotalMonto()), F_TOTAL, Element.ALIGN_RIGHT, GRIS));
        return tabla;
    }

    private PdfPCell celda(String texto, Font fuente, int alineacion, Color fondo) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fuente));
        c.setHorizontalAlignment(alineacion);
        c.setBackgroundColor(fondo);
        c.setPadding(5);
        c.setBorderColor(new Color(0xD5, 0xDB, 0xE3));
        c.setBorder(Rectangle.BOX);
        return c;
    }

    //1234.5 -> $1,234.50
    private String dinero(BigDecimal monto) {
        return String.format(Locale.US, "$%,.2f", monto == null ? BigDecimal.ZERO : monto);
    }

    //Pie de página: "Página N"
    private static class NumeroDePagina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                    new Phrase("Página " + writer.getPageNumber(), F_PIE),
                    (document.right() + document.left()) / 2, document.bottom() - 20, 0);
        }
    }
}
