package com.puig.agenda.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;

@Controller
public class AgendaController {

    @GetMapping("/")
    public String mostrarFormulario() {
        return "upload";
    }

    @PostMapping("/procesar")
    public String procesarArchivos(@RequestParam("configFile") MultipartFile configFile,
                                 @RequestParam("peticionesFile") MultipartFile peticionesFile,
                                 Model model) {
        // Por ahora, solo redirigimos a la vista de agenda con datos de ejemplo
        model.addAttribute("salas", java.util.Arrays.asList(
            new Sala("Sala1", "Sala de Conferencias 1"),
            new Sala("Sala2", "Sala de Conferencias 2")
        ));
        model.addAttribute("diasSemana", java.util.Arrays.asList("L", "M", "X", "J", "V", "S", "D"));
        model.addAttribute("horas", java.util.Arrays.asList("08:00", "09:00", "10:00", "11:00", "12:00"));
        model.addAttribute("incidencias", java.util.Arrays.asList(
            new Incidencia("Conflicto", "Sala1 tiene un conflicto de horario el Lunes a las 09:00")
        ));
        
        return "agenda";
    }

    // Clases internas temporales para la demostración
    private static class Sala {
        private String id;
        private String nombre;

        public Sala(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getEstado(String dia, String hora) { return "disponible"; }
        public String getActividad(String dia, String hora) { return ""; }
    }

    private static class Incidencia {
        private String tipo;
        private String descripcion;

        public Incidencia(String tipo, String descripcion) {
            this.tipo = tipo;
            this.descripcion = descripcion;
        }

        public String getTipo() { return tipo; }
        public String getDescripcion() { return descripcion; }
    }
} 