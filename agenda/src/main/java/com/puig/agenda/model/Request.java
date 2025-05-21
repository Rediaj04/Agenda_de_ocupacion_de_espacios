package com.puig.agenda.model;

import java.util.Date;

public class Request {
    private String activityName;
    private Room room;
    private Date startDate;
    private Date endDate;
    private String maskDays;
    private String maskSchedules;

    public Request() {}
    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getMaskDays() {
        return maskDays;
    }

    public void setMaskDays(String maskDays) {
        this.maskDays = maskDays;
    }

    public String getMaskSchedules() {
        return maskSchedules;
    }

    public void setMaskSchedules(String maskSchedules) {
        this.maskSchedules = maskSchedules;
    }

}
