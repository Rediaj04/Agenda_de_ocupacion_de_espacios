package com.puig.agenda.Controller;

import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.puig.agenda.model.Configuration;
import com.puig.agenda.model.Incidence;
import com.puig.agenda.model.Request;
import com.puig.agenda.service.AgendaService;
import com.puig.agenda.service.AgendaService.AgendaProcessingResult;
import com.puig.agenda.service.AgendaService.SlotInfo;
import com.puig.agenda.service.DataService;
import com.puig.agenda.viewmodel.*;

@Controller
public class AgendaController {

    @Autowired
    private AgendaService agendaService;

    @Autowired
    private DataService dataService;

    @GetMapping("/")
    public String mostrarFormulario() {
        return "upload";
    }

    @PostMapping("/procesar")
    public String procesarArchivos(@RequestParam("configFile") MultipartFile configFile,
            @RequestParam("peticionesFile") MultipartFile peticionesFile,
            Model model) {
        try {
            // Parsear archivos de configuración y peticiones
            Configuration config = dataService.parseConfigFile(configFile);
            List<Request> requests = dataService.parseRequestsFile(peticionesFile);

            // Procesar la agenda
            AgendaProcessingResult result = agendaService.processAgenda(config, requests);

            // Convertir el resultado a ViewModel para la vista
            AgendaViewModel viewModel = convertirAViewModel(config, result);

            // Añadir al modelo para la vista
            model.addAttribute("agenda", viewModel);
            model.addAttribute("salas", viewModel.getRooms());

            // Generar días de la semana según idioma de salida
            List<String> diasSemana = generarDiasSemana(config.getExitLanguage());
            model.addAttribute("diasSemana", diasSemana);

            // Generar horas del día
            List<String> horas = generarHoras();
            model.addAttribute("horas", horas);

            return "agenda";
        } catch (Exception e) {
            // Imprimir el stack trace completo a la consola de errores estándar
            System.err.println("Se ha producido una excepción durante el procesamiento de archivos:");
            e.printStackTrace(System.err); // <<--- AÑADE O CAMBIA A ESTA LÍNEA

            // También puedes mantener tu logger si lo tienes configurado
            // logger.error("Error crítico en procesarArchivos", e);

            model.addAttribute("error", "Error al procesar los archivos: " +
                    (e.getMessage() == null ? "Excepción de tipo " + e.getClass().getName() + " (sin mensaje detallado)"
                            : e.getMessage()));
            return "error";
        }
    }

    /**
     * Convierte el resultado del proceso de la agenda en un ViewModel para la vista
     */
    private AgendaViewModel convertirAViewModel(Configuration config, AgendaProcessingResult result) {
        AgendaViewModel viewModel = new AgendaViewModel();

        // Establecer año y mes
        viewModel.setYear(config.getYear());
        viewModel.setNameMonth(obtenerNombreMes(config.getMonth(), config.getExitLanguage()));

        // Convertir salas
        List<RoomViewModel> roomViewModels = new ArrayList<>();

        // Obtener todos los slots e incidencias
        Map<String, Map<LocalDate, Map<LocalTime, SlotInfo>>> schedule = result.finalSchedule();
        List<Incidence> incidences = result.incidences();

        // Procesar cada sala
        for (String roomName : schedule.keySet()) {
            RoomViewModel roomVM = new RoomViewModel();
            roomVM.setRoomName(roomName);

            // Organizar por semanas
            Map<LocalDate, Map<LocalTime, SlotInfo>> roomSchedule = schedule.get(roomName);
            List<WeekViewModel> weeks = organizarPorSemanas(roomSchedule, config.getMonth(), config.getYear());
            roomVM.setWeekMonth(weeks);

            roomViewModels.add(roomVM);
        }

        viewModel.setRooms(roomViewModels);

        // Convertir incidencias
        List<IncidenceViewModel> incidenceViewModels = new ArrayList<>();
        for (Incidence incidence : incidences) {
            IncidenceViewModel incVM = new IncidenceViewModel();
            incVM.setDescription(incidence.getReason());
            incidenceViewModels.add(incVM);
        }

        viewModel.setIncidences(incidenceViewModels);

        return viewModel;
    }

    /**
     * Organiza los slots por semanas para una sala
     */
    private List<WeekViewModel> organizarPorSemanas(Map<LocalDate, Map<LocalTime, SlotInfo>> roomSchedule,
            int month, int year) {
        List<WeekViewModel> weeks = new ArrayList<>();

        // Obtener todas las fechas para el mes y año configurados
        List<LocalDate> fechasDelMes = new ArrayList<>();
        for (LocalDate date : roomSchedule.keySet()) {
            if (date.getMonthValue() == month && date.getYear() == year) {
                fechasDelMes.add(date);
            }
        }

        // Ordenar fechas
        Collections.sort(fechasDelMes);

        // Agrupar por semanas
        Map<Integer, List<DayViewModel>> diasPorSemana = new HashMap<>();

        for (LocalDate date : fechasDelMes) {
            // Calcular número de semana dentro del mes
            int weekOfMonth = (date.getDayOfMonth() - 1) / 7 + 1;

            // Crear objeto día
            DayViewModel day = new DayViewModel();
            day.setNumeroDelMes(date.getDayOfMonth());
            day.setDayOfWeek(date.getDayOfWeek().getValue()); // Guardar el día de la semana
            day.setNombreDiaSemana(date.getDayOfWeek().toString()); // Opcional: nombre del día

            // Añadir slots del día
            List<AgendaCellViewModel> cells = new ArrayList<>();
            Map<LocalTime, SlotInfo> daySlots = roomSchedule.get(date);

            if (daySlots != null) {
                for (LocalTime time : daySlots.keySet()) {
                    SlotInfo slotInfo = daySlots.get(time);

                    AgendaCellViewModel cell = new AgendaCellViewModel();
                    cell.setHour(time.toString());
                    cell.setStatus(slotInfo.getStatusString());
                    cell.setActivity(slotInfo.activityName());

                    cells.add(cell);
                }
            }

            // Ordenar celdas por hora
            cells.sort(Comparator.comparing(AgendaCellViewModel::getHour));
            day.setCells(cells); // Usar el nuevo método setCells

            // Añadir a la semana correspondiente
            if (!diasPorSemana.containsKey(weekOfMonth)) {
                diasPorSemana.put(weekOfMonth, new ArrayList<>());
            }
            diasPorSemana.get(weekOfMonth).add(day);
        }

        // Convertir mapa a lista de semanas
        for (int weekNumber : diasPorSemana.keySet()) {
            WeekViewModel week = new WeekViewModel();
            week.setWeekNumber(weekNumber); // Añadir esta propiedad a WeekViewModel

            // Ordenar días por día de la semana
            List<DayViewModel> days = diasPorSemana.get(weekNumber);
            days.sort(Comparator.comparing(DayViewModel::getDayOfWeek));

            week.setDays(days);
            weeks.add(week);
        }

        // Ordenar semanas por número
        weeks.sort(Comparator.comparing(WeekViewModel::getWeekNumber)); // Añadir este método a WeekViewModel

        return weeks;
    }

    /**
     * Genera la lista de días de la semana según el idioma de salida
     */
    private List<String> generarDiasSemana(String language) {
        if ("ESP".equalsIgnoreCase(language)) {
            return Arrays.asList("L", "M", "X", "J", "V", "S", "D");
        } else if ("ENG".equalsIgnoreCase(language)) {
            return Arrays.asList("M", "T", "W", "T", "F", "S", "S");
        } else if ("CAT".equalsIgnoreCase(language)) {
            return Arrays.asList("DL", "DT", "DC", "DJ", "DV", "DS", "DG");
        } else {
            // Idioma por defecto
            return Arrays.asList("L", "M", "X", "J", "V", "S", "D");
        }
    }

    /**
     * Genera la lista de horas para mostrar en la agenda
     */
    private List<String> generarHoras() {
        List<String> horas = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:00");

        for (int i = 8; i <= 20; i++) {
            LocalTime hora = LocalTime.of(i, 0);
            horas.add(hora.format(formatter));
        }

        return horas;
    }

    /**
     * Devuelve el nombre del mes según el idioma proporcionado.
     */
    private String obtenerNombreMes(int month, String language) {
        String[] mesesESP = { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
                "Octubre", "Noviembre", "Diciembre" };
        String[] mesesENG = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December" };
        String[] mesesCAT = { "Gener", "Febrer", "Març", "Abril", "Maig", "Juny", "Juliol", "Agost", "Setembre",
                "Octubre", "Novembre", "Desembre" };
        if ("ENG".equalsIgnoreCase(language)) {
            return mesesENG[month - 1];
        } else if ("CAT".equalsIgnoreCase(language)) {
            return mesesCAT[month - 1];
        } else {
            return mesesESP[month - 1]; // Español por defecto
        }
    }

}
