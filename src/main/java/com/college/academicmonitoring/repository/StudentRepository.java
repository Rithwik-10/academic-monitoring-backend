package com.college.academicmonitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.college.academicmonitoring.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Student findByRollNumber(String rollNumber);

}