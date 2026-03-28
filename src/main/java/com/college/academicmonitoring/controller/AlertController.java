package com.college.academicmonitoring.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.college.academicmonitoring.model.Alert;
import com.college.academicmonitoring.service.AcademicRecordService;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AcademicRecordService academicRecordService;

    @GetMapping
    public List<Alert> getRecentAlerts() {
        return academicRecordService.getRecentAlerts();
    }
}
