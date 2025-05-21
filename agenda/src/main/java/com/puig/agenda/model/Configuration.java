package com.puig.agenda.model;

public class Configuration {
    private int year;
    private int month;
    private String entryLanguage;
    private String exitLanguage;

    public Configuration() {};

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public String getEntryLanguage() {
        return entryLanguage;
    }

    public void setEntryLanguage(String entryLanguage) {
        this.entryLanguage = entryLanguage;
    }

    public String getExitLanguage() {
        return exitLanguage;
    }

    public void setExitLanguage(String exitLanguage) {
        this.exitLanguage = exitLanguage;
    }
}
