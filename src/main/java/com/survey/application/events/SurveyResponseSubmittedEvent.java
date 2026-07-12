package com.survey.application.events;

import com.survey.infrastructure.mongo.documents.SurveyResponseDocument;

/**
 * Published by {@code SurveyResponsesServiceImpl} after each survey
 * participation is saved to SQL. Handled after commit so failures in the
 * Mongo mirror can never roll back the primary SQL write.
 */
public record SurveyResponseSubmittedEvent(SurveyResponseDocument document) {
}
