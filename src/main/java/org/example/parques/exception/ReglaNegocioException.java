package org.example.parques.exception;

/**
 * Excepción de negocio que transporta hacia la vista los mensajes de error
 * generados por los procedimientos almacenados (THROW de SQL Server).
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }

    public ReglaNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
