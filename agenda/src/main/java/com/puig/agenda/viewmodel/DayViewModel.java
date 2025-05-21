package com.puig.agenda.viewmodel;
import java.util.List;

public class DayViewModel {

    private String nombreDiaSemana;
    private int numeroDelMes;
    private int dayOfWeek; // Añadir esta propiedad
    private List<AgendaCellViewModel> cells; // Cambiar de Map a List

    public DayViewModel() {}

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

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public List<AgendaCellViewModel> getCells() {
        return cells;
    }

    public void setCells(List<AgendaCellViewModel> cells) {
        this.cells = cells;
    }
}