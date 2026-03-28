package com.college.academicmonitoring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.college.academicmonitoring.model.AcademicRecord;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, Long>, JpaSpecificationExecutor<AcademicRecord> {

    AcademicRecord findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);

    Optional<AcademicRecord> findById(Long id);

    List<AcademicRecord> findByStatus(String status);

    Page<AcademicRecord> findAll(Pageable pageable);

    @Query("SELECT AVG(ar.ahi) FROM AcademicRecord ar")
    Double findAverageAhi();
}
