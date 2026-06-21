package org.example.parques.repository;

import org.example.parques.model.Parque;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Capa de acceso a datos para {@code parques.Parque} (solo lectura).
 *
 * <p>El listado se obtiene con un SELECT directo que une la tabla con
 * {@code parques.TipoParque} para mostrar el nombre del tipo de cada parque.</p>
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
}
