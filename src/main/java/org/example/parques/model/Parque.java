package org.example.parques.model;

import java.math.BigDecimal;

/**
 * Representa una fila de la tabla {@code parques.Parque}.
 *
 * <p>POJO simple (sin Lombok) para mantener el proyecto fácil de leer.
 * El campo {@code tipoDescripcion} no existe en la tabla: se obtiene del
 * JOIN con {@code parques.TipoParque} para mostrar el nombre del tipo.</p>
 */
public class Parque {

    /** Identificador autoincremental (columna id_parque). */
    private Integer idParque;

    /** Código oficial del parque (columna codigo_oficial). */
    private String codigoOficial;

    /** Nombre del parque (columna nombre). */
    private String nombre;

    /** Ubicación geográfica (columna ubicacion). */
    private String ubicacion;

    /** Superficie en hectáreas (columna superficie). */
    private BigDecimal superficie;

    /** Id del tipo de parque (columna id_tipo_parque, FK a TipoParque). */
    private Integer idTipoParque;

    /** Descripción del tipo de parque (obtenida por JOIN, no es columna propia). */
    private String tipoDescripcion;

    public Parque() {
    }

    public Parque(Integer idParque, String codigoOficial, String nombre, String ubicacion,
                  BigDecimal superficie, Integer idTipoParque, String tipoDescripcion) {
        this.idParque = idParque;
        this.codigoOficial = codigoOficial;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.superficie = superficie;
        this.idTipoParque = idTipoParque;
        this.tipoDescripcion = tipoDescripcion;
    }

    public Integer getIdParque() {
        return idParque;
    }

    public void setIdParque(Integer idParque) {
        this.idParque = idParque;
    }

    public String getCodigoOficial() {
        return codigoOficial;
    }

    public void setCodigoOficial(String codigoOficial) {
        this.codigoOficial = codigoOficial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public BigDecimal getSuperficie() {
        return superficie;
    }

    public void setSuperficie(BigDecimal superficie) {
        this.superficie = superficie;
    }

    public Integer getIdTipoParque() {
        return idTipoParque;
    }

    public void setIdTipoParque(Integer idTipoParque) {
        this.idTipoParque = idTipoParque;
    }

    public String getTipoDescripcion() {
        return tipoDescripcion;
    }

    public void setTipoDescripcion(String tipoDescripcion) {
        this.tipoDescripcion = tipoDescripcion;
    }
}
