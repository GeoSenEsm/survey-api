package com.survey.application.services;

import com.survey.application.dtos.CreateSurveySendingPolicyDto;
import com.survey.application.dtos.SurveyParticipationTimeStartFinishDto;
import com.survey.application.dtos.SurveySendingPolicyDto;
import com.survey.domain.models.*;
import com.survey.domain.repository.SurveyParticipationTimeSlotRepository;
import com.survey.domain.repository.SurveyRepository;
import com.survey.domain.repository.SurveySendingPolicyRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.management.InstanceAlreadyExistsException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import java.util.UUID;


@Service
public class SurveySendingPolicyServiceImpl implements SurveySendingPolicyService {
    private final SurveySendingPolicyRepository surveySendingPolicyRepository;
    private final SurveyParticipationTimeSlotRepository surveyParticipationTimeSlotRepository;
    private final SurveyRepository surveyRepository;
    private final ModelMapper modelMapper;
    @PersistenceContext
    private final EntityManager entityManager;

    @Autowired
    public SurveySendingPolicyServiceImpl(SurveySendingPolicyRepository surveySendingPolicyRepository,
                                          SurveyParticipationTimeSlotRepository surveyParticipationTimeSlotRepository,
                                          SurveyRepository surveyRepository,
                                          ModelMapper modelMapper,
                                          EntityManager entityManager) {
        this.surveySendingPolicyRepository = surveySendingPolicyRepository;
        this.surveyParticipationTimeSlotRepository = surveyParticipationTimeSlotRepository;
        this.surveyRepository = surveyRepository;
        this.modelMapper = modelMapper;
        this.entityManager = entityManager;
    }

    private boolean doesSurveySendingPolicyExist(UUID surveyId) {
        return surveySendingPolicyRepository.existsInSurveySendingPolicyBySurveyId(surveyId);
    }

    private boolean doesSurveyExist(UUID surveyId) {
        return surveyRepository.existsInSurveyById(surveyId);
    }

    private void validateTimeSlots(List<SurveyParticipationTimeStartFinishDto> timeSlotList) {
        for (SurveyParticipationTimeStartFinishDto timeSlotDto : timeSlotList) {
            OffsetDateTime start = timeSlotDto.getStart();
            OffsetDateTime finish = timeSlotDto.getFinish();

            if (start.isAfter(finish)) {
                throw new IllegalArgumentException("Start time must be before finish time.");
            }
        }
    }

    @Override
    @Transactional
    public SurveySendingPolicyDto addSurveySendingPolicy(CreateSurveySendingPolicyDto createSurveySendingPolicyDto) throws InstanceAlreadyExistsException, NoSuchElementException,  IllegalArgumentException {

        UUID currentSurveyUUID = createSurveySendingPolicyDto.getSurveyId();

        if (doesSurveySendingPolicyExist(currentSurveyUUID)) {
            throw new InstanceAlreadyExistsException("Survey sending policy already exists for this survey.");
        }

        if (!doesSurveyExist(currentSurveyUUID)) {
            throw new NoSuchElementException("Survey doesn't exist.");
        }

        List<SurveyParticipationTimeStartFinishDto> timeSlotList = createSurveySendingPolicyDto.getSurveyParticipationTimeSlots();
        validateTimeSlots(timeSlotList);

        Survey survey = surveyRepository.getSurveyById(currentSurveyUUID);
        SurveySendingPolicy surveySendingPolicy = new SurveySendingPolicy(survey);
        SurveySendingPolicy saveSurveySendingPolicy = surveySendingPolicyRepository.saveAndFlush(surveySendingPolicy);
        entityManager.refresh(saveSurveySendingPolicy);


        for (SurveyParticipationTimeStartFinishDto timeSlotDto : timeSlotList) {
            OffsetDateTime start = timeSlotDto.getStart();
            OffsetDateTime finish = timeSlotDto.getFinish();

            SurveyParticipationTimeSlot timeSlot = new SurveyParticipationTimeSlot(start, finish, saveSurveySendingPolicy);
            surveyParticipationTimeSlotRepository.save(timeSlot);
        }

        SurveySendingPolicyDto surveySendingPolicyDto = modelMapper.map(saveSurveySendingPolicy, SurveySendingPolicyDto.class);
        surveySendingPolicyDto.setSurveyId(currentSurveyUUID);

        return surveySendingPolicyDto;

    }
}