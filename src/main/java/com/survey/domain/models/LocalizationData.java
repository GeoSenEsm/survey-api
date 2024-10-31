package com.survey.domain.models;

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

    @OneToOne
    @JoinColumn(name = "participation_id")
    private SurveyParticipation surveyParticipation;

    @Column(nullable = false)
    private OffsetDateTime dateTime;

    @Column(nullable = false, columnDefinition = "GEOMETRY")
    private Point localization;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
