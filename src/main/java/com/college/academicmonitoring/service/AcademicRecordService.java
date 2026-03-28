package com.college.academicmonitoring.service;

import java.util.Map;
import java.util.List;
import com.college.academicmonitoring.model.AcademicRecord;
import com.college.academicmonitoring.model.AcademicRecordHistory;
import com.college.academicmonitoring.model.Alert;
import com.college.academicmonitoring.dto.AcademicRecordFilterRequest;
import com.college.academicmonitoring.dto.InterventionRequest;

public interface AcademicRecordService {

    AcademicRecord saveRecord(AcademicRecord record);

    List<AcademicRecord> getAllRecords();

    AcademicRecord getByStudentId(Long studentId);

    List<AcademicRecord> getByStatus(String status);

    Double getAverageAhi();

    AcademicRecord updateRecordByStudentId(Long studentId, AcademicRecord record);

    void deleteRecordByStudentId(Long studentId);

    AcademicRecord intervene(Long recordId, InterventionRequest request);

    List<AcademicRecordHistory> getHistoryByRecordId(Long recordId);

    List<Alert> getRecentAlerts();

    Map<String, Object> getPagedRecords(int page, int size);

    Map<String, Object> filterRecords(AcademicRecordFilterRequest filter, int page, int size);

    byte[] exportRecordsCsv();

    List<AcademicRecord> getTopRiskRecords(int limit);
}
