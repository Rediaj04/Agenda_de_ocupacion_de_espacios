package com.puig.agenda.Controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AgendaController {

    @GetMapping("/")
    public String mostrarFormulario() {
        return "upload";
    };

    @PostMapping("/procesar")
    public String procesarArchivos(@RequestParam("configFile") MultipartFile configFile,
            @RequestParam("peticionesFile") MultipartFile peticionesFile,
            Model model) {
        // Crear salas
        Sala sala1 = new Sala("Sala 1", "Sala de Conferencias 1");
        Sala sala2 = new Sala("Sala 2", "Sala de Conferencias 2");

        // Asignar incidencias a Sala1
        sala1.agregarIncidencia(
                new Incidencia("Conflicto", "Sala 1 tiene un conflicto de horario el Lunes a las 09:00"));

        // Agregar salas al modelo
        model.addAttribute("salas", List.of(sala1, sala2));
        model.addAttribute("diasSemana", List.of("L", "M", "X", "J", "V", "S", "D"));
        model.addAttribute("horas", List.of("08:00", "09:00", "10:00", "11:00", "12:00"));

        return "agenda";
    };

    // Clase interna para la sala
    private static class Sala {
        private String id;
        private String nombre;
        private List<Incidencia> incidencias;

        public Sala(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
            this.incidencias = new ArrayList<>();
        }

        public String getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getEstado(String dia, String hora) {
            return "disponible";
        }

        public String getActividad(String dia, String hora) {
            return "";
        }

        public List<Incidencia> getIncidencias() {
            return incidencias;
        }

        public void agregarIncidencia(Incidencia incidencia) {
            this.incidencias.add(incidencia);
        }
    }

    // Clase interna para la incidencia
    private static class Incidencia {
        private String tipo;
        private String descripcion;

        public Incidencia(String tipo, String descripcion) {
            this.tipo = tipo;
            this.descripcion = descripcion;
        }

        public String getTipo() {
            return tipo;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
