package com.college.academicmonitoring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.academicmonitoring.model.AcademicRecordHistory;

public interface AcademicRecordHistoryRepository extends JpaRepository<AcademicRecordHistory, Long> {

    List<AcademicRecordHistory> findByStudentIdOrderByRecordedAtAsc(Long studentId);
}
