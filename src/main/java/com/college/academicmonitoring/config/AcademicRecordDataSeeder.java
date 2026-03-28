package com.college.academicmonitoring.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.college.academicmonitoring.model.AcademicRecord;
import com.college.academicmonitoring.model.Student;
import com.college.academicmonitoring.repository.AcademicRecordRepository;
import com.college.academicmonitoring.repository.StudentRepository;

@Component
public class AcademicRecordDataSeeder implements CommandLineRunner {

    private final AcademicRecordRepository academicRecordRepository;
    private final StudentRepository studentRepository;

    public AcademicRecordDataSeeder(AcademicRecordRepository academicRecordRepository, StudentRepository studentRepository) {
        this.academicRecordRepository = academicRecordRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) {
        List<Student> students = studentRepository.findAll();

        for (Student student : students) {
            if (!academicRecordRepository.existsByStudentId(student.getId())) {
                AcademicRecord record = buildRecord(student);
                academicRecordRepository.save(record);
            }
        }
    }

    private AcademicRecord buildRecord(Student student) {
        long seed = student.getId() != null ? student.getId() : 1L;

        double attendance = 55 + (seed * 7 % 41);
        double internalMarks = 50 + (seed * 9 % 46);
        int totalAssignments = 10;
        int assignmentsSubmitted = (int) Math.min(totalAssignments, 4 + (seed * 3 % 7));

        double submissionRatio = ((double) assignmentsSubmitted / totalAssignments) * 100;
        double ahi = (attendance * 0.4) + (internalMarks * 0.4) + (submissionRatio * 0.2);

        AcademicRecord record = new AcademicRecord();
        record.setStudent(student);
        record.setAttendancePercentage(attendance);
        record.setInternalMarks(internalMarks);
        record.setAssignmentsSubmitted(assignmentsSubmitted);
        record.setTotalAssignments(totalAssignments);
        record.setAhi(ahi);
        record.setStatus(classifyStatus(ahi));
        return record;
    }

    private String classifyStatus(double ahi) {
        if (ahi >= 75) {
            return "COMPLIANT";
        }
        if (ahi >= 50) {
            return "AT_RISK";
        }
        return "INTERVENTION_REQUIRED";
    }
}
