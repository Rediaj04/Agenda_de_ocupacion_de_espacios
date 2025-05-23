package com.puig.agenda.service;

import com.puig.agenda.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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

        // Añadir este método para exponer el estado como una cadena
        public String getStatusString() {
            return this.estado.toString();
        }

        // Añadir este método para exponer el estado como el enum del modelo
        public SlotAgenda.SlotStatus getModelStatus() {
            return this.estado.getModelStatus();
        }
    }

    private record ConcreteTimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
    }

    private record TimeRange(LocalTime start, LocalTime end) {
    }

    // Mapa para días de la semana por idioma
    private final Map<String, Map<Character, DayOfWeek>> languageDayMapping = initializeLanguageMappings();

    private Map<String, Map<Character, DayOfWeek>> initializeLanguageMappings() {
        Map<String, Map<Character, DayOfWeek>> mappings = new HashMap<>();

        // Español
        Map<Character, DayOfWeek> espMap = Map.of(
                'L', DayOfWeek.MONDAY,
                'M', DayOfWeek.TUESDAY,
                'X', DayOfWeek.WEDNESDAY,
                'J', DayOfWeek.THURSDAY,
                'V', DayOfWeek.FRIDAY,
                'S', DayOfWeek.SATURDAY,
                'G', DayOfWeek.SUNDAY);
        mappings.put("ESP", espMap);

        // Inglés
        mappings.put("ENG", Map.of(
                'M', DayOfWeek.MONDAY,
                'T', DayOfWeek.TUESDAY,
                'W', DayOfWeek.WEDNESDAY,
                'H', DayOfWeek.THURSDAY,
                'F', DayOfWeek.FRIDAY,
                'S', DayOfWeek.SATURDAY,
                'U', DayOfWeek.SUNDAY));

        // Catalán
        mappings.put("CAT", Map.of(
                'L', DayOfWeek.MONDAY,
                'M', DayOfWeek.TUESDAY,
                'X', DayOfWeek.WEDNESDAY,
                'J', DayOfWeek.THURSDAY,
                'V', DayOfWeek.FRIDAY,
                'S', DayOfWeek.SATURDAY,
                'D', DayOfWeek.SUNDAY));

        return mappings;
    }

    /**
     * Procesa las peticiones de agenda de acuerdo a la configuración
     */
    public AgendaProcessingResult processAgenda(Configuration config, List<Request> allRequests) {
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
    private void processSingleRequest(Request request, Configuration config,
            Map<String, Map<LocalDate, Map<LocalTime, SlotInfo>>> scheduleByRoom,
            List<Incidence> incidences, boolean isTancatRequest) {

        String roomName = request.getRoom().getName();
        scheduleByRoom.putIfAbsent(roomName, new HashMap<>());

        List<ConcreteTimeSlot> concreteTimeSlots = generateTimeSlots(request, config, incidences);

        for (ConcreteTimeSlot slot : concreteTimeSlots) {
            LocalDate currentDate = slot.date();

            // Verificar si el slot está dentro del mes/año configurado
            if (currentDate.getYear() != config.getYear() || currentDate.getMonthValue() != config.getMonth()) {
                continue;
            }

            // Asegurar que las estructuras internas para fecha y hora existan
            Map<LocalDate, Map<LocalTime, SlotInfo>> roomSchedule = scheduleByRoom.get(roomName);
            roomSchedule.putIfAbsent(currentDate, new HashMap<>());
            Map<LocalTime, SlotInfo> daySchedule = roomSchedule.get(currentDate);

            // Procesar cada hora del slot
            LocalTime slotHour = slot.startTime();
            while (slotHour.isBefore(slot.endTime())) {
                processTimeSlot(request, roomName, currentDate, slotHour, daySchedule, incidences, isTancatRequest);
                slotHour = slotHour.plusHours(1);
            }
        }
    }

    /**
     * Procesa un slot específico de tiempo y actualiza la agenda
     */
    private void processTimeSlot(Request request, String roomName, LocalDate date, LocalTime time,
            Map<LocalTime, SlotInfo> daySchedule, List<Incidence> incidences, boolean isTancat) {
        // Inicializar slot si es necesario
        daySchedule.putIfAbsent(time, new SlotInfo(SlotState.LIBRE));
        SlotInfo currentSlot = daySchedule.get(time);

        if (isTancat) {
            if (currentSlot.estado() == SlotState.OCUPADO) {
                // Reportar conflicto (Tancat sobre una actividad ya programada)
                createIncidence(request,
                        String.format("Conflicto '%s' sobrepuesto a actividad '%s' en %s el %s de %s a %s",
                                TANCAT, currentSlot.activityName(), roomName, date, time, time.plusHours(1)),
                        incidences);
            }
            daySchedule.put(time, new SlotInfo(SlotState.BLOQUEADO, TANCAT));
        } else {
            // Petición Regular
            if (currentSlot.estado() == SlotState.LIBRE) {
                daySchedule.put(time, new SlotInfo(SlotState.OCUPADO, request.getActivityName()));
            } else {
                // Reportar conflicto (actividad sobre slot ocupado o bloqueado)
                String conflictReason = (currentSlot.estado() == SlotState.BLOQUEADO)
                        ? "conflicto con horario bloqueado (Tancat)"
                        : String.format("conflicto con actividad '%s'", currentSlot.activityName());

                createIncidence(request,
                        String.format("%s: %s en %s el %s de %s a %s",
                                request.getActivityName(), conflictReason, roomName, date, time, time.plusHours(1)),
                        incidences);
            }
        }
    }

    /**
     * Genera slots de tiempo basados en la petición y configuración
     */
    private List<ConcreteTimeSlot> generateTimeSlots(Request request, Configuration config,
            List<Incidence> incidences) {
        List<ConcreteTimeSlot> slots = new ArrayList<>();
        Map<Character, DayOfWeek> currentLangMap = languageDayMapping.get(config.getEntryLanguage().toUpperCase());

        if (currentLangMap == null) {
            logger.error("No se encontró mapeo de idioma para: {}", config.getEntryLanguage());
            createIncidence(request,
                    String.format("Error de configuración: Idioma de entrada '%s' no soportado para interpretar días.",
                            config.getEntryLanguage()),
                    incidences);
            return slots;
        }

        // Parsear máscara de horarios (ej. "08-10_19-21")
        String[] hourRangesStr = request.getMaskSchedules().split("_");
        if (hourRangesStr.length > 5) {
            createIncidence(request,
                    String.format(
                            "Formato de máscara de horas inválido: Máximo 5 franjas horarias permitidas. Petición: %s",
                            request.getActivityName()),
                    incidences);
            return slots;
        }

        List<TimeRange> timeRanges = parseTimeRanges(request, hourRangesStr, incidences);
        if (timeRanges.isEmpty() && hourRangesStr.length > 0) {
            return slots;
        }

        // Convertir las fechas de Date a LocalDate
        LocalDate startLocalDate = dateToLocalDate(request.getStartDate());
        LocalDate endLocalDate = dateToLocalDate(request.getEndDate());

        // Iterar por cada día entre fechas de inicio y fin
        LocalDate currentDate = startLocalDate;
        while (!currentDate.isAfter(endLocalDate)) {
            // Solo procesar días dentro del mes/año configurado
            if (currentDate.getYear() == config.getYear() && currentDate.getMonthValue() == config.getMonth()) {
                boolean dayMatches = matchesDay(request.getMaskDays(), currentDate.getDayOfWeek(), currentLangMap);

                if (dayMatches || request.getMaskDays().isEmpty()) {
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
            if (times.length == 2) {
                try {
                    // Manejo especial para la hora 24
                    int startHour = Integer.parseInt(times[0]);
                    int endHour = Integer.parseInt(times[1]);

                    // Crear el tiempo de inicio
                    LocalTime start = LocalTime.of(startHour, 0);

                    // Crear el tiempo de fin, manejando el caso especial de 24:00
                    LocalTime end;
                    if (endHour == 24) {
                        // Considerar 24:00 como el final del día (a efectos prácticos, 00:00 del día
                        // siguiente)
                        end = LocalTime.of(0, 0);
                    } else {
                        end = LocalTime.of(endHour, 0);
                    }

                    // Manejar caso especial de medianoche
                    if ((end.equals(LocalTime.MIDNIGHT) && !start.equals(LocalTime.MIDNIGHT))
                            || endHour == 24) {
                        // Usar el final del día para representar 24:00/00:00
                        end = LocalTime.MAX.truncatedTo(ChronoUnit.HOURS).plusHours(1);
                    }

                    // Validar que el rango sea válido
                    if (start.isAfter(end) && !end.equals(LocalTime.MIDNIGHT) && endHour != 24) {
                        createIncidence(request,
                                String.format(
                                        "Rango horario inválido: la hora de inicio es posterior a la hora de fin en '%s'. Petición: %s",
                                        hrStr, request.getActivityName()),
                                incidences);
                        continue;
                    }
                    timeRanges.add(new TimeRange(start, end));
                } catch (NumberFormatException e) {
                    createIncidence(request,
                            String.format("Formato de hora inválido en '%s'. Petición: %s",
                                    hrStr, request.getActivityName()),
                            incidences);
                }
            } else {
                createIncidence(request,
                        String.format("Formato de rango horario inválido: '%s'. Petición: %s",
                                hrStr, request.getActivityName()),
                        incidences);
            }
        }

        return timeRanges;
    }

    /**
     * Crea una incidencia y la añade a la lista
     */
    private void createIncidence(Request request, String reason, List<Incidence> incidences) {
        Incidence incidence = new Incidence();
        incidence.setRequestRejected(request);
        incidence.setReason(reason);
        incidences.add(incidence);
    }

    /**
     * Crea slots concretos a partir de un rango de tiempo
     */
    private List<ConcreteTimeSlot> createSlotsFromTimeRange(LocalDate date, TimeRange timeRange) {
        List<ConcreteTimeSlot> slots = new ArrayList<>();
        LocalTime currentHour = timeRange.start();
        while (currentHour.isBefore(timeRange.end())) {
            slots.add(new ConcreteTimeSlot(date, currentHour, currentHour.plusHours(1)));
            currentHour = currentHour.plusHours(1);
        }
        return slots;
    }

    /**
     * Verifica si un día de la semana coincide con la máscara de días
     */
    private boolean matchesDay(String maskDays, DayOfWeek dayOfWeek, Map<Character, DayOfWeek> langMap) {
        if (maskDays == null || maskDays.isEmpty()) {
            return true;
        }

        for (char dayChar : maskDays.toCharArray()) {
            if (langMap.getOrDefault(Character.toUpperCase(dayChar), null) == dayOfWeek) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convierte un java.util.Date a java.time.LocalDate
     */
    private LocalDate dateToLocalDate(java.util.Date utilDate) { // utilDate es el campo de tu clase Request
        if (utilDate == null) {
            logger.debug("Intentando convertir una fecha nula a LocalDate, devolviendo null.");
            return null;
        }
        // El método getTime() devuelve los milisegundos desde la época.
        // Esto es seguro para java.util.Date y sus subclases como java.sql.Date.
        return java.time.Instant.ofEpochMilli(utilDate.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}