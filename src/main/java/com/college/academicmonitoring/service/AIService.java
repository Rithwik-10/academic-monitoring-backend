package com.college.academicmonitoring.service;

import com.college.academicmonitoring.dto.AIAnalysisResponse;
import com.college.academicmonitoring.dto.AIInsightsResponse;
import com.college.academicmonitoring.dto.PredictionResponse;
import com.college.academicmonitoring.model.AcademicRecord;

public interface AIService {

    AIAnalysisResponse analyzeStudent(AcademicRecord record);

    PredictionResponse predictRisk(AcademicRecord record);

    AIInsightsResponse getInsights();

    String answerGeneralQuestion(String question);
}
