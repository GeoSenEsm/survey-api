package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SensorData {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "respondent_id", nullable = false)
    private IdentityUser respondent;

    @Column(name = "date_time")
    private OffsetDateTime dateTime;

    private BigDecimal temperature;

    private BigDecimal humidity;

    @OneToOne
    @JoinColumn(name = "survey_participation_id")
    private SurveyParticipation surveyParticipation;
}
