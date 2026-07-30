package com.survey.infrastructure.mongo.repository;

import com.survey.infrastructure.mongo.documents.SurveyResponseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface SurveyResponseDocumentRepository
        extends MongoRepository<SurveyResponseDocument, UUID> {
}
