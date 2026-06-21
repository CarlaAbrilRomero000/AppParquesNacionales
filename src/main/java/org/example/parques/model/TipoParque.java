package org.example.parques.model;

/**
 * Representa una fila de la tabla {@code parques.TipoParque}.
 *
 * <p>Se utiliza un POJO simple (sin Lombok) para que el proyecto sea
 * fácil de leer y de abrir en cualquier IDE sin configuraciones extra.</p>
 */
public class TipoParque {

    /** Identificador autoincremental (columna id_tipo_parque). */
    private Integer idTipoParque;

    /** Descripción del tipo de parque (columna descripcion). */
    private String descripcion;

    public TipoParque() {
    }

    public TipoParque(Integer idTipoParque, String descripcion) {
        this.idTipoParque = idTipoParque;
        this.descripcion = descripcion;
    }

    public Integer getIdTipoParque() {
        return idTipoParque;
    }

    public void setIdTipoParque(Integer idTipoParque) {
        this.idTipoParque = idTipoParque;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
