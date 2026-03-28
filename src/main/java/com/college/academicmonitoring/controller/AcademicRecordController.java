package com.college.academicmonitoring.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.college.academicmonitoring.dto.AcademicRecordFilterRequest;
import com.college.academicmonitoring.dto.InterventionRequest;
import com.college.academicmonitoring.model.AcademicRecord;
import com.college.academicmonitoring.model.AcademicRecordHistory;
import com.college.academicmonitoring.service.AcademicRecordService;

@RestController
@RequestMapping("/api/records")
@CrossOrigin(origins = "*")
public class AcademicRecordController {

    @Autowired
    private AcademicRecordService recordService;

    @PostMapping
    public AcademicRecord createRecord(@Valid @RequestBody AcademicRecord record) {
        return recordService.saveRecord(record);
    }

    @GetMapping
    public Object getAllRecords(@RequestParam(name = "page", required = false) Integer page,
                                @RequestParam(name = "size", required = false) Integer size) {
        if (page != null && size != null) {
            return recordService.getPagedRecords(page, size);
        }
        return recordService.getAllRecords();
    }

    @GetMapping("/student/{studentId}")
    public AcademicRecord getByStudentId(@PathVariable Long studentId) {
        return recordService.getByStudentId(studentId);
    }

    @GetMapping("/status/{status}")
    public List<AcademicRecord> getByStatus(@PathVariable String status) {
        return recordService.getByStatus(status);
    }

    @GetMapping("/average-ahi")
    public Double getAverageAhi() {
        return recordService.getAverageAhi();
    }

    @PutMapping("/student/{studentId}")
    public AcademicRecord updateRecordByStudentId(@PathVariable Long studentId, @Valid @RequestBody AcademicRecord record) {
        return recordService.updateRecordByStudentId(studentId, record);
    }

    @DeleteMapping("/student/{studentId}")
    public void deleteRecordByStudentId(@PathVariable Long studentId) {
        recordService.deleteRecordByStudentId(studentId);
    }

    @GetMapping("/filter")
    public Map<String, Object> filterRecords(@RequestParam(name = "name", required = false) String name,
                                             @RequestParam(name = "status", required = false) String status,
                                             @RequestParam(name = "minAhi", required = false) Double minAhi,
                                             @RequestParam(name = "maxAhi", required = false) Double maxAhi,
                                             @RequestParam(name = "attendanceBelow", required = false) Double attendanceBelow,
                                             @RequestParam(name = "internalMarksBelow", required = false) Double internalMarksBelow,
                                             @RequestParam(name = "predictedRisk", required = false) Boolean predictedRisk,
                                             @RequestParam(name = "interventionRequired", required = false) Boolean interventionRequired,
                                             @RequestParam(name = "page", defaultValue = "0") int page,
                                             @RequestParam(name = "size", defaultValue = "10") int size) {
        AcademicRecordFilterRequest filter = new AcademicRecordFilterRequest();
        filter.setName(name);
        filter.setStatus(status);
        filter.setMinAhi(minAhi);
        filter.setMaxAhi(maxAhi);
        filter.setAttendanceBelow(attendanceBelow);
        filter.setInternalMarksBelow(internalMarksBelow);
        filter.setPredictedRisk(predictedRisk);
        filter.setInterventionRequired(interventionRequired);
        return recordService.filterRecords(filter, page, size);
    }

    @PutMapping("/{recordId}/intervene")
    public AcademicRecord intervene(@PathVariable Long recordId, @RequestBody InterventionRequest request) {
        return recordService.intervene(recordId, request);
    }

    @GetMapping("/{recordId}/history")
    public List<AcademicRecordHistory> getHistory(@PathVariable Long recordId) {
        return recordService.getHistoryByRecordId(recordId);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportRecords() {
        byte[] csvBytes = recordService.exportRecordsCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=academic-records.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }
}
