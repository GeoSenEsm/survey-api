package com.survey.application.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.Comparator;

import static com.survey.application.services.FileValidationServiceImpl.getFileExtension;


@Service
public class StorageServiceImpl implements StorageService {
    private static final String BASE_DIRECTORY = "/uploads";
    private final FileValidationService fileValidationService;

    public StorageServiceImpl(FileValidationService fileValidationService) {
        this.fileValidationService = fileValidationService;
    }

    @Override
    public String store(MultipartFile file, String surveyName, String sectionOrder, String questionOrder, String optionOrder) throws IOException {
        fileValidationService.validateFileType(file.getOriginalFilename());

        Path directoryPath = Paths.get(BASE_DIRECTORY, formatSurveyName(surveyName), "sections", sectionOrder, "questions", questionOrder, "options");

        Files.createDirectories(directoryPath);

        String fileName = optionOrder + getFileExtension(file.getOriginalFilename());
        Path filePath = directoryPath.resolve(fileName);

        file.transferTo(filePath.toFile());

        return filePath.toString();
    }

    @Override
    public void deleteSurveyImages(String surveyName) {
        Path surveyDirectory = Paths.get(BASE_DIRECTORY, formatSurveyName(surveyName));

        if (!Files.exists(surveyDirectory)) {
            return;
        }

        if (!Files.isDirectory(surveyDirectory)) {
            throw new IllegalStateException("Path is not a directory: " + surveyDirectory);
        }

        try (var paths = Files.walk(surveyDirectory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new IllegalStateException("Failed to delete path: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalStateException("Error while deleting survey directory: " + surveyDirectory, e);
        }
    }

    private String formatSurveyName(String name) {
        return Normalizer.normalize(name.trim(), Normalizer.Form.NFKC)
                .replaceAll(" ", "_");
    }

}
