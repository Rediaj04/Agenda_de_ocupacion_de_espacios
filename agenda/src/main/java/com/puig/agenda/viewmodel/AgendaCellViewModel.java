package com.puig.agenda.viewmodel;

public class AgendaCellViewModel {
    private String hour;
    private String status;
    private String activity;

    public AgendaCellViewModel() {}

    public String getHour() {
        return hour;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }
}