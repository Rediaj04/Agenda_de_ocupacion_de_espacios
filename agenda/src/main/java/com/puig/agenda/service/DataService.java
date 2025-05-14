package com.puig.agenda.service;

import com.puig.agenda.model.Configuration;
import com.puig.agenda.model.Request;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataService {

    public Configuration parseConfigFile(MultipartFile configFile) throws IOException {
        Configuration config = new Configuration();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(configFile.getInputStream()))) {
            // Leer primera línea: año y mes
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    config.setYear(Integer.parseInt(parts[0]));
                    config.setMonth(Integer.parseInt(parts[1]));
                }
            }

            // Leer segunda línea: idiomas
            line = reader.readLine();
            if (line != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    config.setEntryLanguage(parts[0]);
                    config.setExitLanguage(parts[1]);
                }
            }
        }

        return config;
    }

    public List<Request> parseRequestsFile(MultipartFile requestsFile) throws IOException {
        List<Request> requests = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(requestsFile.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;

                // Dividir la línea por espacios
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 6) {
                    try {
                        Request request = new Request();
                        request.setActivityName(parts[0]);

                        // Crear sala
                        com.puig.agenda.model.Room room = new com.puig.agenda.model.Room();
                        room.setName(parts[1]);
                        request.setRoom(room);

                        // Parsear fechas
                        request.setStartDate(dateFormat.parse(parts[2]));
                        request.setEndDate(dateFormat.parse(parts[3]));

                        // Asignar máscaras
                        request.setMaskDays(parts[4]);
                        request.setMaskSchedules(parts[5]);

                        requests.add(request);
                    } catch (ParseException e) {
                        // Manejar error de parseo de fechas
                        System.err.println("Error al parsear fecha: " + e.getMessage());
                    }
                }
            }
        }

        return requests;
    }
}