package com.college.academicmonitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.college.academicmonitoring.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

}
