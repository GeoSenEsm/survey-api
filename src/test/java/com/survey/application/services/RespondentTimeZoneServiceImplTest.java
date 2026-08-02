package com.survey.application.services;

import com.survey.domain.models.SurveyParticipationTimeSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RespondentTimeZoneServiceImplTest {

    private final RespondentTimeZoneServiceImpl service = new RespondentTimeZoneServiceImpl();

    @Test
    void slotStartUtc_InterpretsClockFaceInRespondentZone() {
        SurveyParticipationTimeSlot slot = new SurveyParticipationTimeSlot();
        slot.setStart(OffsetDateTime.of(2026, 8, 3, 13, 0, 0, 0, ZoneOffset.UTC));

        OffsetDateTime result = service.slotStartUtc(slot, ZoneId.of("Europe/Warsaw"));

        assertEquals(OffsetDateTime.of(2026, 8, 3, 11, 0, 0, 0, ZoneOffset.UTC), result);
    }

    @Test
    void toLocalParts_UsesRespondentZone() {
        RespondentTimeZoneService.LocalParts result = service.toLocalParts(
                OffsetDateTime.of(2026, 8, 3, 23, 30, 0, 0, ZoneOffset.UTC),
                ZoneId.of("Europe/Warsaw"));

        assertEquals(LocalDate.of(2026, 8, 4), result.date());
        assertEquals(0, result.time().getHour());
        assertEquals(30, result.time().getMinute());
    }

    @Test
    void slotStartUtc_UsesStoredSlotClockFace() {
        SurveyParticipationTimeSlot slot = new SurveyParticipationTimeSlot();
        slot.setStart(OffsetDateTime.of(2026, 8, 3, 13, 0, 0, 0, ZoneOffset.UTC));

        OffsetDateTime result = service.slotStartUtc(slot, ZoneId.of("America/New_York"));

        assertEquals(OffsetDateTime.of(2026, 8, 3, 17, 0, 0, 0, ZoneOffset.UTC), result);
    }

    @Test
    void normalizeOrDefault_RejectsInvalidZone() {
        assertThrows(IllegalArgumentException.class, () -> service.normalizeOrDefault("Not/AZone"));
    }
}
