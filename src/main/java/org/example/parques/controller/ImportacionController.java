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
 * Controlador de la pantalla de importación de organizaciones distinguidas.
 */
@Controller
@RequestMapping("/importacion")
public class ImportacionController {

    private final OrganizacionDistinguidaService organizacionService;

    public ImportacionController(OrganizacionDistinguidaService organizacionService) {
        this.organizacionService = organizacionService;
    }

    /** Muestra el formulario de importación junto al listado de organizaciones en base. */
    @GetMapping
    public String mostrar(Model model) {
        model.addAttribute("organizaciones", organizacionService.listar());
        return "importacion/index";
    }

    /**
     * Recibe el archivo CSV de organizaciones distinguidas y lo importa en la
     * base de datos a través del procedimiento almacenado
     * {@code importaciones.ImportarOrganizacionesDistinguidas}.
     */
    @PostMapping
    public String importar(@RequestParam("archivo") MultipartFile archivo,
                           RedirectAttributes flash) {
        ResultadoImportacionOrganizaciones resultado = organizacionService.importar(archivo);
        if (resultado.exito()) {
            flash.addFlashAttribute("exito", resultado.mensaje());
        } else {
            flash.addFlashAttribute("error", resultado.mensaje());
        }
        return "redirect:/importacion";
    }
}
