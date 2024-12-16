package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SurveyService {

    ResponseSurveyDto createSurvey(CreateSurveyDto createSurveyDto, List<MultipartFile> files);
    List<ResponseSurveyDto> getSurveysByCompletionDate(LocalDate completionDate);
    List<ResponseSurveyShortDto> getSurveysShort();
    List<ResponseSurveyShortSummariesDto> getSurveysShortSummaries();
    ResponseSurveyDto getSurveyById(UUID surveyId);
    List<ResponseSurveyWithTimeSlotsDto> getAllSurveysWithTimeSlots();
    void publishSurvey(UUID surveyId);
    void deleteSurvey(UUID surveyId);
    ResponseSurveyDto updateSurvey(UUID surveyId, CreateSurveyDto createSurveyDto, List<MultipartFile> files);
    boolean doesNewerDataExistsInDB(Long maxRowVersionFromMobileApp);

}
