package com.college.academicmonitoring.dto;

public class PredictionResponse {

    private boolean predictedRisk;
    private double riskScore;
    private String priorityLevel;
    private String message;

    public PredictionResponse() {
    }

    public PredictionResponse(boolean predictedRisk, double riskScore, String priorityLevel, String message) {
        this.predictedRisk = predictedRisk;
        this.riskScore = riskScore;
        this.priorityLevel = priorityLevel;
        this.message = message;
    }

    public boolean isPredictedRisk() {
        return predictedRisk;
    }

    public void setPredictedRisk(boolean predictedRisk) {
        this.predictedRisk = predictedRisk;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(double riskScore) {
        this.riskScore = riskScore;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
