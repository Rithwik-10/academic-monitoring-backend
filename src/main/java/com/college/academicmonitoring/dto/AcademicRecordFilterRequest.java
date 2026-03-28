package com.college.academicmonitoring.dto;

public class AcademicRecordFilterRequest {

    private String name;
    private String status;
    private Double minAhi;
    private Double maxAhi;
    private Double attendanceBelow;
    private Double internalMarksBelow;
    private Boolean predictedRisk;
    private Boolean interventionRequired;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getMinAhi() {
        return minAhi;
    }

    public void setMinAhi(Double minAhi) {
        this.minAhi = minAhi;
    }

    public Double getMaxAhi() {
        return maxAhi;
    }

    public void setMaxAhi(Double maxAhi) {
        this.maxAhi = maxAhi;
    }

    public Double getAttendanceBelow() {
        return attendanceBelow;
    }

    public void setAttendanceBelow(Double attendanceBelow) {
        this.attendanceBelow = attendanceBelow;
    }

    public Double getInternalMarksBelow() {
        return internalMarksBelow;
    }

    public void setInternalMarksBelow(Double internalMarksBelow) {
        this.internalMarksBelow = internalMarksBelow;
    }

    public Boolean getPredictedRisk() {
        return predictedRisk;
    }

    public void setPredictedRisk(Boolean predictedRisk) {
        this.predictedRisk = predictedRisk;
    }

    public Boolean getInterventionRequired() {
        return interventionRequired;
    }

    public void setInterventionRequired(Boolean interventionRequired) {
        this.interventionRequired = interventionRequired;
    }
}
