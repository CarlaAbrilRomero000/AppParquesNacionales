package org.example.parques.controller;

import org.example.parques.exception.ReglaNegocioException;
import org.example.parques.model.TipoParque;
import org.example.parques.service.TipoParqueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador del ABM de Tipos de Parque.
 *
 * <p>Sigue el flujo Controller → Service → Repository → Stored Procedures.
 * Los mensajes de éxito y de error se envían a la vista mediante
 * {@link RedirectAttributes} (patrón Post/Redirect/Get) para mostrarlos como
 * alertas de Bootstrap.</p>
 */
@Controller
@RequestMapping("/tipos-parque")
public class TipoParqueController {

    private final TipoParqueService service;

    public TipoParqueController(TipoParqueService service) {
        this.service = service;
    }

    /** Listado en tabla de todos los tipos de parque. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("tipos", service.listar());
        return "tipoparque/lista";
    }

    /** Muestra el formulario vacío para dar de alta un nuevo tipo. */
    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("tipoParque", new TipoParque());
        model.addAttribute("modoEdicion", false);
        return "tipoparque/formulario";
    }

    /** Muestra el formulario precargado para editar un tipo existente. */
    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable int id, Model model,
                                   RedirectAttributes flash) {
        try {
            model.addAttribute("tipoParque", service.buscarPorId(id));
            model.addAttribute("modoEdicion", true);
            return "tipoparque/formulario";
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/tipos-parque";
        }
    }

    /** Procesa el alta o la modificación según venga o no el id. */
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute TipoParque tipoParque,
                          RedirectAttributes flash) {
        try {
            if (tipoParque.getIdTipoParque() == null) {
                service.crear(tipoParque.getDescripcion());
                flash.addFlashAttribute("exito", "Tipo de parque creado correctamente.");
            } else {
                service.modificar(tipoParque.getIdTipoParque(), tipoParque.getDescripcion());
                flash.addFlashAttribute("exito", "Tipo de parque modificado correctamente.");
            }
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/tipos-parque";
    }

    /** Baja de un tipo de parque. */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id, RedirectAttributes flash) {
        try {
            service.eliminar(id);
            flash.addFlashAttribute("exito", "Tipo de parque eliminado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/tipos-parque";
    }
}
