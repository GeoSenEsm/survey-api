package com.survey.application.services;

import com.survey.application.dtos.initialSurvey.CreateInitialSurveyQuestionDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyQuestionResponseDto;
import com.survey.application.dtos.initialSurvey.InitialSurveyStateDto;

import java.util.List;

public interface InitialSurveyService {
    List<InitialSurveyQuestionResponseDto> createInitialSurvey(List<CreateInitialSurveyQuestionDto> createInitialSurveyQuestionDtoList);
    List<InitialSurveyQuestionResponseDto> getInitialSurvey();
    InitialSurveyStateDto checkInitialSurveyState();
    void publishInitialSurveyAndCreateRespondentGroups();

    /**
     * Once the initial survey is published, respondent groups have already been created from it
     * and the study is considered live. Other services use this as the signal to stop accepting
     * changes that would otherwise apply retroactively to a running study (e.g. sensor data setup).
     */
    boolean isPublished();

    /**
     * Shared guard for every sensor-data-setup mutation (types, GATT profiles, template installs,
     * parameter definitions, and raw sensor type parameters): once the study is live, reshaping
     * what sensor data means would risk breaking in-flight BLE syncs or orphaning already-collected
     * data.
     */
    default void requireNotPublished() {
        if (isPublished()) {
            throw new IllegalStateException(
                    "Sensor data setup is locked: the initial survey has already been published.");
        }
    }
}