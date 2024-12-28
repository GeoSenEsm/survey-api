package com.survey.application.services;


import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class FileValidationServiceImpl implements FileValidationService {
    private static final List<String> ALLOWED_FILE_EXTENSIONS = List.of(".jpg", ".jpeg", ".png");
    @Override
    public void validateFileType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file type: " + extension + ". Allowed types are " + ALLOWED_FILE_EXTENSIONS);
        }
    }

    static String getFileExtension(String fileName) {
        return fileName != null && fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf("."))
                : "";
    }
}
