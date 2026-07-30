package com.survey.application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.survey.application.dtos.PagedResponseDto;
import com.survey.application.events.SurveyResponseSubmittedEvent;
import com.survey.infrastructure.mongo.documents.SurveyResponseDocument;
import com.survey.infrastructure.mongo.repository.SurveyResponseDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyResponseDocumentServiceImpl implements SurveyResponseDocumentService {

    private static final String PARTICIPATION_DATE_FIELD = "participationDate";

    private final SurveyResponseDocumentRepository repository;
    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSurveyResponseSubmitted(SurveyResponseSubmittedEvent event) {
        SurveyResponseDocument document = event.document();
        try {
            repository.save(document);
        } catch (RuntimeException ex) {
            log.warn("Failed to mirror survey response {} to MongoDB: {}",
                    document.getParticipationId(), ex.getMessage());
        }
    }

    @Override
    public PagedResponseDto<SurveyResponseDocument> find(
            UUID surveyId,
            UUID respondentId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            int page,
            int size
    ) {
        Query query = buildFilterQuery(surveyId, respondentId, dateFrom, dateTo);
        long total = mongoTemplate.count(query, SurveyResponseDocument.class);

        PageRequest pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, PARTICIPATION_DATE_FIELD));
        query.with(pageable);

        List<SurveyResponseDocument> content = mongoTemplate.find(query, SurveyResponseDocument.class);

        PagedResponseDto<SurveyResponseDocument> result = new PagedResponseDto<>();
        result.setContent(content);
        result.setPage(page);
        result.setSize(size);
        result.setTotalElements(total);
        result.setTotalPages(size == 0 ? 0 : (int) Math.ceil((double) total / size));
        return result;
    }

    @Override
    public Optional<SurveyResponseDocument> findById(UUID participationId) {
        return repository.findById(participationId);
    }

    @Override
    public void exportZip(
            UUID surveyId,
            UUID respondentId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            OutputStream out
    ) throws IOException {
        Query query = buildFilterQuery(surveyId, respondentId, dateFrom, dateTo)
                .with(Sort.by(Sort.Direction.DESC, PARTICIPATION_DATE_FIELD));

        try (ZipOutputStream zip = new ZipOutputStream(out);
             Stream<SurveyResponseDocument> cursor =
                     mongoTemplate.stream(query, SurveyResponseDocument.class)) {

            cursor.forEach(doc -> writeZipEntry(zip, doc));
        }
    }

    private void writeZipEntry(ZipOutputStream zip, SurveyResponseDocument doc) {
        try {
            byte[] json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(doc);
            zip.putNextEntry(new ZipEntry("survey-response-" + doc.getParticipationId() + ".json"));
            zip.write(json);
            zip.closeEntry();
        } catch (IOException ex) {
            throw new java.io.UncheckedIOException(ex);
        }
    }

    private Query buildFilterQuery(
            UUID surveyId,
            UUID respondentId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo
    ) {
        Query query = new Query();
        if (surveyId != null) {
            query.addCriteria(Criteria.where("surveyId").is(surveyId));
        }
        if (respondentId != null) {
            query.addCriteria(Criteria.where("respondentId").is(respondentId));
        }
        if (dateFrom != null || dateTo != null) {
            Criteria dateCriteria = Criteria.where(PARTICIPATION_DATE_FIELD);
            if (dateFrom != null) {
                dateCriteria = dateCriteria.gte(dateFrom);
            }
            if (dateTo != null) {
                dateCriteria = dateCriteria.lte(dateTo);
            }
            query.addCriteria(dateCriteria);
        }
        return query;
    }
}
