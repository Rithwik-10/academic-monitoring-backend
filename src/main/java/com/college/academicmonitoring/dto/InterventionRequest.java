package com.college.academicmonitoring.dto;

public class InterventionRequest {

    private Boolean interventionRequired;
    private String interventionNotes;

    public Boolean getInterventionRequired() {
        return interventionRequired;
    }

    public void setInterventionRequired(Boolean interventionRequired) {
        this.interventionRequired = interventionRequired;
    }

    public String getInterventionNotes() {
        return interventionNotes;
    }

    public void setInterventionNotes(String interventionNotes) {
        this.interventionNotes = interventionNotes;
    }
}
