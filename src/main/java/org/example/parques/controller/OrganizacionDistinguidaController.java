package org.example.parques.controller;

import org.example.parques.service.OrganizacionDistinguidaService;
import org.example.parques.service.ResultadoImportacionOrganizaciones;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de Organizaciones Distinguidas.
 *
 * <p>A diferencia del ABM, esta pantalla solo permite consultar la lista de
 * organizaciones distinguidas e importar nuevas desde un archivo CSV. Tras la
 * importación se redirige al listado (patrón Post/Redirect/Get), por lo que la
 * tabla se vuelve a consultar y refleja los registros recién cargados.</p>
 */
@Controller
@RequestMapping("/organizaciones-distinguidas")
public class OrganizacionDistinguidaController {

    private final OrganizacionDistinguidaService service;

    public OrganizacionDistinguidaController(OrganizacionDistinguidaService service) {
        this.service = service;
    }

    /** Lista las organizaciones distinguidas existentes y muestra el formulario de carga. */
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("organizaciones", service.listar());
        return "organizacion/lista";
    }

    /** Recibe el CSV, ejecuta la importación y vuelve al listado actualizado. */
    @PostMapping("/importar")
    public String importar(@RequestParam("archivo") MultipartFile archivo,
                           RedirectAttributes flash) {
        ResultadoImportacionOrganizaciones resultado = service.importar(archivo);
        if (resultado.exito()) {
            flash.addFlashAttribute("exito", resultado.mensaje());
        } else {
            flash.addFlashAttribute("error", resultado.mensaje());
        }
        return "redirect:/organizaciones-distinguidas";
    }
}
