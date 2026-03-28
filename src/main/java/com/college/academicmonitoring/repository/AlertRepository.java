package com.college.academicmonitoring.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.college.academicmonitoring.model.Alert;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findTop20ByOrderByCreatedAtDesc();
}
