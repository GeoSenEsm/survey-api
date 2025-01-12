package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
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

    @Column(nullable = false)
    private BigDecimal latitude;

    @Column(nullable = false)
    private BigDecimal longitude;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    private Boolean outsideResearchArea;
}
