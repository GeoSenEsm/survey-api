package com.survey.application.services;

import com.survey.domain.models.IdentityUser;
import com.survey.domain.models.SurveyParticipationTimeSlot;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service
public class RespondentTimeZoneServiceImpl implements RespondentTimeZoneService {

    @Override
    public ZoneId resolveZoneId(IdentityUser user) {
        if (user == null) {
            return ZoneOffset.UTC;
        }
        return resolveZoneId(user.getTimeZone());
    }

    @Override
    public ZoneId resolveZoneId(String timeZoneId) {
        return ZoneId.of(normalizeOrDefault(timeZoneId));
    }

    @Override
    public String normalizeOrDefault(String timeZoneId) {
        if (timeZoneId == null || timeZoneId.isBlank()) {
            return DEFAULT_TIME_ZONE;
        }
        String trimmed = timeZoneId.trim();
        if (!isValidTimeZone(trimmed)) {
            throw new IllegalArgumentException("Invalid IANA time zone: " + trimmed);
        }
        return ZoneId.of(trimmed).getId();
    }

    private boolean isValidTimeZone(String timeZoneId) {
        if (timeZoneId == null || timeZoneId.isBlank()) {
            return false;
        }
        try {
            ZoneId.of(timeZoneId.trim());
            return true;
        } catch (DateTimeException ex) {
            return false;
        }
    }

    @Override
    public OffsetDateTime toUtc(OffsetDateTime anyOffset) {
        return anyOffset.withOffsetSameInstant(ZoneOffset.UTC);
    }

    private OffsetDateTime wallClockToUtc(LocalDateTime wallClock, ZoneId zone) {
        return wallClock.atZone(zone).toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
    }

    @Override
    public OffsetDateTime slotStartUtc(SurveyParticipationTimeSlot slot, ZoneId zone) {
        return wallClockToUtc(slotWallStart(slot), zone);
    }

    @Override
    public OffsetDateTime slotFinishUtc(SurveyParticipationTimeSlot slot, ZoneId zone) {
        return wallClockToUtc(slotWallFinish(slot), zone);
    }

    private LocalDateTime slotWallStart(SurveyParticipationTimeSlot slot) {
        return slot.getStart().toLocalDateTime();
    }

    private LocalDateTime slotWallFinish(SurveyParticipationTimeSlot slot) {
        return slot.getFinish().toLocalDateTime();
    }

    @Override
    public LocalParts toLocalParts(OffsetDateTime utcInstant, ZoneId zone) {
        var zoned = utcInstant.atZoneSameInstant(zone);
        return new LocalParts(zoned.toLocalDate(), zoned.toLocalTime().withNano(0));
    }
}
