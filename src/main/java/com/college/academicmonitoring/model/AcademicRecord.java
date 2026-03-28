package com.college.academicmonitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "academic_records")
public class AcademicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to student
    @OneToOne
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    @NotNull(message = "Student is required")
    private Student student;

    @Column(nullable = false)
    @NotNull(message = "Attendance percentage is required")
    @Min(value = 0, message = "Attendance percentage cannot be less than 0")
    @Max(value = 100, message = "Attendance percentage cannot be greater than 100")
    private Double attendancePercentage;

    @Column(nullable = false)
    @NotNull(message = "Internal marks are required")
    @Min(value = 0, message = "Internal marks cannot be less than 0")
    @Max(value = 100, message = "Internal marks cannot be greater than 100")
    private Double internalMarks;

    @Column(nullable = false)
    @NotNull(message = "Assignments submitted is required")
    @Min(value = 0, message = "Assignments submitted cannot be less than 0")
    private Integer assignmentsSubmitted;

    @Column(nullable = false)
    @NotNull(message = "Total assignments is required")
    @Min(value = 0, message = "Total assignments cannot be less than 0")
    private Integer totalAssignments;

    @Column(nullable = false)
    private Double ahi;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Boolean interventionRequired = false;

    private String interventionNotes;

    private LocalDateTime lastReviewedAt;

    @Column(nullable = false)
    private Boolean predictedRisk = false;

    @Column(nullable = false)
    private Double riskScore = 0.0;

    @Column(nullable = false)
    private String priorityLevel = "LOW";

    @Column(length = 2000)
    private String aiSummary;

    public AcademicRecord() {}

    public AcademicRecord(Student student,
                          Double attendancePercentage,
                          Double internalMarks,
                          Integer assignmentsSubmitted,
                          Integer totalAssignments) {
        this.student = student;
        this.attendancePercentage = attendancePercentage;
        this.internalMarks = internalMarks;
        this.assignmentsSubmitted = assignmentsSubmitted;
        this.totalAssignments = totalAssignments;
    }

    public Long getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public Double getInternalMarks() {
        return internalMarks;
    }

    public void setInternalMarks(Double internalMarks) {
        this.internalMarks = internalMarks;
    }

    public Integer getAssignmentsSubmitted() {
        return assignmentsSubmitted;
    }

    public void setAssignmentsSubmitted(Integer assignmentsSubmitted) {
        this.assignmentsSubmitted = assignmentsSubmitted;
    }

    public Integer getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(Integer totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public Double getAhi() {
        return ahi;
    }

    public void setAhi(Double ahi) {
        this.ahi = ahi;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getInterventionRequired() {
        return interventionRequired;
    }

    public void setInterventionRequired(Boolean interventionRequired) {
        this.interventionRequired = interventionRequired;
    }

    public String getInterventionNotes() {
        return interventionNotes;
    }

    public void setInterventionNotes(String interventionNotes) {
        this.interventionNotes = interventionNotes;
    }

    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(LocalDateTime lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public Boolean getPredictedRisk() {
        return predictedRisk;
    }

    public void setPredictedRisk(Boolean predictedRisk) {
        this.predictedRisk = predictedRisk;
    }

    public Double getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Double riskScore) {
        this.riskScore = riskScore;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }
}
