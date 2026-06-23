package org.example.parques.controller;

import org.example.parques.exception.ReglaNegocioException;
import org.example.parques.model.Parque;
import org.example.parques.service.ParqueService;
import org.example.parques.service.TipoParqueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador del ABM de Parques.
 *
 * <p>Sigue el flujo Controller → Service → Repository → Stored Procedures.
 * Los mensajes de éxito y de error se envían a la vista mediante
 * {@link RedirectAttributes} (patrón Post/Redirect/Get) para mostrarlos como
 * alertas de Bootstrap. El formulario necesita además la lista de tipos de
 * parque ({@link TipoParqueService}) para poblar el desplegable.</p>
 */
@Controller
@RequestMapping("/parques")
public class ParqueController {

    private final ParqueService service;
    private final TipoParqueService tipoParqueService;

    public ParqueController(ParqueService service, TipoParqueService tipoParqueService) {
        this.service = service;
        this.tipoParqueService = tipoParqueService;
    }

    /** Listado en tabla de todos los parques. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("parques", service.listar());
        return "parque/lista";
    }

    /** Muestra el formulario vacío para dar de alta un nuevo parque. */
    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("parque", new Parque());
        model.addAttribute("tipos", tipoParqueService.listar());
        model.addAttribute("modoEdicion", false);
        return "parque/formulario";
    }

    /** Muestra el formulario precargado para editar un parque existente. */
    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable int id, Model model,
                                   RedirectAttributes flash) {
        try {
            model.addAttribute("parque", service.buscarPorId(id));
            model.addAttribute("tipos", tipoParqueService.listar());
            model.addAttribute("modoEdicion", true);
            return "parque/formulario";
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/parques";
        }
    }

    /** Procesa el alta o la modificación según venga o no el id. */
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Parque parque,
                          RedirectAttributes flash) {
        try {
            if (parque.getIdParque() == null) {
                service.crear(parque);
                flash.addFlashAttribute("exito", "Parque creado correctamente.");
            } else {
                service.modificar(parque);
                flash.addFlashAttribute("exito", "Parque modificado correctamente.");
            }
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/parques";
    }

    /** Baja de un parque. */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable int id, RedirectAttributes flash) {
        try {
            service.eliminar(id);
            flash.addFlashAttribute("exito", "Parque eliminado correctamente.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/parques";
    }
}
