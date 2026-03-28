package com.college.academicmonitoring.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.college.academicmonitoring.dto.AIAnalysisResponse;
import com.college.academicmonitoring.dto.AIInsightsResponse;
import com.college.academicmonitoring.dto.AIReasonResponse;
import com.college.academicmonitoring.dto.PredictionResponse;
import com.college.academicmonitoring.model.AcademicRecord;
import com.college.academicmonitoring.repository.AcademicRecordRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AIServiceImpl implements AIService {

    private static final String STATUS_COMPLIANT = "COMPLIANT";
    private static final String STATUS_AT_RISK = "AT_RISK";
    private static final String STATUS_INTERVENTION_REQUIRED = "INTERVENTION_REQUIRED";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String openAiApiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String openAiModel;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AcademicRecordRepository academicRecordRepository;

    @Autowired
    private AcademicRecordService academicRecordService;

    @Override
    public AIAnalysisResponse analyzeStudent(AcademicRecord record) {
        validateRecord(record);

        // Cache the generated summary so repeated dashboard/API calls do not keep
        // recalculating or re-calling the LLM for the same unchanged record.
        if (StringUtils.hasText(record.getAiSummary())) {
            return buildResponseFromSummary(record.getAiSummary(), record);
        }

        AIAnalysisResponse response;
        if (StringUtils.hasText(openAiApiKey)) {
            try {
                response = requestAnalysisFromLlm(record);
            } catch (Exception ex) {
                response = buildFallbackAnalysis(record);
            }
        } else {
            response = buildFallbackAnalysis(record);
        }

        record.setAiSummary(response.getSummary());
        academicRecordRepository.save(record);
        return response;
    }

    @Override
    public PredictionResponse predictRisk(AcademicRecord record) {
        validateRecord(record);
        boolean predictedRisk = Boolean.TRUE.equals(record.getPredictedRisk());
        double riskScore = record.getRiskScore() != null ? record.getRiskScore() : 0.0;
        String message = predictedRisk ? "Likely to become AT_RISK" : "Student currently appears stable";
        return new PredictionResponse(predictedRisk, riskScore, safeText(record.getPriorityLevel()), message);
    }

    @Override
    public AIInsightsResponse getInsights() {
        List<AcademicRecord> records = academicRecordService.getAllRecords();
        if (records.isEmpty()) {
            return new AIInsightsResponse(0.0, 0.0, "No common issue detected", 0);
        }

        double averageAhi = academicRecordService.getAverageAhi();
        long atRiskStudents = records.stream()
                .filter(record -> !STATUS_COMPLIANT.equals(record.getStatus()))
                .count();
        double atRiskPercentage = (atRiskStudents * 100.0) / records.size();
        long attendanceIssues = records.stream()
                .filter(record -> safeValue(record.getAttendancePercentage()) < 75)
                .count();
        long marksIssues = records.stream()
                .filter(record -> safeValue(record.getInternalMarks()) < 60)
                .count();
        long criticalStudents = records.stream()
                .filter(record -> Boolean.TRUE.equals(record.getInterventionRequired()))
                .count();

        return new AIInsightsResponse(
                averageAhi,
                atRiskPercentage,
                resolveMostCommonIssue(attendanceIssues, marksIssues),
                criticalStudents
        );
    }

    @Override
    public String answerGeneralQuestion(String question) {
        if (!StringUtils.hasText(question)) {
            throw new IllegalArgumentException("Question is required");
        }

        if (StringUtils.hasText(openAiApiKey)) {
            try {
                return requestGeneralAnswer(question);
            } catch (Exception ex) {
                return buildFallbackChatAnswer(question);
            }
        }
        return buildFallbackChatAnswer(question);
    }

    private AIAnalysisResponse requestAnalysisFromLlm(AcademicRecord record) throws IOException, InterruptedException {
        String prompt = buildAnalysisPrompt(record);
        String responseContent = callOpenAi(
                "You are an academic monitoring assistant. Always return valid JSON with keys summary, riskLevel, recommendations.",
                prompt
        );

        JsonNode aiJson = objectMapper.readTree(responseContent);
        AIAnalysisResponse response = new AIAnalysisResponse();
        response.setSummary(aiJson.path("summary").asText(buildFallbackSummary(record)));
        response.setRiskLevel(aiJson.path("riskLevel").asText(determineRiskLevel(record)));
        response.setConfidence(calculateConfidence(record));
        response.setReasons(buildStructuredReasons(record));

        List<String> recommendations = new ArrayList<>();
        JsonNode recommendationsNode = aiJson.path("recommendations");
        if (recommendationsNode.isArray()) {
            recommendationsNode.forEach(node -> recommendations.add(node.asText()));
        }
        if (recommendations.isEmpty()) {
            recommendations.addAll(buildRecommendations(record));
        }
        response.setRecommendations(recommendations);
        return response;
    }

    private String requestGeneralAnswer(String question) throws IOException, InterruptedException {
        return callOpenAi(
                "You are an academic monitoring assistant. Answer clearly and concisely for faculty users.",
                question
        );
    }

    private String callOpenAi(String systemPrompt, String userPrompt) throws IOException, InterruptedException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", openAiModel);
        payload.put("temperature", 0.2);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(openAiApiUrl))
                .header("Authorization", "Bearer " + openAiApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 400) {
            throw new IOException("OpenAI API call failed with status " + response.statusCode());
        }

        JsonNode root = objectMapper.readTree(response.body());
        return root.path("choices").path(0).path("message").path("content").asText();
    }

    private AIAnalysisResponse buildFallbackAnalysis(AcademicRecord record) {
        return new AIAnalysisResponse(
                buildFallbackSummary(record),
                determineRiskLevel(record),
                calculateConfidence(record),
                buildStructuredReasons(record),
                buildRecommendations(record)
        );
    }

    private AIAnalysisResponse buildResponseFromSummary(String summary, AcademicRecord record) {
        return new AIAnalysisResponse(
                summary,
                determineRiskLevel(record),
                calculateConfidence(record),
                buildStructuredReasons(record),
                buildRecommendations(record)
        );
    }

    private String buildFallbackSummary(AcademicRecord record) {
        List<String> reasons = buildReasonMessages(record);
        if (STATUS_COMPLIANT.equals(record.getStatus()) && !Boolean.TRUE.equals(record.getPredictedRisk())) {
            return "Student is compliant with healthy attendance, marks, and assignment completion. Current indicators do not show immediate academic risk.";
        }
        return "Student needs closer attention because " + String.join(", ", reasons) + ".";
    }

    private String determineRiskLevel(AcademicRecord record) {
        double ahi = record.getAhi() != null ? record.getAhi() : 0.0;
        double riskScore = record.getRiskScore() != null ? record.getRiskScore() : 0.0;

        if (STATUS_INTERVENTION_REQUIRED.equals(record.getStatus()) || ahi < 50 || riskScore >= 28) {
            return "HIGH";
        }
        if (STATUS_AT_RISK.equals(record.getStatus()) || Boolean.TRUE.equals(record.getPredictedRisk()) || ahi < 75) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private List<String> buildReasonMessages(AcademicRecord record) {
        List<String> reasons = new ArrayList<>();
        double attendance = safeValue(record.getAttendancePercentage());
        double internalMarks = safeValue(record.getInternalMarks());
        int submitted = record.getAssignmentsSubmitted() != null ? record.getAssignmentsSubmitted() : 0;
        int totalAssignments = record.getTotalAssignments() != null ? record.getTotalAssignments() : 0;
        double submissionRatio = totalAssignments > 0 ? (submitted * 100.0) / totalAssignments : 0.0;

        if (attendance < 65) {
            reasons.add(String.format(Locale.ROOT, "attendance is low at %.2f%%", attendance));
        }
        if (internalMarks < 60) {
            reasons.add(String.format(Locale.ROOT, "internal marks are low at %.2f", internalMarks));
        }
        if (submissionRatio < 70) {
            reasons.add(String.format(Locale.ROOT, "assignment completion is weak at %.2f%%", submissionRatio));
        }
        if (record.getAhi() != null && record.getAhi() < 70) {
            reasons.add(String.format(Locale.ROOT, "AHI is below the safe threshold at %.2f", record.getAhi()));
        }
        if (reasons.isEmpty()) {
            reasons.add("performance indicators are currently stable");
        }
        return reasons;
    }

    private List<AIReasonResponse> buildStructuredReasons(AcademicRecord record) {
        List<AIReasonResponse> reasons = new ArrayList<>();
        double attendance = safeValue(record.getAttendancePercentage());
        double internalMarks = safeValue(record.getInternalMarks());
        int submitted = record.getAssignmentsSubmitted() != null ? record.getAssignmentsSubmitted() : 0;
        int totalAssignments = record.getTotalAssignments() != null ? record.getTotalAssignments() : 0;

        if (attendance < 75) {
            reasons.add(new AIReasonResponse("ATTENDANCE", "Below 75% attendance"));
        }
        if (internalMarks < 60) {
            reasons.add(new AIReasonResponse("MARKS", "Low internal marks"));
        }
        if (totalAssignments > 0 && submitted < totalAssignments) {
            reasons.add(new AIReasonResponse("ASSIGNMENTS", "Incomplete submissions"));
        }
        if (record.getAhi() != null && record.getAhi() < 70) {
            reasons.add(new AIReasonResponse("AHI", String.format(Locale.ROOT, "AHI is below 70 at %.2f", record.getAhi())));
        }
        if (reasons.isEmpty()) {
            reasons.add(new AIReasonResponse("STABLE", "Current record does not show a major academic concern"));
        }
        return reasons;
    }

    private List<String> buildRecommendations(AcademicRecord record) {
        List<String> recommendations = new ArrayList<>();

        if (record.getAttendancePercentage() != null && record.getAttendancePercentage() < 65) {
            recommendations.add("Schedule attendance counselling and review class participation weekly.");
        }
        if (record.getInternalMarks() != null && record.getInternalMarks() < 60) {
            recommendations.add("Assign remedial academic support for weak internal assessment performance.");
        }
        if (record.getTotalAssignments() != null && record.getTotalAssignments() > 0) {
            double submissionRatio = (record.getAssignmentsSubmitted() != null ? record.getAssignmentsSubmitted() : 0) * 100.0
                    / record.getTotalAssignments();
            if (submissionRatio < 70) {
                recommendations.add("Track assignment completion with short milestones and faculty follow-up.");
            }
        }
        if (recommendations.isEmpty()) {
            recommendations.add("Maintain the current study plan and continue periodic monitoring.");
            recommendations.add("Recognize the student for stable academic performance to reinforce consistency.");
        }
        return recommendations;
    }

    private String buildAnalysisPrompt(AcademicRecord record) {
        int submitted = record.getAssignmentsSubmitted() != null ? record.getAssignmentsSubmitted() : 0;
        int total = record.getTotalAssignments() != null ? record.getTotalAssignments() : 0;
        return "Analyze this student:\n"
                + "Attendance: " + safeNumber(record.getAttendancePercentage()) + "\n"
                + "Internal Marks: " + safeNumber(record.getInternalMarks()) + "\n"
                + "Assignments: " + submitted + "/" + total + "\n"
                + "AHI: " + safeNumber(record.getAhi()) + "\n"
                + "Status: " + safeText(record.getStatus()) + "\n\n"
                + "Return valid JSON only with:\n"
                + "{\n"
                + "  \"summary\": \"...\",\n"
                + "  \"riskLevel\": \"LOW | MEDIUM | HIGH\",\n"
                + "  \"recommendations\": [\"...\"]\n"
                + "}";
    }

    private double calculateConfidence(AcademicRecord record) {
        double riskScore = record.getRiskScore() != null ? record.getRiskScore() : 0.0;
        return Math.max(0.0, Math.min(1.0, 1 - (riskScore / 100.0)));
    }

    private String resolveMostCommonIssue(long attendanceIssues, long marksIssues) {
        if (attendanceIssues == 0 && marksIssues == 0) {
            return "No common issue detected";
        }
        if (attendanceIssues >= marksIssues) {
            return "Low attendance";
        }
        return "Low internal marks";
    }

    private String buildFallbackChatAnswer(String question) {
        return "OpenAI integration is not configured for free-form chat right now. "
                + "You can ask about at risk students, compliant students, or average AHI, "
                + "or configure an OpenAI API key to enable broader AI answers.";
    }

    private void validateRecord(AcademicRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Academic record is required");
        }
    }

    private double safeValue(Double value) {
        return value != null ? value : 0.0;
    }

    private String safeText(String value) {
        return value != null ? value : "";
    }

    private String safeNumber(Double value) {
        return value != null ? String.format(Locale.ROOT, "%.2f", value) : "0.00";
    }
}
