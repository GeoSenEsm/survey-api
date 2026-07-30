package com.survey.application.services;

import com.survey.application.dtos.PagedResponseDto;
import com.survey.infrastructure.mongo.documents.SurveyResponseDocument;

import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface SurveyResponseDocumentService {

    PagedResponseDto<SurveyResponseDocument> find(
            UUID surveyId,
            UUID respondentId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            int page,
            int size
    );

    Optional<SurveyResponseDocument> findById(UUID participationId);

    /**
     * Streams every document matching the filters as a ZIP archive to
     * {@code out}. Each entry is a pretty-printed JSON document named
     * {@code survey-response-<participationId>.json}.
     */
    void exportZip(
            UUID surveyId,
            UUID respondentId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            OutputStream out
    ) throws java.io.IOException;
}
