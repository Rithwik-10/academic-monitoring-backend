package com.college.academicmonitoring.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.college.academicmonitoring.model.User;
import com.college.academicmonitoring.repository.UserRepository;
import com.college.academicmonitoring.service.UserService;

@Component
public class UserDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserDataSeeder(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setRole("ADMIN");
            userService.saveUser(admin);
        }

        if (userRepository.findByUsername("faculty") == null) {
            User faculty = new User();
            faculty.setUsername("faculty");
            faculty.setPassword("faculty123");
            faculty.setRole("FACULTY");
            userService.saveUser(faculty);
        }
    }
}
