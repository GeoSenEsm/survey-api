package com.survey.application.services;

import java.util.UUID;

/**
 * Recomputes denormalized {@code local_date}/{@code local_time} on SQL
 * participations and matching Mongo response documents after a respondent
 * timezone change (typically on login).
 */
public interface LocalParticipationRecalculationService {

    void recalculateForRespondent(UUID respondentId);
}
