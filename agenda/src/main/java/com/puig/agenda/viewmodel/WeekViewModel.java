package com.puig.agenda.viewmodel;

import java.util.List;

public class WeekViewModel {
    private List<DayViewoModel> days;

    public WeekViewModel() {};

    public List<DayViewoModel> getDays() {
        return days;
    }

    public void setDays(List<DayViewoModel> days) {
        this.days = days;
    }
}
