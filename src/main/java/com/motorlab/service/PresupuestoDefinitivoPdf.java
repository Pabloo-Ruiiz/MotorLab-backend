package com.motorlab.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.motorlab.config.DatosTaller;
import com.motorlab.entity.LineaPresupuestoDefinitivo;
import com.motorlab.entity.PresupuestoDefinitivo;
import com.motorlab.entity.enums.TipoLinea;
import com.motorlab.entity.enums.TipoServicio;

// Documento PDF del presupuesto definitivo con la identidad visual de MotorLab (docs/DESIGN.md)
@Component
public class PresupuestoDefinitivoPdf {

    private static final Color TINTA = new Color(0x0a, 0x0a, 0x0a);
    private static final Color PIZARRA = new Color(0x3b, 0x3b, 0x3b);
    private static final Color ACERO = new Color(0x54, 0x54, 0x54);
    private static final Color CREMA = new Color(0xf9, 0xf8, 0xf6);
    private static final Color ARENA = new Color(0xf2, 0xed, 0xe5);
    private static final Color LINEA = new Color(0xd5, 0xd9, 0xe2);

    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", ES);

    private final DatosTaller taller;

    public PresupuestoDefinitivoPdf(DatosTaller taller) {
        this.taller = taller;
    }

    public byte[] generar(PresupuestoDefinitivo definitivo) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 48, 48, 44, 72);
        PdfWriter escritor = PdfWriter.getInstance(documento, salida);
        escritor.setPageEvent(new PiePagina());
        documento.addTitle("Presupuesto de reparación " + definitivo.getNumero());
        documento.addAuthor(taller.nombre());
        documento.addCreator("MotorLab");
        documento.open();

        documento.add(cabecera(definitivo));
        documento.add(datosOrden(definitivo));
        documento.add(clienteYVehiculo(definitivo));
        documento.add(conceptos(definitivo));
        documento.add(totales(definitivo));
        documento.add(nota(definitivo));

        documento.close();
        return salida.toByteArray();
    }

    private PdfPTable cabecera(PresupuestoDefinitivo definitivo) {
        PdfPTable marca = tabla(new float[] {34, 160});
        PdfPCell logo = celda(logo());
        logo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        marca.addCell(logo);
        PdfPCell nombre = celda(new Phrase("MotorLab", fuente(15, Font.BOLD, TINTA)));
        nombre.setVerticalAlignment(Element.ALIGN_MIDDLE);
        nombre.setPaddingLeft(8);
        marca.addCell(nombre);

        PdfPTable cabecera = tabla(new float[] {1, 1});
        PdfPCell izquierda = celda(marca);
        izquierda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cabecera.addCell(izquierda);

        Paragraph titulo = new Paragraph("Presupuesto de reparación", fuente(19, Font.NORMAL, TINTA));
        titulo.setAlignment(Element.ALIGN_RIGHT);
        Paragraph numero = new Paragraph("Nº " + definitivo.getNumero(), fuente(9, Font.NORMAL, ACERO));
        numero.setAlignment(Element.ALIGN_RIGHT);
        PdfPCell derecha = celda(null);
        derecha.addElement(titulo);
        derecha.addElement(numero);
        cabecera.addCell(derecha);
        cabecera.setSpacingAfter(18);
        return cabecera;
    }

    private PdfPTable datosOrden(PresupuestoDefinitivo definitivo) {
        PdfPTable tabla = tabla(new float[] {1, 1, 1});
        tabla.addCell(dato("Orden de reparación", "#" + definitivo.getOrden().getId()));
        tabla.addCell(dato("Fecha de emisión", definitivo.getEmitidoEn().format(FECHA)));
        tabla.addCell(dato("Servicio", servicio(definitivo.getTipoServicio())));
        for (PdfPCell celda : tabla.getRow(0).getCells()) {
            celda.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
            celda.setBorderColor(LINEA);
            celda.setPaddingTop(9);
            celda.setPaddingBottom(11);
        }
        tabla.setSpacingAfter(16);
        return tabla;
    }

    private PdfPTable clienteYVehiculo(PresupuestoDefinitivo definitivo) {
        PdfPTable tabla = tabla(new float[] {1, 1});
        tabla.addCell(bloque("Cliente", List.of(
                destacado(definitivo.getClienteNombre()),
                texto(definitivo.getClienteEmail()),
                texto("Tel. " + definitivo.getClienteTelefono()))));

        String modelo = definitivo.getVehiculoMarca() + " " + definitivo.getVehiculoModelo()
                + (definitivo.getVehiculoVersion() == null ? "" : " " + definitivo.getVehiculoVersion());
        tabla.addCell(bloque("Vehículo", List.of(
                destacado(modelo),
                texto("Matrícula " + matricula(definitivo.getVehiculoMatricula())),
                texto(definitivo.getVehiculoAnio() + " · " + numero(BigDecimal.valueOf(definitivo.getVehiculoKilometros()))
                        + " km"))));
        tabla.setSpacingAfter(22);
        return tabla;
    }

    private PdfPTable conceptos(PresupuestoDefinitivo definitivo) {
        PdfPTable tabla = tabla(new float[] {4.2f, 1.5f, 1.1f, 1.4f, 1.4f});
        tabla.setHeaderRows(1);
        for (String titulo : List.of("Concepto", "Referencia", "Cantidad", "Precio", "Importe")) {
            PdfPCell celda = celda(new Phrase(titulo, fuente(8, Font.BOLD, ACERO)));
            celda.setBorder(Rectangle.BOTTOM);
            celda.setBorderColor(TINTA);
            celda.setBorderWidth(0.8f);
            celda.setPaddingBottom(7);
            celda.setPaddingLeft(7);
            celda.setPaddingRight(7);
            celda.setHorizontalAlignment(titulo.equals("Concepto") || titulo.equals("Referencia")
                    ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            tabla.addCell(celda);
        }

        grupo(tabla, "Mano de obra", definitivo, TipoLinea.mano_obra, definitivo.getSubtotalManoObra());
        grupo(tabla, "Piezas", definitivo, TipoLinea.pieza, definitivo.getSubtotalPiezas());
        tabla.setSpacingAfter(18);
        return tabla;
    }

    private void grupo(
            PdfPTable tabla, String titulo, PresupuestoDefinitivo definitivo, TipoLinea tipo, BigDecimal subtotal) {
        List<LineaPresupuestoDefinitivo> lineas = definitivo.getLineas().stream()
                .filter(linea -> linea.getTipo() == tipo)
                .toList();
        if (lineas.isEmpty()) {
            return;
        }

        PdfPCell cabecera = celda(new Phrase(titulo, fuente(9, Font.BOLD, TINTA)));
        cabecera.setColspan(5);
        cabecera.setBackgroundColor(ARENA);
        cabecera.setPadding(7);
        tabla.addCell(cabecera);

        for (LineaPresupuestoDefinitivo linea : lineas) {
            tabla.addCell(fila(linea.getConcepto(), Element.ALIGN_LEFT, TINTA));
            tabla.addCell(fila(linea.getReferencia() == null ? "—" : linea.getReferencia(), Element.ALIGN_LEFT, ACERO));
            tabla.addCell(fila(cantidad(linea), Element.ALIGN_RIGHT, TINTA));
            tabla.addCell(fila(euros(linea.getPrecioUnitario()) + (tipo == TipoLinea.mano_obra ? "/h" : ""),
                    Element.ALIGN_RIGHT, TINTA));
            tabla.addCell(fila(euros(linea.getImporte()), Element.ALIGN_RIGHT, TINTA));
        }

        PdfPCell etiqueta = celda(new Phrase("Subtotal " + titulo.toLowerCase(ES), fuente(9, Font.NORMAL, ACERO)));
        etiqueta.setColspan(4);
        etiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        etiqueta.setPadding(7);
        tabla.addCell(etiqueta);
        PdfPCell importe = celda(new Phrase(euros(subtotal), fuente(9, Font.BOLD, TINTA)));
        importe.setHorizontalAlignment(Element.ALIGN_RIGHT);
        importe.setPadding(7);
        tabla.addCell(importe);
    }

    private PdfPTable totales(PresupuestoDefinitivo definitivo) {
        PdfPTable tabla = tabla(new float[] {1.6f, 1});
        tabla.setWidthPercentage(46);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        filaTotal(tabla, "Base imponible", euros(definitivo.getSubtotal()), false);
        filaTotal(tabla, "IVA " + numero(definitivo.getIvaPorcentaje()) + " %", euros(definitivo.getIva()), false);
        filaTotal(tabla, "Total", euros(definitivo.getTotal()), true);
        tabla.setSpacingAfter(22);
        return tabla;
    }

    private void filaTotal(PdfPTable tabla, String etiqueta, String valor, boolean total) {
        Color fondo = total ? TINTA : CREMA;
        Color tinta = total ? Color.WHITE : TINTA;
        float tamano = total ? 12 : 9.5f;
        PdfPCell izquierda = celda(new Phrase(etiqueta, fuente(tamano, total ? Font.BOLD : Font.NORMAL, total ? tinta : ACERO)));
        PdfPCell derecha = celda(new Phrase(valor, fuente(tamano, Font.BOLD, tinta)));
        derecha.setHorizontalAlignment(Element.ALIGN_RIGHT);
        for (PdfPCell celda : List.of(izquierda, derecha)) {
            celda.setBackgroundColor(fondo);
            celda.setPaddingTop(total ? 10 : 7);
            celda.setPaddingBottom(total ? 12 : 7);
            celda.setPaddingLeft(12);
            celda.setPaddingRight(12);
            tabla.addCell(celda);
        }
    }

    private Paragraph nota(PresupuestoDefinitivo definitivo) {
        Paragraph nota = new Paragraph(
                "Presupuesto emitido al finalizar la reparación de la orden #" + definitivo.getOrden().getId()
                        + ". Recoge los trabajos realizados y las piezas utilizadas; sus importes no cambian aunque "
                        + "varíen después los precios del catálogo.",
                fuente(8, Font.NORMAL, ACERO));
        nota.setLeading(11);
        return nota;
    }

    private PdfPCell bloque(String titulo, List<Phrase> lineas) {
        PdfPCell celda = celda(null);
        celda.setBackgroundColor(CREMA);
        celda.setPadding(12);
        celda.setPaddingBottom(14);
        celda.setBorder(Rectangle.LEFT | Rectangle.RIGHT);
        celda.setBorderColor(Color.WHITE);
        celda.setBorderWidth(4);
        Paragraph cabecera = new Paragraph(titulo.toUpperCase(ES), fuente(7.5f, Font.BOLD, ACERO));
        cabecera.setSpacingAfter(5);
        celda.addElement(cabecera);
        for (Phrase linea : lineas) {
            Paragraph parrafo = new Paragraph(linea);
            parrafo.setLeading(14);
            celda.addElement(parrafo);
        }
        return celda;
    }

    private PdfPCell dato(String etiqueta, String valor) {
        PdfPCell celda = celda(null);
        celda.addElement(new Paragraph(etiqueta, fuente(7.5f, Font.NORMAL, ACERO)));
        celda.addElement(new Paragraph(valor, fuente(10.5f, Font.BOLD, TINTA)));
        return celda;
    }

    private PdfPCell fila(String texto, int alineacion, Color color) {
        PdfPCell celda = celda(new Phrase(texto, fuente(9, Font.NORMAL, color)));
        celda.setHorizontalAlignment(alineacion);
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(LINEA);
        celda.setBorderWidth(0.5f);
        celda.setPaddingTop(7);
        celda.setPaddingBottom(8);
        // Mismo margen lateral que los grupos y los subtotales: las cifras quedan en columna
        celda.setPaddingLeft(7);
        celda.setPaddingRight(7);
        return celda;
    }

    private Image logo() {
        try {
            Image logo = Image.getInstance(new ClassPathResource("correo/logo.png").getContentAsByteArray());
            logo.scaleToFit(30, 30);
            return logo;
        } catch (IOException excepcion) {
            throw new UncheckedIOException(excepcion);
        }
    }

    private static PdfPTable tabla(float[] anchos) {
        PdfPTable tabla = new PdfPTable(anchos);
        tabla.setWidthPercentage(100);
        return tabla;
    }

    private static PdfPCell celda(Object contenido) {
        PdfPCell celda;
        if (contenido instanceof Phrase frase) {
            celda = new PdfPCell(frase);
        } else if (contenido instanceof PdfPTable tabla) {
            celda = new PdfPCell(tabla);
        } else if (contenido instanceof Image imagen) {
            celda = new PdfPCell(imagen, false);
        } else {
            celda = new PdfPCell();
        }
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPadding(0);
        return celda;
    }

    private static Phrase destacado(String texto) {
        return new Phrase(texto, fuente(10.5f, Font.BOLD, TINTA));
    }

    private static Phrase texto(String texto) {
        return new Phrase(texto, fuente(9, Font.NORMAL, PIZARRA));
    }

    private static Font fuente(float tamano, int estilo, Color color) {
        return new Font(Font.HELVETICA, tamano, estilo, color);
    }

    private static String euros(BigDecimal importe) {
        DecimalFormat formato = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(ES));
        formato.setGroupingUsed(true);
        return formato.format(importe) + " €";
    }

    private static String numero(BigDecimal valor) {
        DecimalFormat formato = new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(ES));
        return formato.format(valor);
    }

    private static String cantidad(LineaPresupuestoDefinitivo linea) {
        return numero(linea.getCantidad()) + (linea.getTipo() == TipoLinea.mano_obra ? " h" : "");
    }

    // 4821KLB → 4821 KLB, como en la placa
    private static String matricula(String matricula) {
        return matricula.length() == 7 ? matricula.substring(0, 4) + " " + matricula.substring(4) : matricula;
    }

    private static String servicio(TipoServicio tipo) {
        return switch (tipo) {
            case frenos -> "Frenos";
            case motor -> "Motor";
            case aceite_filtros -> "Aceite y filtros";
            case neumaticos -> "Neumáticos";
            case electricidad -> "Electricidad";
            case itv -> "ITV";
            case otro -> "Otro";
        };
    }

    // Datos del taller y número de página al pie de cada hoja
    private final class PiePagina extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter escritor, Document documento) {
            PdfContentByte lienzo = escritor.getDirectContent();
            float y = documento.bottom() - 26;
            lienzo.setColorStroke(LINEA);
            lienzo.setLineWidth(0.5f);
            lienzo.moveTo(documento.left(), y + 14);
            lienzo.lineTo(documento.right(), y + 14);
            lienzo.stroke();

            Font pie = fuente(7.5f, Font.NORMAL, ACERO);
            ColumnText.showTextAligned(lienzo, Element.ALIGN_LEFT,
                    new Phrase(new Chunk(taller.resumen(), pie)), documento.left(), y, 0);
            ColumnText.showTextAligned(lienzo, Element.ALIGN_RIGHT,
                    new Phrase("Página " + escritor.getPageNumber(), pie), documento.right(), y, 0);
        }
    }
}
