package com.puig.agenda.service;

import com.puig.agenda.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Servicio para procesar agendas de ocupación de espacios
 */
@Service
public class AgendaService {

    private static final Logger logger = LoggerFactory.getLogger(AgendaService.class);
    private static final String TANCAT = "Tancat";

    // Enumeración para manejar estados de slots
    private enum SlotState {
        LIBRE(SlotAgenda.SlotStatus.FREE),
        OCUPADO(SlotAgenda.SlotStatus.BOOKED),
        BLOQUEADO(SlotAgenda.SlotStatus.BLOCKED);

        private final SlotAgenda.SlotStatus modelStatus;

        SlotState(SlotAgenda.SlotStatus modelStatus) {
            this.modelStatus = modelStatus;
        }

        public SlotAgenda.SlotStatus getModelStatus() {
            return this.modelStatus;
        }
    }

    // Records para resultados y slots
    public record AgendaProcessingResult(Map<String, Map<LocalDate, Map<LocalTime, SlotInfo>>> finalSchedule,
            List<Incidence> incidences) {
    }

    public record SlotInfo(SlotState estado, String activityName) {
        public SlotInfo(SlotState estado) {
            this(estado, null);
        }

        public String getStatusString() {
            return this.estado.toString();
        }

        public SlotAgenda.SlotStatus getModelStatus() {
            return this.estado.getModelStatus();
        }
    }

    private record ConcreteTimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
    }

    private record TimeRange(LocalTime start, LocalTime end) {
    }

    // Formato unificado LMCJVSG para todos los idiomas
    private final Map<Character, DayOfWeek> dayMap = Map.of(
            'L', DayOfWeek.MONDAY,
            'M', DayOfWeek.TUESDAY,
            'C', DayOfWeek.WEDNESDAY,
            'J', DayOfWeek.THURSDAY,
            'V', DayOfWeek.FRIDAY,
            'S', DayOfWeek.SATURDAY,
            'G', DayOfWeek.SUNDAY);

    /**
     * Procesa las peticiones de agenda de acuerdo a la configuración
     */
    public AgendaProcessingResult processAgenda(AgendaConfiguration config, List<Request> allRequests) {
        logger.info("Iniciando procesamiento de agenda para {}/{}", config.getMonth(), config.getYear());

        Map<String, Map<LocalDate, Map<LocalTime, SlotInfo>>> scheduleByRoom = new HashMap<>();
        List<Incidence> incidences = new ArrayList<>();

        // Procesar primero peticiones "Tancat" (tienen prioridad)
        allRequests.stream()
                .filter(r -> TANCAT.equalsIgnoreCase(r.getActivityName()))
                .forEach(request -> processSingleRequest(request, config, scheduleByRoom, incidences, true));

        // Luego procesar peticiones regulares
        allRequests.stream()
                .filter(r -> !TANCAT.equalsIgnoreCase(r.getActivityName()))
                .forEach(request -> processSingleRequest(request, config, scheduleByRoom, incidences, false));

        logger.info("Procesamiento completado. Incidencias: {}", incidences.size());
        return new AgendaProcessingResult(scheduleByRoom, incidences);
    }

    /**
     * Procesa una petición individual y actualiza la agenda
     */
    private void processSingleRequest(Request request, AgendaConfiguration config,
            Map<String, Map<LocalDate, Map<LocalTime, SlotInfo>>> scheduleByRoom,
            List<Incidence> incidences, boolean isTancatRequest) {

        String roomName = request.getRoom().getName();
        scheduleByRoom.putIfAbsent(roomName, new HashMap<>());

        List<ConcreteTimeSlot> slots = generateTimeSlots(request, config, incidences);

        for (ConcreteTimeSlot slot : slots) {
            LocalDate date = slot.date();

            // Solo procesar slots del mes y año configurados
            if (date.getYear() != config.getYear() || date.getMonthValue() != config.getMonth()) {
                continue;
            }

            // Preparar estructura de datos para la sala y fecha
            Map<LocalDate, Map<LocalTime, SlotInfo>> roomSchedule = scheduleByRoom.get(roomName);
            roomSchedule.putIfAbsent(date, new HashMap<>());
            Map<LocalTime, SlotInfo> daySchedule = roomSchedule.get(date);

            // Procesar cada hora del slot
            LocalTime time = slot.startTime();
            while (time.isBefore(slot.endTime())) {
                processHourSlot(request, roomName, date, time, daySchedule, incidences, isTancatRequest);
                time = time.plusHours(1);
            }
        }
    }

    /**
     * Procesa un slot específico de tiempo y actualiza la agenda
     */
    private void processHourSlot(Request request, String roomName, LocalDate date, LocalTime time,
            Map<LocalTime, SlotInfo> daySchedule, List<Incidence> incidences, boolean isTancat) {

        // Inicializar slot si es necesario
        daySchedule.putIfAbsent(time, new SlotInfo(SlotState.LIBRE));
        SlotInfo currentSlot = daySchedule.get(time);

        if (isTancat) {
            if (currentSlot.estado() == SlotState.OCUPADO) {
                // Reportar conflicto (Tancat sobre actividad ya programada)
                String mensaje = String.format("Conflicto '%s' sobrepuesto a actividad '%s' en %s el %s de %s a %s",
                        TANCAT, currentSlot.activityName(), roomName, date, time, time.plusHours(1));
                createIncidence(request, mensaje, incidences);
            }
            daySchedule.put(time, new SlotInfo(SlotState.BLOQUEADO, TANCAT));
        } else {
            // Petición Regular
            if (currentSlot.estado() == SlotState.LIBRE) {
                daySchedule.put(time, new SlotInfo(SlotState.OCUPADO, request.getActivityName()));
            } else {
                // Conflicto (actividad sobre slot ocupado o bloqueado)
                String conflictReason = (currentSlot.estado() == SlotState.BLOQUEADO)
                        ? "conflicto con horario bloqueado (Tancat)"
                        : String.format("conflicto con actividad '%s'", currentSlot.activityName());

                String mensaje = String.format("%s: %s en %s el %s de %s a %s",
                        request.getActivityName(), conflictReason, roomName, date, time, time.plusHours(1));
                createIncidence(request, mensaje, incidences);
            }
        }
    }

    /**
     * Genera slots de tiempo basados en la petición y configuración
     */
    private List<ConcreteTimeSlot> generateTimeSlots(Request request, AgendaConfiguration config,
            List<Incidence> incidences) {

        List<ConcreteTimeSlot> slots = new ArrayList<>();

        // Parsear máscara de horarios (ej. "08-10_19-21")
        String[] hourRangesStr = request.getMaskSchedules().split("_");
        if (hourRangesStr.length > 5) {
            String mensaje = String.format(
                    "Formato de máscara de horas inválido: Máximo 5 franjas horarias permitidas. Petición: %s",
                    request.getActivityName());
            createIncidence(request, mensaje, incidences);
            return slots;
        }

        List<TimeRange> timeRanges = parseTimeRanges(request, hourRangesStr, incidences);
        if (timeRanges.isEmpty() && hourRangesStr.length > 0) {
            return slots;
        }

        // Convertir las fechas de Date a LocalDate
        LocalDate startDate = dateToLocalDate(request.getStartDate());
        LocalDate endDate = dateToLocalDate(request.getEndDate());
        if (startDate == null || endDate == null) {
            return slots;
        }

        // Iterar por cada día entre fechas de inicio y fin
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Solo procesar días dentro del mes/año configurado
            if (currentDate.getYear() == config.getYear() && currentDate.getMonthValue() == config.getMonth()) {
                if (matchesDay(request.getMaskDays(), currentDate.getDayOfWeek())) {
                    // Generar slots para cada rango horario en este día
                    for (TimeRange timeRange : timeRanges) {
                        slots.addAll(createSlotsFromTimeRange(currentDate, timeRange));
                    }
                }
            }
            currentDate = currentDate.plusDays(1);
        }
        return slots;
    }

    /**
     * Parsea los rangos de tiempo a partir de las máscaras de horarios
     */
    private List<TimeRange> parseTimeRanges(Request request, String[] hourRangesStr, List<Incidence> incidences) {
        List<TimeRange> timeRanges = new ArrayList<>();

        for (String hrStr : hourRangesStr) {
            String[] times = hrStr.split("-");
            if (times.length != 2) {
                createIncidence(request, "Formato de rango horario inválido: '" + hrStr + "'", incidences);
                continue;
            }

            try {
                int startHour = Integer.parseInt(times[0]);
                int endHour = Integer.parseInt(times[1]);

                // Validación básica
                if (startHour < 0 || startHour > 23 || endHour < 0 || endHour > 24) {
                    createIncidence(request, "Hora fuera de rango en '" + hrStr + "'. Horas válidas: 0-24", incidences);
                    continue;
                }

                // Crear el tiempo de inicio y fin
                LocalTime start = LocalTime.of(startHour, 0);
                LocalTime end = (endHour == 24) ? LocalTime.of(23, 59, 59) : LocalTime.of(endHour, 0);

                // Validar que el rango sea válido
                if (start.isAfter(end) && endHour != 24) {
                    createIncidence(request, "Rango horario inválido: inicio posterior a fin en '" + hrStr + "'",
                            incidences);
                    continue;
                }

                timeRanges.add(new TimeRange(start, end));

            } catch (NumberFormatException e) {
                createIncidence(request, "Formato de hora inválido en '" + hrStr + "'", incidences);
            }
        }

        return timeRanges;
    }

    /**
     * Crea slots concretos a partir de un rango de tiempo
     */
    private List<ConcreteTimeSlot> createSlotsFromTimeRange(LocalDate date, TimeRange timeRange) {
        List<ConcreteTimeSlot> slots = new ArrayList<>();
        LocalTime currentHour = timeRange.start();
        LocalTime endTime = timeRange.end();

        // Detectar si es un horario que termina a las 24:00
        boolean endsAt24 = endTime.equals(LocalTime.of(23, 59, 59));

        while (currentHour.getHour() < 24) {
            // Para horarios normales, terminamos en la hora final
            // Para horarios que terminan a las 24:00, incluimos hasta las 23:00 inclusive
            if (!endsAt24 && currentHour.equals(endTime)) {
                break;
            }

            slots.add(new ConcreteTimeSlot(date, currentHour, currentHour.plusHours(1)));
            currentHour = currentHour.plusHours(1);

            // Parar al llegar a la medianoche o si hemos procesado la hora 23 para slots
            // que terminan a las 24:00
            if (currentHour.getHour() == 0 || (endsAt24 && currentHour.getHour() > 23)) {
                break;
            }
        }

        return slots;
    }

    /**
     * Crea una incidencia y la añade a la lista sin duplicados
     */
    private void createIncidence(Request request, String reason, List<Incidence> incidences) {
        String activityName = request.getActivityName();
        String roomName = request.getRoom().getName();

        // Evitar duplicados por actividad y sala
        boolean exists = incidences.stream()
                .anyMatch(inc -> inc.getRequestRejected().getActivityName().equals(activityName) &&
                        inc.getRequestRejected().getRoom().getName().equals(roomName));

        if (!exists) {
            Incidence incidence = new Incidence();
            incidence.setRequestRejected(request);

            // Mensaje resumido con los datos esenciales
            String mensaje = String.format("Incidencia con la actividad '%s' en la sala '%s' del %s al %s",
                    activityName, roomName,
                    formatDate(request.getStartDate()),
                    formatDate(request.getEndDate()));

            incidence.setReason(reason != null ? reason : mensaje);
            incidences.add(incidence);
        }
    }

    /**
     * Verifica si un día de la semana coincide con la máscara de días
     */
    private boolean matchesDay(String maskDays, DayOfWeek dayOfWeek) {
        if (maskDays == null || maskDays.isEmpty()) {
            return true;
        }

        String maskUpperCase = maskDays.toUpperCase();

        // Comprobar cada carácter en la máscara
        for (char dayChar : maskUpperCase.toCharArray()) {
            DayOfWeek mappedDay = dayMap.get(dayChar);
            if (mappedDay == dayOfWeek) {
                return true;
            }
        }

        return false;
    }

    /**
     * Formatea un Date a String
     */
    private String formatDate(java.util.Date date) {
        if (date == null)
            return "";
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    /**
     * Convierte java.util.Date a java.time.LocalDate
     */
    private LocalDate dateToLocalDate(java.util.Date utilDate) {
        if (utilDate == null)
            return null;

        return java.time.Instant.ofEpochMilli(utilDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}