package org.example.parques.repository;

import org.example.parques.model.OrganizacionDistinguida;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * Capa de acceso a datos para {@code estadisticas.OrganizacionesDistinguidas}.
 *
 * <p>El listado se obtiene con un SELECT directo. La carga de nuevas
 * organizaciones se delega íntegramente en el procedimiento almacenado
 * {@code importaciones.ImportarOrganizacionesDistinguidas}, que valida el CSV,
 * hace el upsert (sin MERGE) y devuelve un resumen con la cantidad de filas
 * insertadas, actualizadas y rechazadas.</p>
 */
@Repository
public class OrganizacionDistinguidaRepository {

    private final JdbcTemplate jdbcTemplate;

    /** Mapea cada fila del ResultSet a un objeto {@link OrganizacionDistinguida}. */
    private static final RowMapper<OrganizacionDistinguida> ROW_MAPPER = (rs, rowNum) -> {
        Date distincion = rs.getDate("fecha_distincion");
        Date revalidacion = rs.getDate("fecha_revalidacion");
        return new OrganizacionDistinguida(
                rs.getInt("id_organizacion"),
                rs.getString("organizacion"),
                rs.getString("rubro"),
                rs.getString("subrubro"),
                rs.getString("calle"),
                rs.getString("numero"),
                rs.getString("pais"),
                rs.getString("provincia"),
                rs.getString("ciudad"),
                rs.getString("telefono"),
                rs.getString("facebook"),
                rs.getString("web"),
                rs.getString("programa"),
                distincion == null ? null : distincion.toLocalDate(),
                revalidacion == null ? null : revalidacion.toLocalDate());
    };

    public OrganizacionDistinguidaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Lista todas las organizaciones distinguidas ordenadas por nombre. */
    public List<OrganizacionDistinguida> listar() {
        return jdbcTemplate.query(
                "SELECT id_organizacion, organizacion, rubro, subrubro, calle, numero, " +
                "       pais, provincia, ciudad, telefono, facebook, web, programa, " +
                "       fecha_distincion, fecha_revalidacion " +
                "FROM estadisticas.OrganizacionesDistinguidas " +
                "ORDER BY organizacion, calle, numero",
                ROW_MAPPER);
    }

    /**
     * Ejecuta {@code importaciones.ImportarOrganizacionesDistinguidas} con el
     * nombre de archivo indicado (relativo a {@code C:\Importaciones\}) y
     * devuelve la fila de resumen que produce el procedimiento.
     *
     * @return mapa con las claves {@code registros_insertados},
     *         {@code registros_actualizados} y {@code registros_rechazados}.
     */
    public Map<String, Object> importarCsv(String nombreArchivo) {
        return jdbcTemplate.queryForMap(
                "EXEC importaciones.ImportarOrganizacionesDistinguidas @p_ruta_archivo = ?",
                nombreArchivo);
    }
}
