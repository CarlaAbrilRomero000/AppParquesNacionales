package org.example.parques.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de las páginas estáticas: inicio e información del sistema.
 */
@Controller
public class HomeController {

    /** Página principal con el menú de opciones (tarjetas). */
    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    /** Página "Información del sistema". */
    @GetMapping("/info")
    public String info() {
        return "info";
    }
}
