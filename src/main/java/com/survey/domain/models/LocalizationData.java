package com.survey.domain.models;

import com.survey.api.configuration.PointConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Point;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
public class LocalizationData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "respondent_id", nullable = false)
    private IdentityUser identityUser;

    @ManyToOne
    @JoinColumn(name = "participation_id")
    private SurveyParticipation surveyParticipation;

    @Column(nullable = false)
    private OffsetDateTime dateTime;

    @Convert(converter = PointConverter.class)
    @Column(nullable = false, columnDefinition = "GEOGRAPHY")
    private Point localization;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
