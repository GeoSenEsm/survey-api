package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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

    @ManyToOne
    @JoinColumn(name = "source_sensor_type_id")
    private SensorType sourceSensorType;

    @Column(name = "source")
    private String source;

    @OneToMany(mappedBy = "sensorData", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SensorDataParameterValue> values = new ArrayList<>();

    // Many-to-one, not one-to-one: a single survey submission can carry a reading from each of
    // several connected sensor types, so more than one SensorData row can point at the same
    // participation (the FK column has never had a uniqueness constraint enforcing otherwise).
    @ManyToOne
    @JoinColumn(name = "survey_participation_id")
    private SurveyParticipation surveyParticipation;
}
