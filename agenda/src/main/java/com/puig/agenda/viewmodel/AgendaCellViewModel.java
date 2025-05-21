package com.puig.agenda.viewmodel;

public class AgendaCellViewModel {
    private String textToShow;
    private String cssClass;

    public AgendaCellViewModel() {}

    public String getTextToShow() {
        return textToShow;
    }

    public void setTextToShow(String textToShow) {
        this.textToShow = textToShow;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }
}
