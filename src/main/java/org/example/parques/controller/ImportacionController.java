package org.example.parques.controller;

import org.example.parques.service.ImportacionService;
import org.example.parques.service.OrganizacionDistinguidaService;
import org.example.parques.service.ResultadoImportacion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de la pantalla de importación de archivos CSV.
 */
@Controller
@RequestMapping("/importacion")
public class ImportacionController {

    private final ImportacionService service;
    private final OrganizacionDistinguidaService organizacionService;

    public ImportacionController(ImportacionService service,
                                 OrganizacionDistinguidaService organizacionService) {
        this.service = service;
        this.organizacionService = organizacionService;
    }

    /** Muestra el formulario de importación junto al listado de organizaciones en base. */
    @GetMapping
    public String mostrar(Model model) {
        model.addAttribute("ultimoXml", service.getUltimoXmlGenerado());
        model.addAttribute("organizaciones", organizacionService.listar());
        return "importacion/index";
    }

    /** Recibe el archivo CSV, ejecuta la importación y reporta el resultado. */
    @PostMapping
    public String importar(@RequestParam("archivo") MultipartFile archivo,
                           RedirectAttributes flash) {
        ResultadoImportacion resultado = service.importar(archivo);
        if (resultado.exito()) {
            flash.addFlashAttribute("exito", resultado.mensaje());
            flash.addFlashAttribute("xmlGenerado", resultado.nombreArchivoXml());
        } else {
            flash.addFlashAttribute("error", resultado.mensaje());
        }
        return "redirect:/importacion";
    }
}
