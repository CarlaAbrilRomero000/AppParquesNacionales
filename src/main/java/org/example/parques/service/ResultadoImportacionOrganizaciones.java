package org.example.parques.service;

/**
 * Resultado de importar un CSV de organizaciones distinguidas, usado para
 * informar a la vista. Refleja lo que devuelve el procedimiento almacenado
 * {@code importaciones.ImportarOrganizacionesDistinguidas}.
 *
 * @param exito        true si la importación finalizó correctamente
 * @param mensaje      texto descriptivo a mostrar al usuario
 * @param insertados   cantidad de organizaciones nuevas insertadas
 * @param actualizados cantidad de organizaciones existentes actualizadas
 * @param rechazados   cantidad de filas rechazadas (quedan en ErroresImportacion)
 */
public record ResultadoImportacionOrganizaciones(
        boolean exito,
        String mensaje,
        int insertados,
        int actualizados,
        int rechazados) {
}
