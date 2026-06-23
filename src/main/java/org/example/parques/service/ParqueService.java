package org.example.parques.service;

import org.example.parques.exception.ReglaNegocioException;
import org.example.parques.model.Parque;
import org.example.parques.repository.ParqueRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lógica de negocio del ABM de Parques.
 *
 * <p>Invoca al repositorio (que delega en los procedimientos almacenados de
 * SQL Server) y traduce las excepciones de base de datos —principalmente los
 * mensajes de los {@code THROW} de los procedimientos— en una
 * {@link ReglaNegocioException} con un texto limpio para mostrar en la
 * interfaz. Sigue el mismo patrón que {@link TipoParqueService}.</p>
 */
@Service
public class ParqueService {

    private final ParqueRepository repository;

    public ParqueService(ParqueRepository repository) {
        this.repository = repository;
    }

    public List<Parque> listar() {
        return repository.listar();
    }

    public Parque buscarPorId(int id) {
        try {
            return repository.buscarPorId(id);
        } catch (DataAccessException ex) {
            throw new ReglaNegocioException("No se encontró el parque solicitado.", ex);
        }
    }

    public void crear(Parque parque) {
        try {
            repository.insertar(parque.getCodigoOficial(), parque.getNombre(),
                    parque.getUbicacion(), parque.getSuperficie(), parque.getIdTipoParque());
        } catch (DataAccessException ex) {
            throw new ReglaNegocioException(mensajeDeBaseDatos(ex), ex);
        }
    }

    public void modificar(Parque parque) {
        try {
            repository.modificar(parque.getIdParque(), parque.getCodigoOficial(), parque.getNombre(),
                    parque.getUbicacion(), parque.getSuperficie(), parque.getIdTipoParque());
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
