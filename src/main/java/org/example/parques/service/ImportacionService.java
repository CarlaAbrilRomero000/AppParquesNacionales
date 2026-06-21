package org.example.parques.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de importar archivos CSV y generar a partir de ellos un
 * archivo XML que se guarda en disco.
 *
 * <p>La lógica es genérica: la primera fila del CSV se interpreta como
 * encabezados y cada fila siguiente se transforma en un nodo {@code <registro>}
 * dentro del XML.</p>
 */
@Service
public class ImportacionService {

    private static final DateTimeFormatter SELLO_TIEMPO =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** Carpeta donde se almacenan los XML generados (configurable). */
    private final Path carpetaSalida;

    /** Nombre del último XML generado correctamente (se muestra en la vista). */
    private volatile String ultimoXmlGenerado;

    public ImportacionService(
            @Value("${app.importacion.carpeta-xml:xml-generados}") String carpeta) {
        this.carpetaSalida = Paths.get(carpeta).toAbsolutePath();
    }

    public String getUltimoXmlGenerado() {
        return ultimoXmlGenerado;
    }

    /**
     * Procesa el archivo CSV recibido: lo lee, lo convierte a XML y guarda el
     * resultado en {@link #carpetaSalida}.
     */
    public ResultadoImportacion importar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return new ResultadoImportacion(false,
                    "Debe seleccionar un archivo CSV antes de importar.", 0, null);
        }

        String nombre = archivo.getOriginalFilename();
        if (nombre == null || !nombre.toLowerCase().endsWith(".csv")) {
            return new ResultadoImportacion(false,
                    "El archivo seleccionado no es un CSV válido.", 0, null);
        }

        try {
            List<String[]> filas = leerCsv(archivo);
            if (filas.isEmpty()) {
                return new ResultadoImportacion(false,
                        "El archivo CSV está vacío.", 0, null);
            }

            String xml = construirXml(filas);
            String nombreXml = guardarXml(xml);

            int cantidadDatos = Math.max(0, filas.size() - 1); // se descuenta el encabezado
            this.ultimoXmlGenerado = nombreXml;

            return new ResultadoImportacion(true,
                    "Importación realizada con éxito. Se procesaron " + cantidadDatos
                            + " registro(s).",
                    cantidadDatos, nombreXml);

        } catch (IOException ex) {
            return new ResultadoImportacion(false,
                    "Error al procesar el archivo: " + ex.getMessage(), 0, null);
        }
    }

    /** Lee el CSV (separador coma o punto y coma) en memoria. */
    private List<String[]> leerCsv(MultipartFile archivo) throws IOException {
        List<String[]> filas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                String separador = linea.contains(";") ? ";" : ",";
                filas.add(linea.split(separador, -1));
            }
        }
        return filas;
    }

    /** Construye el documento XML a partir de las filas del CSV. */
    private String construirXml(List<String[]> filas) {
        String[] encabezados = filas.get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<datos>\n");

        for (int i = 1; i < filas.size(); i++) {
            String[] fila = filas.get(i);
            sb.append("  <registro>\n");
            for (int c = 0; c < encabezados.length; c++) {
                String etiqueta = normalizarEtiqueta(encabezados[c]);
                String valor = c < fila.length ? fila[c] : "";
                sb.append("    <").append(etiqueta).append(">")
                        .append(escapar(valor))
                        .append("</").append(etiqueta).append(">\n");
            }
            sb.append("  </registro>\n");
        }
        sb.append("</datos>\n");
        return sb.toString();
    }

    /** Guarda el XML en disco y devuelve el nombre del archivo. */
    private String guardarXml(String xml) throws IOException {
        Files.createDirectories(carpetaSalida);
        String nombreXml = "importacion_" + LocalDateTime.now().format(SELLO_TIEMPO) + ".xml";
        Files.writeString(carpetaSalida.resolve(nombreXml), xml, StandardCharsets.UTF_8);
        return nombreXml;
    }

    /** Convierte un encabezado en un nombre de etiqueta XML válido. */
    private String normalizarEtiqueta(String texto) {
        if (texto == null || texto.isBlank()) {
            return "campo";
        }
        String limpio = texto.trim()
                .replaceAll("[^a-zA-Z0-9_]", "_")
                .replaceAll("^[^a-zA-Z_]+", "");
        return limpio.isBlank() ? "campo" : limpio;
    }

    /** Escapa los caracteres reservados de XML. */
    private String escapar(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.trim()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
