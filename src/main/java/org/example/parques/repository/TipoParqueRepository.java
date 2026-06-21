package org.example.parques.repository;

import org.example.parques.model.TipoParque;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Capa de acceso a datos para {@code parques.TipoParque}.
 *
 * <p>Todas las operaciones de escritura se delegan en procedimientos
 * almacenados de SQL Server. La consulta utiliza un SELECT directo.</p>
 */
@Repository
public class TipoParqueRepository {

    private final JdbcTemplate jdbcTemplate;

    /** Mapea cada fila del ResultSet a un objeto {@link TipoParque}. */
    private static final RowMapper<TipoParque> ROW_MAPPER = (rs, rowNum) ->
            new TipoParque(rs.getInt("id_tipo_parque"), rs.getString("descripcion"));

    public TipoParqueRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Lista todos los tipos de parque ordenados por id.
     * <pre>SELECT * FROM parques.TipoParque ORDER BY id_tipo_parque</pre>
     */
    public List<TipoParque> listar() {
        return jdbcTemplate.query(
                "SELECT * FROM parques.TipoParque ORDER BY id_tipo_parque",
                ROW_MAPPER);
    }

    /** Devuelve un tipo de parque por id (para precargar el formulario de edición). */
    public TipoParque buscarPorId(int id) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM parques.TipoParque WHERE id_tipo_parque = ?",
                ROW_MAPPER, id);
    }

    /** Alta: EXEC parques.TipoParqueInsertar @p_descripcion */
    public void insertar(String descripcion) {
        jdbcTemplate.update(
                "EXEC parques.TipoParqueInsertar @p_descripcion = ?",
                descripcion);
    }

    /** Modificación: EXEC parques.TipoParqueModificar @p_id_tipo_parque, @p_descripcion */
    public void modificar(int id, String descripcion) {
        jdbcTemplate.update(
                "EXEC parques.TipoParqueModificar @p_id_tipo_parque = ?, @p_descripcion = ?",
                id, descripcion);
    }

    /** Baja: EXEC parques.TipoParqueEliminar @p_id_tipo_parque */
    public void eliminar(int id) {
        jdbcTemplate.update(
                "EXEC parques.TipoParqueEliminar @p_id_tipo_parque = ?",
                id);
    }
}
