package com.survey.application.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageServiceImpl implements StorageService {
    @Override
    public String store(MultipartFile file, String surveyName, String sectionOrder, String questionOrder, String optionOrder) throws IOException {
        String baseDirectory = "/uploads";
        Path directoryPath = Paths.get(baseDirectory, formatSurveyName(surveyName), "sections", sectionOrder, "questions", questionOrder, "options");

        Files.createDirectories(directoryPath);

        String fileName = optionOrder + getFileExtension(file);
        Path filePath = directoryPath.resolve(fileName);

        file.transferTo(filePath.toFile());

        return filePath.toString();
    }

    private String formatSurveyName(String name) {
        return name.trim().replaceAll(" ", "_");
    }
    private String getFileExtension(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        return originalFileName != null && originalFileName.contains(".")
                ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : "";
    }
}
