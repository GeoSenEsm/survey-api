package com.survey.application.services;

import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SurveyParticipationTimeSlot;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Study schedule times are timezone-naive wall clocks stored on time slots
 * (the LocalDateTime face of {@code start}/{@code finish}). Instant math and
 * participation {@code local_date}/{@code local_time} use each respondent's
 * IANA timezone, defaulting to UTC when unset.
 */
public interface RespondentTimeZoneService {

    String DEFAULT_TIME_ZONE = "UTC";

    ZoneId resolveZoneId(IdentityUser user);

    ZoneId resolveZoneId(String timeZoneId);

    String normalizeOrDefault(String timeZoneId);

    OffsetDateTime toUtc(OffsetDateTime anyOffset);

    OffsetDateTime slotStartUtc(SurveyParticipationTimeSlot slot, ZoneId zone);

    OffsetDateTime slotFinishUtc(SurveyParticipationTimeSlot slot, ZoneId zone);

    record LocalParts(LocalDate date, java.time.LocalTime time) {}

    LocalParts toLocalParts(OffsetDateTime utcInstant, ZoneId zone);

    default OffsetDateTime nowUtc() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
