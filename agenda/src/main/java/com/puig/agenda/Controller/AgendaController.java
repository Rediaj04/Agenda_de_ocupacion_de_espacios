package com.puig.agenda.Controller;

import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.puig.agenda.model.AgendaConfiguration;
import com.puig.agenda.model.Incidence;
import com.puig.agenda.model.Request;
import com.puig.agenda.service.AgendaProcessor;
import com.puig.agenda.service.AgendaProcessor.AgendaProcessingResult;
import com.puig.agenda.service.AgendaProcessor.SlotInfo;
import com.puig.agenda.service.DataLoader;
import com.puig.agenda.viewmodel.*;

@Controller
public class AgendaController {

    private static final Logger logger = LoggerFactory.getLogger(AgendaController.class);

    // Mapeo de idiomas a nombres de meses y días
    private static final Map<String, String[]> MONTH_NAMES = Map.of(
            "ESP", new String[] { "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto",
                    "Septiembre", "Octubre", "Noviembre", "Diciembre" },
            "ENG", new String[] { "January", "February", "March", "April", "May", "June", "July", "August",
                    "September", "October", "November", "December" },
            "CAT", new String[] { "Gener", "Febrer", "Març", "Abril", "Maig", "Juny", "Juliol", "Agost",
                    "Setembre", "Octubre", "Novembre", "Desembre" });

    private static final Map<String, List<String>> WEEK_DAYS = Map.of(
            "ESP", Arrays.asList("L", "M", "X", "J", "V", "S", "D"),
            "ENG", Arrays.asList("M", "T", "W", "R", "F", "S", "U"),
            "CAT", Arrays.asList("DL", "DT", "DC", "DJ", "DV", "DS", "DG"));

    @Autowired
    private AgendaProcessor agendaService;

    @Autowired
    private DataLoader dataService;

    @GetMapping("/")
    public String mostrarFormulario() {
        return "upload";
    }

    @PostMapping("/procesar")
    public String procesarArchivos(@RequestParam("configFile") MultipartFile configFile,
            @RequestParam("peticionesFile") MultipartFile peticionesFile,
            Model model) {
        try {
            // Procesar archivos y generar agenda
            AgendaConfiguration config = dataService.parseConfigFile(configFile);
            List<Request> requests = dataService.parseRequestsFile(peticionesFile);
            AgendaProcessingResult result = agendaService.processAgenda(config, requests);

            // Convertir resultados al formato de vista
            AgendaViewModel viewModel = convertirAViewModel(config, result);

            // Preparar el modelo para la vista
            model.addAttribute("agenda", viewModel);
            model.addAttribute("salas", viewModel.getRooms());
            model.addAttribute("diasSemana", generarDiasSemana(config.getExitLanguage()));
            model.addAttribute("horas", generarHoras());

            return "agenda";
        } catch (Exception e) {
            logger.error("Error al procesar los archivos", e);

            String mensaje = e.getMessage() != null ? e.getMessage() : "Excepción de tipo " + e.getClass().getName();

            model.addAttribute("error", "Error al procesar los archivos: " + mensaje);
            return "error";
        }
    }

    /**
     * Convierte el resultado del proceso de la agenda en un ViewModel para la vista
     */
    private AgendaViewModel convertirAViewModel(AgendaConfiguration config, AgendaProcessingResult result) {
        AgendaViewModel viewModel = new AgendaViewModel();

        // Configurar datos básicos
        viewModel.setYear(config.getYear());
        viewModel.setNameMonth(obtenerNombreMes(config.getMonth(), config.getExitLanguage()));

        // Procesar salas
        List<RoomViewModel> roomViewModels = new ArrayList<>();
        for (String roomName : result.finalSchedule().keySet()) {
            RoomViewModel roomVM = new RoomViewModel();
            roomVM.setRoomName(roomName);

            // Organizar por semanas
            Map<LocalDate, Map<LocalTime, SlotInfo>> roomSchedule = result.finalSchedule().get(roomName);
            roomVM.setWeekMonth(organizarPorSemanas(roomSchedule, config.getMonth(), config.getYear()));

            roomViewModels.add(roomVM);
        }
        // Ordenar las salas por nombre
        roomViewModels.sort(Comparator.comparing(RoomViewModel::getRoomName));
        viewModel.setRooms(roomViewModels);

        // Convertir incidencias
        List<IncidenceViewModel> incidenceViewModels = new ArrayList<>();
        for (Incidence incidence : result.incidences()) {
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
        // Crear un mapa para agrupar por semana
        Map<Integer, List<DayViewModel>> diasPorSemana = new HashMap<>();

        // Generar todas las fechas del mes
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate inicioMes = yearMonth.atDay(1);
        LocalDate finMes = yearMonth.atEndOfMonth();

        // Calcular el primer lunes para referencia de las semanas
        LocalDate primerLunes = inicioMes;
        if (inicioMes.getDayOfWeek().getValue() > 1) {
            primerLunes = inicioMes.minusDays(inicioMes.getDayOfWeek().getValue() - 1);
        }

        // Procesar cada día del mes
        for (LocalDate fecha = inicioMes; !fecha.isAfter(finMes); fecha = fecha.plusDays(1)) {
            // Añadir fecha al mapa si no existe
            roomSchedule.putIfAbsent(fecha, new HashMap<>());

            // Calcular semana
            int diasDesdePrimerLunes = (int) (primerLunes.until(fecha, java.time.temporal.ChronoUnit.DAYS));
            int semana = (diasDesdePrimerLunes / 7) + 1;

            // Crear el objeto de día con sus slots
            DayViewModel dia = crearDiaViewModel(fecha, roomSchedule.get(fecha));

            // Añadir a la semana correspondiente
            diasPorSemana.computeIfAbsent(semana, k -> new ArrayList<>()).add(dia);
        }

        // Convertir el mapa a una lista ordenada de semanas
        return diasPorSemana.entrySet().stream()
                .map(entry -> {
                    WeekViewModel semana = new WeekViewModel();
                    semana.setWeekNumber(entry.getKey());

                    // Ordenar días por día de la semana
                    List<DayViewModel> dias = entry.getValue();
                    dias.sort(Comparator.comparing(DayViewModel::getDayOfWeek));
                    semana.setDays(dias);

                    return semana;
                })
                .sorted(Comparator.comparing(WeekViewModel::getWeekNumber))
                .toList();
    }

    /**
     * Crea el objeto ViewModel para un día específico
     */
    private DayViewModel crearDiaViewModel(LocalDate fecha, Map<LocalTime, SlotInfo> daySlots) {
        DayViewModel dia = new DayViewModel();
        dia.setNumeroDelMes(fecha.getDayOfMonth());
        dia.setDayOfWeek(fecha.getDayOfWeek().getValue());
        dia.setNombreDiaSemana(fecha.getDayOfWeek().toString());

        // Procesar celdas de horas
        List<AgendaCellViewModel> celdas = daySlots.entrySet().stream()
                .map(entry -> {
                    LocalTime hora = entry.getKey();
                    SlotInfo infoSlot = entry.getValue();

                    AgendaCellViewModel celda = new AgendaCellViewModel();
                    celda.setHour(hora.toString());
                    celda.setStatus(infoSlot.getStatusString().toLowerCase());

                    // Manejar caso especial de "Tancat"
                    boolean esTancat = "BLOQUEADO".equalsIgnoreCase(infoSlot.getStatusString()) &&
                            "Tancat".equals(infoSlot.activityName());

                    celda.setActivity(esTancat ? null : infoSlot.activityName());

                    return celda;
                })
                .sorted(Comparator.comparing(AgendaCellViewModel::getHour))
                .toList();

        dia.setCells(celdas);
        return dia;
    }

    /**
     * Genera la lista de días de la semana según el idioma de salida
     */
    private List<String> generarDiasSemana(String language) {
        return WEEK_DAYS.getOrDefault(language.toUpperCase(), WEEK_DAYS.get("ESP"));
    }

    /**
     * Genera la lista de horas para mostrar en la agenda
     */
    private List<String> generarHoras() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:00");

        return IntStream.rangeClosed(0, 23)
                .mapToObj(i -> LocalTime.of(i, 0).format(formatter))
                .toList();
    }

    /**
     * Devuelve el nombre del mes según el idioma proporcionado
     */
    private String obtenerNombreMes(int month, String language) {
        String[] meses = MONTH_NAMES.getOrDefault(language.toUpperCase(), MONTH_NAMES.get("ESP"));
        return meses[month - 1];
    }
}