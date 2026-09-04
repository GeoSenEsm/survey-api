package com.survey.domain.models;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class IdentityUser {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;
    private String username;
    private String passwordHash;
    private String role;
    /** Inclusive start of the respondent's study window, or null if unset. */
    private LocalDate surveyStartDate;
    /** Inclusive end of the respondent's study window, or null if unset. */
    private LocalDate surveyEndDate;
    /**
     * IANA timezone id used for study wall-clock slots and denormalized
     * participation local date/time. Defaults to {@code UTC} until the
     * respondent logs in from a device.
     */
    private String timeZone = "UTC";

    public boolean hasSurveyWindow() {
        return surveyStartDate != null && surveyEndDate != null;
    }

    public boolean isActiveOn(LocalDate day) {
        return hasSurveyWindow()
                && !day.isBefore(surveyStartDate)
                && !day.isAfter(surveyEndDate);
    }
}
