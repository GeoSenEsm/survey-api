package com.survey.application.services;

import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SurveyParticipation;
import com.survey.domain.repository.IdentityUserRepository;
import com.survey.domain.repository.SurveyParticipationRepository;
import com.survey.infrastructure.mongo.documents.SurveyResponseDocument;
import com.survey.infrastructure.mongo.repository.SurveyResponseDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalParticipationRecalculationServiceImpl implements LocalParticipationRecalculationService {

    private final IdentityUserRepository identityUserRepository;
    private final SurveyParticipationRepository surveyParticipationRepository;
    private final SurveyResponseDocumentRepository surveyResponseDocumentRepository;
    private final RespondentTimeZoneService respondentTimeZoneService;

    @Override
    @Transactional
    public void recalculateForRespondent(UUID respondentId) {
        IdentityUser user = identityUserRepository.findById(respondentId)
                .orElseThrow(() -> new NoSuchElementException("Respondent not found: " + respondentId));
        ZoneId zone = respondentTimeZoneService.resolveZoneId(user);

        List<SurveyParticipation> participations = surveyParticipationRepository.findAllByIdentityUser(user);
        for (SurveyParticipation participation : participations) {
            applyLocalParts(participation, zone);
        }
        surveyParticipationRepository.saveAll(participations);

        for (SurveyParticipation participation : participations) {
            updateMongoLocalParts(participation, zone);
        }
    }

    private void applyLocalParts(SurveyParticipation participation, ZoneId zone) {
        var parts = respondentTimeZoneService.toLocalParts(participation.getDate(), zone);
        participation.setLocalDate(parts.date());
        participation.setLocalTime(parts.time());
    }

    private void updateMongoLocalParts(SurveyParticipation participation, ZoneId zone) {
        Optional<SurveyResponseDocument> existing =
                surveyResponseDocumentRepository.findById(participation.getId());
        if (existing.isEmpty()) {
            return;
        }
        SurveyResponseDocument document = existing.get();
        var parts = respondentTimeZoneService.toLocalParts(participation.getDate(), zone);
        document.setLocalDate(parts.date());
        document.setLocalTime(parts.time());
        try {
            surveyResponseDocumentRepository.save(document);
        } catch (RuntimeException ex) {
            log.warn("Failed to update local date/time on Mongo document {}: {}",
                    participation.getId(), ex.getMessage());
        }
    }
}
