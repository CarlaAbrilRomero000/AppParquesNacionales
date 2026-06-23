package org.example.parques.repository;

import org.example.parques.model.Parque;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Capa de acceso a datos para {@code parques.Parque}.
 *
 * <p>El listado y la búsqueda por id se obtienen con un SELECT directo que une
 * la tabla con {@code parques.TipoParque} para mostrar el nombre del tipo de
 * cada parque. Todas las operaciones de escritura (alta, baja y modificación)
 * se delegan en procedimientos almacenados de SQL Server.</p>
 */
@Repository
public class ParqueRepository {

    private final JdbcTemplate jdbcTemplate;

    /** Mapea cada fila del ResultSet a un objeto {@link Parque}. */
    private static final RowMapper<Parque> ROW_MAPPER = (rs, rowNum) -> new Parque(
            rs.getInt("id_parque"),
            rs.getString("codigo_oficial"),
            rs.getString("nombre"),
            rs.getString("ubicacion"),
            rs.getBigDecimal("superficie"),
            rs.getInt("id_tipo_parque"),
            rs.getString("tipo_descripcion"));

    public ParqueRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Lista todos los parques (con el nombre de su tipo) ordenados por id. */
    public List<Parque> listar() {
        return jdbcTemplate.query(
                "SELECT p.id_parque, p.codigo_oficial, p.nombre, p.ubicacion, " +
                "       p.superficie, p.id_tipo_parque, " +
                "       t.descripcion AS tipo_descripcion " +
                "FROM parques.Parque p " +
                "LEFT JOIN parques.TipoParque t ON p.id_tipo_parque = t.id_tipo_parque " +
                "ORDER BY p.id_parque",
                ROW_MAPPER);
    }

    /** Devuelve un parque por id (para precargar el formulario de edición). */
    public Parque buscarPorId(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT p.id_parque, p.codigo_oficial, p.nombre, p.ubicacion, " +
                "       p.superficie, p.id_tipo_parque, " +
                "       t.descripcion AS tipo_descripcion " +
                "FROM parques.Parque p " +
                "LEFT JOIN parques.TipoParque t ON p.id_tipo_parque = t.id_tipo_parque " +
                "WHERE p.id_parque = ?",
                ROW_MAPPER, id);
    }

    /**
     * Alta:
     * {@code EXEC parques.ParqueInsertar @p_codigo_oficial, @p_nombre, @p_ubicacion,
     * @p_superficie, @p_id_tipo_parque}
     */
    public void insertar(String codigoOficial, String nombre, String ubicacion,
                         BigDecimal superficie, Integer idTipoParque) {
        jdbcTemplate.update(
                "EXEC parques.ParqueInsertar " +
                "@p_codigo_oficial = ?, @p_nombre = ?, @p_ubicacion = ?, " +
                "@p_superficie = ?, @p_id_tipo_parque = ?",
                codigoOficial, nombre, ubicacion, superficie, idTipoParque);
    }

    /**
     * Modificación:
     * {@code EXEC parques.ParqueModificar @p_id_parque, @p_codigo_oficial, @p_nombre,
     * @p_ubicacion, @p_superficie, @p_id_tipo_parque}
     */
    public void modificar(Integer idParque, String codigoOficial, String nombre, String ubicacion,
                          BigDecimal superficie, Integer idTipoParque) {
        jdbcTemplate.update(
                "EXEC parques.ParqueModificar " +
                "@p_id_parque = ?, @p_codigo_oficial = ?, @p_nombre = ?, @p_ubicacion = ?, " +
                "@p_superficie = ?, @p_id_tipo_parque = ?",
                idParque, codigoOficial, nombre, ubicacion, superficie, idTipoParque);
    }

    /** Baja: {@code EXEC parques.ParqueEliminar @p_id_parque} */
    public void eliminar(int id) {
        jdbcTemplate.update(
                "EXEC parques.ParqueEliminar @p_id_parque = ?",
                id);
    }
}
