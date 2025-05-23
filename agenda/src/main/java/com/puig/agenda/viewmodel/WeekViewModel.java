package com.puig.agenda.viewmodel;

import java.util.List;

public class WeekViewModel {
    private int weekNumber;
    private List<DayViewModel> days;

    public WeekViewModel() {};

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public List<DayViewModel> getDays() {
        return days;
    }

    public void setDays(List<DayViewModel> days) {
        this.days = days;
    }
}