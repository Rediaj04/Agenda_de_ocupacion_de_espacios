package com.puig.agenda.service;

import com.puig.agenda.model.AgendaConfiguration;
import com.puig.agenda.model.Request;
import com.puig.agenda.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataService {

    private static final Logger logger = LoggerFactory.getLogger(DataService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");    public AgendaConfiguration parseConfigFile(MultipartFile configFile) throws IOException {
        AgendaConfiguration config = new AgendaConfiguration();

        if (configFile == null || configFile.isEmpty()) {
            logger.error("El archivo de configuración está vacío o es nulo.");
            return config;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(configFile.getInputStream(), StandardCharsets.UTF_8))) {
            // Leer primera línea: año y mes
            String yearMonthLine = reader.readLine();
            if (yearMonthLine != null && !yearMonthLine.trim().isEmpty()) {
                String[] parts = yearMonthLine.trim().split("\\s+");
                if (parts.length >= 2) {
                    try {
                        config.setYear(Integer.parseInt(parts[0]));
                        config.setMonth(Integer.parseInt(parts[1]));
                    } catch (NumberFormatException e) {
                        logger.error("Error al parsear año o mes desde la línea: '{}'", yearMonthLine, e);
                    }
                } else {
                    logger.warn("La línea de año/mes no tiene el formato esperado: '{}'", yearMonthLine);
                }
            } else {
                logger.warn("La línea de año/mes está vacía o ausente en el archivo de configuración.");
            }

            // Leer segunda línea: idiomas
            String languageLine = reader.readLine();
            if (languageLine != null && !languageLine.trim().isEmpty()) {
                String[] parts = languageLine.trim().split("\\s+");
                if (parts.length >= 2) {
                    config.setEntryLanguage(parts[0]);
                    config.setExitLanguage(parts[1]);
                } else {
                    logger.warn("La línea de idiomas no tiene el formato esperado: '{}'", languageLine);
                }
            } else {
                logger.warn("La línea de idiomas está vacía o ausente en el archivo de configuración.");
            }
        }
        return config;
    }

    public List<Request> parseRequestsFile(MultipartFile requestsFile) throws IOException {
        List<Request> requests = new ArrayList<>();

        if (requestsFile == null || requestsFile.isEmpty()) {
            logger.warn("El archivo de peticiones está vacío o es nulo.");
            return requests;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(requestsFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; // Ignorar líneas vacías
                }

                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 6) { // NombreActividad Sala FechaInicio FechaFin DíasHoras Horarios
                    try {
                        Request request = new Request();
                        request.setActivityName(parts[0]);

                        // Crear y asignar la sala (Room) a la petición (Request)
                        Room room = new Room(); // Asumiendo que Room es com.puig.agenda.model.Room
                        room.setName(parts[1]);
                        request.setRoom(room);

                        // Parsear fechas usando java.time.LocalDate
                        // Convertir LocalDate a java.util.Date
                        LocalDate startLocalDate = LocalDate.parse(parts[2], DATE_FORMATTER);
                        LocalDate endLocalDate = LocalDate.parse(parts[3], DATE_FORMATTER);
                        request.setStartDate(java.sql.Date.valueOf(startLocalDate));
                        request.setEndDate(java.sql.Date.valueOf(endLocalDate));

                        // Asignar máscaras
                        request.setMaskDays(parts[4]);
                        request.setMaskSchedules(parts[5]);

                        // Validación básica de fechas (más validaciones en AgendaService)
                        if (request.getStartDate() != null && request.getEndDate() != null && request.getEndDate().before(request.getStartDate())) {
                            logger.warn("Petición inválida en línea {}: la fecha de fin es anterior a la fecha de inicio. Línea: '{}'", lineNumber, line);
                            continue; // Saltar esta petición
                        }

                        requests.add(request);
                    } catch (DateTimeParseException e) {
                        logger.warn("Error al parsear fecha en la línea {}: '{}'. Error: {}", lineNumber, line, e.getMessage());
                        // Esta petición no se añadirá, podría ser una incidencia
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.warn("Error de formato (partes insuficientes después del split) en la línea {}: '{}'. Error: {}", lineNumber, line, e.getMessage());
                    } catch (Exception e) { // Captura genérica para otros errores inesperados por línea
                        logger.error("Error inesperado procesando la línea {}: '{}'", lineNumber, line, e);
                    }
                } else {
                    logger.warn("Línea ignorada (formato incorrecto - partes insuficientes) en línea {}: '{}'", lineNumber, line);
                }
            }
        }
        return requests;
    }
}