package com.college.academicmonitoring.service;

import java.util.List;
import com.college.academicmonitoring.model.Student;

public interface StudentService {

    Student saveStudent(Student student);

    List<Student> getAllStudents();

    Student getByRollNumber(String rollNumber);

    void deleteStudent(Long id);
}