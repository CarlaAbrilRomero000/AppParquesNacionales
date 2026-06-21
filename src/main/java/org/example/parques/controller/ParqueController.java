package org.example.parques.controller;

import org.example.parques.repository.ParqueRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador de consulta del listado de parques (solo lectura).
 */
@Controller
@RequestMapping("/parques")
public class ParqueController {

    private final ParqueRepository repository;

    public ParqueController(ParqueRepository repository) {
        this.repository = repository;
    }

    /** Listado en tabla de todos los parques. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("parques", repository.listar());
        return "parque/lista";
    }
}
