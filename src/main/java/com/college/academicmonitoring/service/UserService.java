package com.college.academicmonitoring.service;
import java.util.List;
import com.college.academicmonitoring.model.User;

public interface UserService {

    User saveUser(User user);

    User findByUsername(String username);

    List<User> getAllUsers();
}
