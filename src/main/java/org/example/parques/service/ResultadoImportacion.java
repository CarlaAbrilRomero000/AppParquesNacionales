package org.example.parques.service;

/**
 * Resultado de una importación de CSV, utilizado para informar a la vista.
 *
 * @param exito           true si la importación finalizó correctamente
 * @param mensaje         texto descriptivo a mostrar al usuario
 * @param cantidadFilas   cantidad de filas de datos procesadas
 * @param nombreArchivoXml nombre del XML generado (si corresponde)
 */
public record ResultadoImportacion(
        boolean exito,
        String mensaje,
        int cantidadFilas,
        String nombreArchivoXml) {
}
