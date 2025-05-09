package com.puig.agenda.model;

public class Incidence {
    private Request requestRejected;
    private String reason;

    public Incidence() {}

    public Request getRequestRejected() {
        return requestRejected;
    }

    public void setRequestRejected(Request requestRejected) {
        this.requestRejected = requestRejected;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
