package org.example.parques.service;

import org.example.parques.model.OrganizacionDistinguida;
import org.example.parques.repository.OrganizacionDistinguidaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Lógica de negocio para las organizaciones distinguidas.
 *
 * <p>Solo permite consultar la lista existente e importar nuevas desde un CSV.
 * La importación guarda el archivo recibido en la carpeta que lee el motor de
 * SQL Server ({@code C:\Importaciones\} por defecto) y luego delega toda la
 * validación y el upsert en el procedimiento almacenado
 * {@code importaciones.ImportarOrganizacionesDistinguidas}.</p>
 */
@Service
public class OrganizacionDistinguidaService {

    private static final DateTimeFormatter SELLO_TIEMPO =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final OrganizacionDistinguidaRepository repository;

    /**
     * Carpeta desde la que el motor de SQL Server lee los CSV a importar.
     * Debe coincidir con la ruta base del procedimiento almacenado
     * ({@code C:\Importaciones\}).
     */
    private final Path carpetaImportacion;

    public OrganizacionDistinguidaService(
            OrganizacionDistinguidaRepository repository,
            @Value("${app.importacion.carpeta-csv:C:/Importaciones}") String carpetaCsv) {
        this.repository = repository;
        this.carpetaImportacion = Paths.get(carpetaCsv).toAbsolutePath();
    }

    public List<OrganizacionDistinguida> listar() {
        return repository.listar();
    }

    /**
     * Importa el CSV recibido: lo guarda en la carpeta de importación y ejecuta
     * el procedimiento almacenado, que devuelve el resumen de la operación.
     */
    public ResultadoImportacionOrganizaciones importar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return new ResultadoImportacionOrganizaciones(false,
                    "Debe seleccionar un archivo CSV antes de importar.", 0, 0, 0);
        }

        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || !nombreOriginal.toLowerCase().endsWith(".csv")) {
            return new ResultadoImportacionOrganizaciones(false,
                    "El archivo seleccionado no es un CSV válido.", 0, 0, 0);
        }

        String nombreArchivo = "organizaciones_" +
                LocalDateTime.now().format(SELLO_TIEMPO) + ".csv";

        try {
            Files.createDirectories(carpetaImportacion);
            Path destino = carpetaImportacion.resolve(nombreArchivo);
            try (var in = archivo.getInputStream()) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            return new ResultadoImportacionOrganizaciones(false,
                    "No se pudo guardar el archivo en la carpeta de importación ("
                            + carpetaImportacion + "): " + ex.getMessage(), 0, 0, 0);
        }

        try {
            Map<String, Object> resumen = repository.importarCsv(nombreArchivo);
            int insertados = aEntero(resumen.get("registros_insertados"));
            int actualizados = aEntero(resumen.get("registros_actualizados"));
            int rechazados = aEntero(resumen.get("registros_rechazados"));

            String mensaje = String.format(
                    "Importación realizada. Insertadas: %d, actualizadas: %d, rechazadas: %d.",
                    insertados, actualizados, rechazados);
            return new ResultadoImportacionOrganizaciones(true, mensaje,
                    insertados, actualizados, rechazados);
        } catch (DataAccessException ex) {
            return new ResultadoImportacionOrganizaciones(false,
                    mensajeDeBaseDatos(ex), 0, 0, 0);
        }
    }

    private int aEntero(Object valor) {
        return valor instanceof Number numero ? numero.intValue() : 0;
    }

    /**
     * Extrae el mensaje más específico de la cadena de excepciones, que
     * normalmente corresponde al texto del THROW lanzado por el procedimiento.
     */
    private String mensajeDeBaseDatos(DataAccessException ex) {
        Throwable causa = ex.getMostSpecificCause();
        String mensaje = causa != null ? causa.getMessage() : ex.getMessage();
        return (mensaje == null || mensaje.isBlank())
                ? "Ocurrió un error al importar el archivo en la base de datos."
                : mensaje;
    }
}
