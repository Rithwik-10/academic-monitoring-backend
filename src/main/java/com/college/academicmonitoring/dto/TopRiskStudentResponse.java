package com.college.academicmonitoring.dto;

public class TopRiskStudentResponse {

    private Long studentId;
    private String studentName;
    private String rollNumber;
    private Double riskScore;
    private Boolean predictedRisk;
    private Double ahi;
    private String status;
    private String priorityLevel;

    public TopRiskStudentResponse() {
    }

    public TopRiskStudentResponse(Long studentId, String studentName, String rollNumber,
                                  Double riskScore, Boolean predictedRisk, Double ahi, String status,
                                  String priorityLevel) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.riskScore = riskScore;
        this.predictedRisk = predictedRisk;
        this.ahi = ahi;
        this.status = status;
        this.priorityLevel = priorityLevel;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public Boolean getPredictedRisk() {
        return predictedRisk;
    }

    public void setPredictedRisk(Boolean predictedRisk) {
        this.predictedRisk = predictedRisk;
    }

    public Double getAhi() {
        return ahi;
    }

    public void setAhi(Double ahi) {
        this.ahi = ahi;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }
}
