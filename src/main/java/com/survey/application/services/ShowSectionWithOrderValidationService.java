package com.survey.application.services;

import java.util.Dictionary;

public interface ShowSectionWithOrderValidationService {
    void validateSectionExistsAndIsAnswerTriggered(Integer showSectionOrder, Dictionary<Integer, String> sectionOrderAndVisibilityDict, Integer currentSectionOrder);
}
