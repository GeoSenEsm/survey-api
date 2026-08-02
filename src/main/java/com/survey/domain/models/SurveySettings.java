package com.survey.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "survey_settings")
public class SurveySettings {
    @Id
    private Integer id;

    @Column(name = "show_sending_policy_calendar", nullable = false)
    private boolean showSendingPolicyCalendar;

    @Column(name = "csv_column_separator", nullable = false, length = 1)
    private String csvColumnSeparator;

    @Column(name = "csv_decimal_separator", nullable = false, length = 1)
    private String csvDecimalSeparator;
}
