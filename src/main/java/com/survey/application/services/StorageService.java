package com.survey.application.services;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {
    String store(MultipartFile file, String surveyName, String sectionOrder, String questionOrder, String optionOrder) throws IOException;
    void deleteSurveyImages(String surveyName);
}
