package com.college.academicmonitoring.dto;

import java.util.ArrayList;
import java.util.List;

public class AIAnalysisResponse {

    private String summary;
    private String riskLevel;
    private double confidence;
    private List<AIReasonResponse> reasons = new ArrayList<>();
    private List<String> recommendations = new ArrayList<>();

    public AIAnalysisResponse() {
    }

    public AIAnalysisResponse(String summary, String riskLevel, double confidence,
                              List<AIReasonResponse> reasons, List<String> recommendations) {
        this.summary = summary;
        this.riskLevel = riskLevel;
        this.confidence = confidence;
        this.reasons = reasons;
        this.recommendations = recommendations;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public List<AIReasonResponse> getReasons() {
        return reasons;
    }

    public void setReasons(List<AIReasonResponse> reasons) {
        this.reasons = reasons;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
