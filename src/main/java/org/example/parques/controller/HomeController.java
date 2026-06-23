package org.example.parques.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de las páginas estáticas: inicio e información del sistema.
 */
@Controller
public class HomeController {

    /**
     * Menú de opciones (tarjetas) de la aplicación.
     * Se mueve de "/" a "/menu" porque la raíz la sirve ahora la landing
     * estática (src/main/resources/static/index.html).
     */
    @GetMapping("/menu")
    public String inicio() {
        return "index";
    }

    /** Página "Información del sistema". */
    @GetMapping("/info")
    public String info() {
        return "info";
    }
}
