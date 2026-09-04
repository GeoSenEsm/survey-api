package com.survey.domain.models;

import com.survey.domain.models.enums.NotificationRelativeTo;
import com.survey.domain.models.enums.NotificationRelativeToConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "survey_notification")
public class SurveyNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(name = "[order]", nullable = false)
    private int order;

    @Column(name = "relative_to", nullable = false)
    @Convert(converter = NotificationRelativeToConverter.class)
    private NotificationRelativeTo relativeTo;

    @Column(name = "minutes_before", nullable = false)
    private int minutesBefore;

    @Column(name = "row_version", insertable = false, updatable = false)
    private byte[] rowVersion;
}
