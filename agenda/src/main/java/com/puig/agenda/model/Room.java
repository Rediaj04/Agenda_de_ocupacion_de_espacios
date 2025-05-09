package com.puig.agenda.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class Room {
    private String name;

    Map<LocalDate, Map<LocalTime, SlotAgenda>> horario;

    public Room() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
