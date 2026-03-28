package com.college.academicmonitoring.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.college.academicmonitoring.model.Student;
import com.college.academicmonitoring.repository.StudentRepository;

@Component
public class StudentDataSeeder implements CommandLineRunner {

    private final StudentRepository studentRepository;

    public StudentDataSeeder(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) {
        List<Student> sampleStudents = buildSampleStudents();
        List<Student> missingStudents = new ArrayList<>();

        for (Student student : sampleStudents) {
            if (studentRepository.findByRollNumber(student.getRollNumber()) == null) {
                missingStudents.add(student);
            }
        }

        if (!missingStudents.isEmpty()) {
            studentRepository.saveAll(missingStudents);
        }
    }

    private List<Student> buildSampleStudents() {
        List<Student> students = new ArrayList<>();

        students.add(createStudent("22CS002", "Aarav Kumar", "CSE", 2, "A"));
        students.add(createStudent("22CS003", "Diya Sharma", "CSE", 2, "A"));
        students.add(createStudent("22CS004", "Ishaan Reddy", "CSE", 2, "A"));
        students.add(createStudent("22CS005", "Meera Nair", "CSE", 2, "A"));
        students.add(createStudent("22CS006", "Rohan Gupta", "CSE", 2, "A"));
        students.add(createStudent("22CS007", "Ananya Das", "CSE", 2, "A"));
        students.add(createStudent("22CS008", "Vikram Patel", "CSE", 2, "A"));
        students.add(createStudent("22CS009", "Sneha Iyer", "CSE", 2, "A"));
        students.add(createStudent("22CS010", "Arjun Singh", "CSE", 2, "A"));
        students.add(createStudent("22CS011", "Pooja Menon", "CSE", 2, "B"));
        students.add(createStudent("22CS012", "Karthik Raj", "CSE", 2, "B"));
        students.add(createStudent("22CS013", "Nisha Verma", "CSE", 2, "B"));
        students.add(createStudent("22CS014", "Rahul Yadav", "CSE", 2, "B"));
        students.add(createStudent("22CS015", "Lavanya Sri", "CSE", 2, "B"));
        students.add(createStudent("22CS016", "Harsha Vardhan", "CSE", 2, "B"));
        students.add(createStudent("22CS017", "Priya Nandini", "CSE", 2, "B"));
        students.add(createStudent("22CS018", "Manoj Kumar", "CSE", 2, "B"));
        students.add(createStudent("22CS019", "Keerthana P", "CSE", 2, "B"));
        students.add(createStudent("22CS020", "Sanjay R", "CSE", 2, "B"));

        students.add(createStudent("22IT021", "Akhil Varma", "IT", 2, "A"));
        students.add(createStudent("22IT022", "Neha Kapoor", "IT", 2, "A"));
        students.add(createStudent("22IT023", "Sai Teja", "IT", 2, "A"));
        students.add(createStudent("22IT024", "Anjali Rao", "IT", 2, "A"));
        students.add(createStudent("22IT025", "Ritesh Jain", "IT", 2, "A"));
        students.add(createStudent("22IT026", "Bhavya S", "IT", 2, "A"));
        students.add(createStudent("22IT027", "Tarun K", "IT", 2, "A"));
        students.add(createStudent("22IT028", "Madhavi L", "IT", 2, "A"));
        students.add(createStudent("22IT029", "Yash Mehta", "IT", 2, "A"));
        students.add(createStudent("22IT030", "Divya Rani", "IT", 2, "A"));
        students.add(createStudent("22IT031", "Lokesh Babu", "IT", 2, "B"));
        students.add(createStudent("22IT032", "Deepika P", "IT", 2, "B"));
        students.add(createStudent("22IT033", "Nitin S", "IT", 2, "B"));
        students.add(createStudent("22IT034", "Shreya Kulkarni", "IT", 2, "B"));
        students.add(createStudent("22IT035", "Aditya Rao", "IT", 2, "B"));
        students.add(createStudent("22IT036", "Varsha M", "IT", 2, "B"));
        students.add(createStudent("22IT037", "Tejaswini K", "IT", 2, "B"));
        students.add(createStudent("22IT038", "Gokul N", "IT", 2, "B"));
        students.add(createStudent("22IT039", "Monika Das", "IT", 2, "B"));
        students.add(createStudent("22IT040", "Surya Prakash", "IT", 2, "B"));

        students.add(createStudent("22EC041", "Aditi Sen", "ECE", 2, "A"));
        students.add(createStudent("22EC042", "Naveen Kumar", "ECE", 2, "A"));
        students.add(createStudent("22EC043", "Harini Devi", "ECE", 2, "A"));
        students.add(createStudent("22EC044", "Pranav S", "ECE", 2, "A"));
        students.add(createStudent("22EC045", "Swathi R", "ECE", 2, "A"));
        students.add(createStudent("22EC046", "Kiran B", "ECE", 2, "A"));
        students.add(createStudent("22EC047", "Mohan Krishna", "ECE", 2, "A"));
        students.add(createStudent("22EC048", "Pallavi G", "ECE", 2, "A"));
        students.add(createStudent("22EC049", "Rachana N", "ECE", 2, "A"));
        students.add(createStudent("22EC050", "Sathvik Reddy", "ECE", 2, "A"));
        students.add(createStudent("22EC051", "Akash M", "ECE", 2, "B"));
        students.add(createStudent("22EC052", "Bhavana T", "ECE", 2, "B"));
        students.add(createStudent("22EC053", "Charan P", "ECE", 2, "B"));
        students.add(createStudent("22EC054", "Dhanya S", "ECE", 2, "B"));
        students.add(createStudent("22EC055", "Eshan Gupta", "ECE", 2, "B"));
        students.add(createStudent("22EC056", "Fathima Noor", "ECE", 2, "B"));
        students.add(createStudent("22EC057", "Ganesh V", "ECE", 2, "B"));
        students.add(createStudent("22EC058", "Hema Priya", "ECE", 2, "B"));
        students.add(createStudent("22EC059", "Jeevan K", "ECE", 2, "B"));
        students.add(createStudent("22EC060", "Kavya Sri", "ECE", 2, "B"));
        students.add(createStudent("22EC061", "Likitha R", "ECE", 2, "B"));

        return students;
    }

    private Student createStudent(String rollNumber, String name, String department, Integer year, String section) {
        Student student = new Student();
        student.setRollNumber(rollNumber);
        student.setName(name);
        student.setDepartment(department);
        student.setYear(year);
        student.setSection(section);
        return student;
    }
}
