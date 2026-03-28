package com.college.academicmonitoring.dto;

public class AIInsightsResponse {

    private double averageAhi;
    private double atRiskPercentage;
    private String mostCommonIssue;
    private long criticalStudents;

    public AIInsightsResponse() {
    }

    public AIInsightsResponse(double averageAhi, double atRiskPercentage, String mostCommonIssue, long criticalStudents) {
        this.averageAhi = averageAhi;
        this.atRiskPercentage = atRiskPercentage;
        this.mostCommonIssue = mostCommonIssue;
        this.criticalStudents = criticalStudents;
    }

    public double getAverageAhi() {
        return averageAhi;
    }

    public void setAverageAhi(double averageAhi) {
        this.averageAhi = averageAhi;
    }

    public double getAtRiskPercentage() {
        return atRiskPercentage;
    }

    public void setAtRiskPercentage(double atRiskPercentage) {
        this.atRiskPercentage = atRiskPercentage;
    }

    public String getMostCommonIssue() {
        return mostCommonIssue;
    }

    public void setMostCommonIssue(String mostCommonIssue) {
        this.mostCommonIssue = mostCommonIssue;
    }

    public long getCriticalStudents() {
        return criticalStudents;
    }

    public void setCriticalStudents(long criticalStudents) {
        this.criticalStudents = criticalStudents;
    }
}
