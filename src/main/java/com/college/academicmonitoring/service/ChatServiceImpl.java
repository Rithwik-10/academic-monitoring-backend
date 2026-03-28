package com.college.academicmonitoring.service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.college.academicmonitoring.dto.ChatResponse;
import com.college.academicmonitoring.model.AcademicRecord;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private AcademicRecordService academicRecordService;

    @Autowired
    private AIService aiService;

    @Override
    public ChatResponse answerQuestion(String question) {
        if (!StringUtils.hasText(question)) {
            throw new IllegalArgumentException("Question is required");
        }

        String normalizedQuestion = question.trim().toLowerCase(Locale.ROOT);

        if (normalizedQuestion.contains("at risk")) {
            List<AcademicRecord> atRiskStudents = academicRecordService.getByStatus("AT_RISK");
            return new ChatResponse(buildStudentListAnswer("AT_RISK students", atRiskStudents));
        }

        if (normalizedQuestion.contains("compliant")) {
            List<AcademicRecord> compliantStudents = academicRecordService.getByStatus("COMPLIANT");
            return new ChatResponse(buildStudentListAnswer("COMPLIANT students", compliantStudents));
        }

        if (normalizedQuestion.contains("average ahi")) {
            Double averageAhi = academicRecordService.getAverageAhi();
            return new ChatResponse(String.format(Locale.ROOT, "Current average AHI is %.2f.", averageAhi));
        }

        return new ChatResponse(aiService.answerGeneralQuestion(question));
    }

    private String buildStudentListAnswer(String label, List<AcademicRecord> records) {
        if (records == null || records.isEmpty()) {
            return "No " + label + " found right now.";
        }

        String students = records.stream()
                .map(record -> record.getStudent().getName() + " (" + record.getStudent().getRollNumber() + ")")
                .collect(Collectors.joining(", "));

        return label + ": " + students;
    }
}
