package com.college.academicmonitoring.service;

import com.college.academicmonitoring.dto.ChatResponse;

public interface ChatService {

    ChatResponse answerQuestion(String question);
}
