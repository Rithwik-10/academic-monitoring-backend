package com.college.academicmonitoring.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.college.academicmonitoring.dto.AcademicRecordFilterRequest;
import com.college.academicmonitoring.dto.InterventionRequest;
import com.college.academicmonitoring.exception.DuplicateResourceException;
import com.college.academicmonitoring.exception.ResourceNotFoundException;
import com.college.academicmonitoring.model.AcademicRecord;
import com.college.academicmonitoring.model.AcademicRecordHistory;
import com.college.academicmonitoring.model.Alert;
import com.college.academicmonitoring.model.Student;
import com.college.academicmonitoring.repository.AcademicRecordHistoryRepository;
import com.college.academicmonitoring.repository.AcademicRecordRepository;
import com.college.academicmonitoring.repository.AlertRepository;
import com.college.academicmonitoring.repository.StudentRepository;

import jakarta.persistence.criteria.Predicate;

@Service
public class AcademicRecordServiceImpl implements AcademicRecordService {

    private static final String STATUS_COMPLIANT = "COMPLIANT";
    private static final String STATUS_AT_RISK = "AT_RISK";
    private static final String STATUS_INTERVENTION_REQUIRED = "INTERVENTION_REQUIRED";
    private static final String PRIORITY_HIGH = "HIGH";
    private static final String PRIORITY_MEDIUM = "MEDIUM";
    private static final String PRIORITY_LOW = "LOW";

    @Autowired
    private AcademicRecordRepository recordRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AcademicRecordHistoryRepository historyRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Override
    public AcademicRecord saveRecord(AcademicRecord record) {
        if (record == null || record.getStudent() == null || record.getStudent().getId() == null) {
            throw new IllegalArgumentException("Student id is required");
        }

        Long studentId = record.getStudent().getId();

        if (recordRepository.existsByStudentId(studentId)) {
            throw new DuplicateResourceException("Academic record already exists for student id: " + studentId);
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        record.setStudent(student);
        record.setAiSummary(null);
        applyCalculatedFields(record);
        AcademicRecord savedRecord = recordRepository.save(record);
        saveHistory(savedRecord);
        saveAlertIfNeeded(savedRecord);
        return savedRecord;
    }

    @Override
    public List<AcademicRecord> getAllRecords() {
        List<AcademicRecord> records = recordRepository.findAll();
        records.forEach(this::applyCalculatedFields);
        return records;
    }

    @Override
    public AcademicRecord getByStudentId(Long studentId) {
        AcademicRecord record = recordRepository.findByStudentId(studentId);
        if (record == null) {
            throw new ResourceNotFoundException("Academic record not found for student id: " + studentId);
        }
        applyCalculatedFields(record);
        return record;
    }

    @Override
    public List<AcademicRecord> getByStatus(String status) {
        String normalizedStatus = normalizeStatus(status);
        List<AcademicRecord> records = recordRepository.findByStatus(normalizedStatus);
        records.forEach(this::applyCalculatedFields);
        return records;
    }

    @Override
    public Double getAverageAhi() {
        Double averageAhi = recordRepository.findAverageAhi();
        return averageAhi != null ? averageAhi : 0.0;
    }

    @Override
    public AcademicRecord updateRecordByStudentId(Long studentId, AcademicRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("Academic record data is required");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        AcademicRecord existingRecord = recordRepository.findByStudentId(studentId);
        if (existingRecord == null) {
            throw new ResourceNotFoundException("Academic record not found for student id: " + studentId);
        }

        existingRecord.setStudent(student);
        existingRecord.setAttendancePercentage(record.getAttendancePercentage());
        existingRecord.setInternalMarks(record.getInternalMarks());
        existingRecord.setAssignmentsSubmitted(record.getAssignmentsSubmitted());
        existingRecord.setTotalAssignments(record.getTotalAssignments());
        existingRecord.setAiSummary(null);
        applyCalculatedFields(existingRecord);
        AcademicRecord savedRecord = recordRepository.save(existingRecord);
        saveHistory(savedRecord);
        saveAlertIfNeeded(savedRecord);
        return savedRecord;
    }

    @Override
    public void deleteRecordByStudentId(Long studentId) {
        AcademicRecord existingRecord = recordRepository.findByStudentId(studentId);
        if (existingRecord == null) {
            throw new ResourceNotFoundException("Academic record not found for student id: " + studentId);
        }
        recordRepository.delete(existingRecord);
    }

    @Override
    public AcademicRecord intervene(Long recordId, InterventionRequest request) {
        AcademicRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic record not found with id: " + recordId));

        boolean interventionRequired = request.getInterventionRequired() != null ? request.getInterventionRequired() : true;
        record.setInterventionRequired(interventionRequired);
        record.setInterventionNotes(request.getInterventionNotes());
        record.setLastReviewedAt(LocalDateTime.now());
        applyCalculatedFields(record);
        AcademicRecord savedRecord = recordRepository.save(record);
        saveHistory(savedRecord);
        return savedRecord;
    }

    @Override
    public List<AcademicRecordHistory> getHistoryByRecordId(Long recordId) {
        AcademicRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic record not found with id: " + recordId));
        return historyRepository.findByStudentIdOrderByRecordedAtAsc(record.getStudent().getId());
    }

    @Override
    public List<Alert> getRecentAlerts() {
        return alertRepository.findTop20ByOrderByCreatedAtDesc();
    }

    @Override
    public Map<String, Object> getPagedRecords(int page, int size) {
        Page<AcademicRecord> recordPage = recordRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
        recordPage.getContent().forEach(this::applyCalculatedFields);
        return buildPageResponse(recordPage);
    }

    @Override
    public Map<String, Object> filterRecords(AcademicRecordFilterRequest filter, int page, int size) {
        Specification<AcademicRecord> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("student").get("name")), "%" + filter.getName().trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus().trim().toUpperCase(Locale.ROOT)));
            }
            if (filter.getMinAhi() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("ahi"), filter.getMinAhi()));
            }
            if (filter.getMaxAhi() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("ahi"), filter.getMaxAhi()));
            }
            if (filter.getAttendanceBelow() != null) {
                predicates.add(cb.lessThan(root.get("attendancePercentage"), filter.getAttendanceBelow()));
            }
            if (filter.getInternalMarksBelow() != null) {
                predicates.add(cb.lessThan(root.get("internalMarks"), filter.getInternalMarksBelow()));
            }
            if (filter.getPredictedRisk() != null) {
                predicates.add(cb.equal(root.get("predictedRisk"), filter.getPredictedRisk()));
            }
            if (filter.getInterventionRequired() != null) {
                predicates.add(cb.equal(root.get("interventionRequired"), filter.getInterventionRequired()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AcademicRecord> recordPage = recordRepository.findAll(specification, PageRequest.of(page, size, Sort.by("ahi").ascending()));
        recordPage.getContent().forEach(this::applyCalculatedFields);
        return buildPageResponse(recordPage);
    }

    @Override
    public byte[] exportRecordsCsv() {
        StringBuilder builder = new StringBuilder();
        builder.append("Record ID,Student ID,Student Name,Roll Number,Department,Attendance,Internal Marks,Assignments Submitted,Total Assignments,AHI,Status,Predicted Risk,Risk Score,Priority Level,Intervention Required,AI Summary,Last Reviewed At\n");
        List<AcademicRecord> records = recordRepository.findAll(Sort.by("id").ascending());
        records.forEach(this::applyCalculatedFields);
        for (AcademicRecord record : records) {
            builder.append(record.getId()).append(",")
                    .append(record.getStudent().getId()).append(",")
                    .append(escapeCsv(record.getStudent().getName())).append(",")
                    .append(escapeCsv(record.getStudent().getRollNumber())).append(",")
                    .append(escapeCsv(record.getStudent().getDepartment())).append(",")
                    .append(record.getAttendancePercentage()).append(",")
                    .append(record.getInternalMarks()).append(",")
                    .append(record.getAssignmentsSubmitted()).append(",")
                    .append(record.getTotalAssignments()).append(",")
                    .append(record.getAhi()).append(",")
                    .append(record.getStatus()).append(",")
                    .append(record.getPredictedRisk()).append(",")
                    .append(record.getRiskScore()).append(",")
                    .append(escapeCsv(record.getPriorityLevel())).append(",")
                    .append(record.getInterventionRequired()).append(",")
                    .append(escapeCsv(record.getAiSummary())).append(",")
                    .append(record.getLastReviewedAt() != null ? record.getLastReviewedAt() : "")
                    .append("\n");
        }
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public List<AcademicRecord> getTopRiskRecords(int limit) {
        return recordRepository.findAll().stream()
                .peek(this::applyCalculatedFields)
                .sorted(Comparator.comparing(AcademicRecord::getRiskScore, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AcademicRecord::getAhi, Comparator.nullsLast(Comparator.naturalOrder())))
                .limit(Math.max(limit, 0))
                .collect(Collectors.toList());
    }

    private void applyCalculatedFields(AcademicRecord record) {
        double attendance = safeDouble(record.getAttendancePercentage());
        double internalMarks = safeDouble(record.getInternalMarks());
        int assignmentsSubmitted = safeInteger(record.getAssignmentsSubmitted());
        int totalAssignments = safeInteger(record.getTotalAssignments());

        if (assignmentsSubmitted > totalAssignments) {
            throw new IllegalArgumentException("Assignments submitted cannot be greater than total assignments");
        }

        double submissionRatio = 0.0;
        if (totalAssignments > 0) {
            submissionRatio = ((double) assignmentsSubmitted / totalAssignments) * 100;
        }

        double ahi = (attendance * 0.4) + (internalMarks * 0.4) + (submissionRatio * 0.2);
        double riskScore = ((100 - attendance) * 0.4) + ((100 - internalMarks) * 0.4);
        boolean predictedRisk = attendance < 65 || internalMarks < 60 || ahi < 70;
        boolean interventionRequired = predictedRisk || ahi < 70;

        record.setAhi(ahi);
        record.setStatus(classifyStatus(ahi));
        record.setPredictedRisk(predictedRisk);
        record.setRiskScore(riskScore);
        record.setInterventionRequired(interventionRequired);
        record.setPriorityLevel(classifyPriorityLevel(riskScore));
    }

    private String classifyStatus(double ahi) {
        if (ahi >= 75) {
            return STATUS_COMPLIANT;
        }
        if (ahi >= 50) {
            return STATUS_AT_RISK;
        }
        return STATUS_INTERVENTION_REQUIRED;
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private String classifyPriorityLevel(double riskScore) {
        if (riskScore > 70) {
            return PRIORITY_HIGH;
        }
        if (riskScore > 40) {
            return PRIORITY_MEDIUM;
        }
        return PRIORITY_LOW;
    }

    private double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private int safeInteger(Integer value) {
        return value != null ? value : 0;
    }

    private void saveHistory(AcademicRecord record) {
        AcademicRecordHistory history = new AcademicRecordHistory();
        history.setAcademicRecord(record);
        history.setStudent(record.getStudent());
        history.setAttendancePercentage(record.getAttendancePercentage());
        history.setInternalMarks(record.getInternalMarks());
        history.setAssignmentsSubmitted(record.getAssignmentsSubmitted());
        history.setTotalAssignments(record.getTotalAssignments());
        history.setAhi(record.getAhi());
        history.setStatus(record.getStatus());
        history.setRecordedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private void saveAlertIfNeeded(AcademicRecord record) {
        if ("COMPLIANT".equals(record.getStatus()) && !Boolean.TRUE.equals(record.getPredictedRisk())) {
            return;
        }
        Alert alert = new Alert();
        alert.setStudent(record.getStudent());
        if (Boolean.TRUE.equals(record.getInterventionRequired())) {
            alert.setMessage("Needs intervention for " + record.getStudent().getName() + " (" + record.getStatus() + ")");
        } else if (Boolean.TRUE.equals(record.getPredictedRisk())) {
            alert.setMessage("Likely to become at risk: " + record.getStudent().getName());
        } else {
            alert.setMessage("Student became " + record.getStatus() + ": " + record.getStudent().getName());
        }
        alert.setCreatedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }

    private Map<String, Object> buildPageResponse(Page<AcademicRecord> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("page", page.getNumber());
        response.put("size", page.getSize());
        response.put("totalElements", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        return response;
    }

    private String escapeCsv(String value) {
        String safeValue = value != null ? value : "";
        return "\"" + safeValue.replace("\"", "\"\"") + "\"";
    }
}
