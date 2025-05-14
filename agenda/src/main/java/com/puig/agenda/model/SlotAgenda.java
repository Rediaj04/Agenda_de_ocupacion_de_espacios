package com.puig.agenda.model;

import java.time.LocalTime;

public class SlotAgenda {
    public enum SlotStatus {
        FREE,
        BOOKED,
        PENDING,
        BLOCKED
    };

    private LocalTime startTime;
    private LocalTime endTime;
    private String assignedActivityName;

    public SlotAgenda() {}

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getAssignedActivityName() {
        return assignedActivityName;
    }

    public void setAssignedActivityName(String assignedActivityName) {
        this.assignedActivityName = assignedActivityName;
    }
}
