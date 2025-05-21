package com.puig.agenda.viewmodel;

import java.util.List;

public class AgendaViewModel {
    private int year;
    private String nameMonth;
    private List<RoomViewModel> rooms;
    private List<IncidenceViewModel> incidences;

    public AgendaViewModel() {};

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getNameMonth() {
        return nameMonth;
    }

    public void setNameMonth(String nameMonth) {
        this.nameMonth = nameMonth;
    }

    public List<RoomViewModel> getRooms() {
        return rooms;
    }

    public void setRooms(List<RoomViewModel> rooms) {
        this.rooms = rooms;
    }

    public List<IncidenceViewModel> getIncidences() {
        return incidences;
    }

    public void setIncidences(List<IncidenceViewModel> incidences) {
        this.incidences = incidences;
    }
}
