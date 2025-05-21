package com.puig.agenda.viewmodel;
import java.util.Map;

public class DayViewoModel {

    private String nombreDiaSemana;
    private int numeroDelMes;
    private Map<String, AgendaCellViewModel> slotsPerHour;

    public DayViewoModel() {}

    public String getNombreDiaSemana() {
        return nombreDiaSemana;
    }

    public void setNombreDiaSemana(String nombreDiaSemana) {
        this.nombreDiaSemana = nombreDiaSemana;
    }

    public int getNumeroDelMes() {
        return numeroDelMes;
    }

    public void setNumeroDelMes(int numeroDelMes) {
        this.numeroDelMes = numeroDelMes;
    }

    public Map<String, AgendaCellViewModel> getSlotsPorHora() {
        return slotsPerHour;
    }

    public void setSlotsPorHora(Map<String, AgendaCellViewModel> slotsPorHora) {
        this.slotsPerHour = slotsPorHora;
    }
}