package org.example.parques.model;

import java.time.LocalDate;

/**
 * Representa una fila de la tabla {@code estadisticas.OrganizacionesDistinguidas}.
 *
 * <p>Estas organizaciones se cargan exclusivamente por importación de un CSV
 * (no hay alta/baja/modificación manual), por eso se modela como un
 * {@code record} de solo lectura.</p>
 */
public record OrganizacionDistinguida(
        Integer idOrganizacion,
        String organizacion,
        String rubro,
        String subrubro,
        String calle,
        String numero,
        String pais,
        String provincia,
        String ciudad,
        String telefono,
        String facebook,
        String web,
        String programa,
        LocalDate fechaDistincion,
        LocalDate fechaRevalidacion) {
}
