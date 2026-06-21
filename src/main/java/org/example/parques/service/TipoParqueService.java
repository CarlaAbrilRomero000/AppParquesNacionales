package org.example.parques.service;

import org.example.parques.exception.ReglaNegocioException;
import org.example.parques.model.TipoParque;
import org.example.parques.repository.TipoParqueRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lógica de negocio del ABM de Tipos de Parque.
 *
 * <p>Su principal responsabilidad es invocar al repositorio y traducir las
 * excepciones de SQL Server (mensajes de los THROW de los procedimientos
 * almacenados) en una {@link ReglaNegocioException} con un texto limpio
 * para mostrar en la interfaz.</p>
 */
@Service
public class TipoParqueService {

    private final TipoParqueRepository repository;

    public TipoParqueService(TipoParqueRepository repository) {
        this.repository = repository;
    }

    public List<TipoParque> listar() {
        return repository.listar();
    }

    public TipoParque buscarPorId(int id) {
        try {
            return repository.buscarPorId(id);
        } catch (DataAccessException ex) {
            throw new ReglaNegocioException("No se encontró el tipo de parque solicitado.", ex);
        }
    }

    public void crear(String descripcion) {
        try {
            repository.insertar(descripcion);
        } catch (DataAccessException ex) {
            throw new ReglaNegocioException(mensajeDeBaseDatos(ex), ex);
        }
    }

    public void modificar(int id, String descripcion) {
        try {
            repository.modificar(id, descripcion);
        } catch (DataAccessException ex) {
            throw new ReglaNegocioException(mensajeDeBaseDatos(ex), ex);
        }
    }

    public void eliminar(int id) {
        try {
            repository.eliminar(id);
        } catch (DataAccessException ex) {
            throw new ReglaNegocioException(mensajeDeBaseDatos(ex), ex);
        }
    }

    /**
     * Extrae el mensaje más específico de la cadena de excepciones, que
     * normalmente corresponde al texto del THROW lanzado por el procedimiento
     * almacenado en SQL Server.
     */
    private String mensajeDeBaseDatos(DataAccessException ex) {
        Throwable causa = ex.getMostSpecificCause();
        String mensaje = causa != null ? causa.getMessage() : ex.getMessage();
        return (mensaje == null || mensaje.isBlank())
                ? "Ocurrió un error al procesar la operación en la base de datos."
                : mensaje;
    }
}
