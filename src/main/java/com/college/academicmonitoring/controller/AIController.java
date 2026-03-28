package com.college.academicmonitoring.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.college.academicmonitoring.dto.AIAnalysisResponse;
import com.college.academicmonitoring.dto.AIInsightsResponse;
import com.college.academicmonitoring.dto.PredictionResponse;
import com.college.academicmonitoring.dto.TopRiskStudentResponse;
import com.college.academicmonitoring.model.AcademicRecord;
import com.college.academicmonitoring.service.AIService;
import com.college.academicmonitoring.service.AcademicRecordService;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AcademicRecordService academicRecordService;

    @Autowired
    private AIService aiService;

    @GetMapping("/analyze/{studentId}")
    public AIAnalysisResponse analyzeStudent(@PathVariable Long studentId) {
        AcademicRecord record = academicRecordService.getByStudentId(studentId);
        return aiService.analyzeStudent(record);
    }

    @GetMapping("/predict/{studentId}")
    public PredictionResponse predictStudentRisk(@PathVariable Long studentId) {
        AcademicRecord record = academicRecordService.getByStudentId(studentId);
        return aiService.predictRisk(record);
    }

    @GetMapping("/insights")
    public AIInsightsResponse getInsights() {
        return aiService.getInsights();
    }

    @GetMapping("/top-risk")
    public List<TopRiskStudentResponse> getTopRiskStudents() {
        return academicRecordService.getTopRiskRecords(5).stream()
                .map(this::mapToTopRiskResponse)
                .collect(Collectors.toList());
    }

    private TopRiskStudentResponse mapToTopRiskResponse(AcademicRecord record) {
        return new TopRiskStudentResponse(
                record.getStudent().getId(),
                record.getStudent().getName(),
                record.getStudent().getRollNumber(),
                record.getRiskScore(),
                record.getPredictedRisk(),
                record.getAhi(),
                record.getStatus(),
                record.getPriorityLevel()
        );
    }
}
