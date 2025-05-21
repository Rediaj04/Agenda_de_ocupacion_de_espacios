package com.puig.agenda.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
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

    public Map<LocalDate, Map<LocalTime, SlotAgenda>> getHorario() {
        return horario;
    }

    public void setHorario(Map<LocalDate, Map<LocalTime, SlotAgenda>> horario) {
        this.horario = horario;
    }

    public void setSlot(LocalDate date, LocalTime time, SlotAgenda slot) {
        if (!horario.containsKey(date)) {
            horario.put(date, new HashMap<>());
        }
        horario.get(date).put(time, slot);
    }

    public SlotAgenda getSlot(LocalDate date, LocalTime time) {
        if (horario.containsKey(date) && horario.get(date).containsKey(time)) {
            return horario.get(date).get(time);
        }
        return null;
    }
}
