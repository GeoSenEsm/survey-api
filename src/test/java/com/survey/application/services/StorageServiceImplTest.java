package com.survey.application.services;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageServiceImplTest {

    @Test
    void toUrlPath_normalizesWindowsSeparators() {
        assertThat(StorageServiceImpl.toUrlPath(Paths.get("\\uploads\\survey_settings\\logo.png")))
                .isEqualTo("/uploads/survey_settings/logo.png");
    }

    @Test
    void validateFileType_rejectsUnsupportedLogoExtension() {
        FileValidationServiceImpl validationService = new FileValidationServiceImpl();

        assertThatThrownBy(() -> validationService.validateFileType("logo.gif"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file type");
    }
}
