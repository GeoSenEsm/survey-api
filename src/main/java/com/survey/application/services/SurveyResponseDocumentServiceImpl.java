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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
    private static final String LOCAL_DATE_FIELD = "localDate";
    private static final String LOCAL_TIME_FIELD = "localTime";

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
            query.addCriteria(localDateTimeRangeCriteria(dateFrom, dateTo));
        }
        return query;
    }

    /**
     * Study wall-clock filter against denormalized {@code localDate}/{@code localTime}.
     * {@code dateFrom}/{@code dateTo} LocalDateTime faces are the bounds.
     */
    private static Criteria localDateTimeRangeCriteria(OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        List<Criteria> parts = new ArrayList<>();
        if (dateFrom != null) {
            LocalDateTime from = dateFrom.toLocalDateTime();
            LocalDate fromDate = from.toLocalDate();
            LocalTime fromTime = from.toLocalTime().withNano(0);
            parts.add(new Criteria().orOperator(
                    Criteria.where(LOCAL_DATE_FIELD).gt(fromDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)),
                    new Criteria().andOperator(
                            Criteria.where(LOCAL_DATE_FIELD).is(fromDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)),
                            Criteria.where(LOCAL_TIME_FIELD).gte(fromTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME))
                    )
            ));
        }
        if (dateTo != null) {
            LocalDateTime to = dateTo.toLocalDateTime();
            LocalDate toDate = to.toLocalDate();
            LocalTime toTime = to.toLocalTime().withNano(0);
            parts.add(new Criteria().orOperator(
                    Criteria.where(LOCAL_DATE_FIELD).lt(toDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)),
                    new Criteria().andOperator(
                            Criteria.where(LOCAL_DATE_FIELD).is(toDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)),
                            Criteria.where(LOCAL_TIME_FIELD).lte(toTime.format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME))
                    )
            ));
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        return new Criteria().andOperator(parts.toArray(Criteria[]::new));
    }
}
