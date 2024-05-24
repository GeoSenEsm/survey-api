package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class SurveyParticipationTimeSlot {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    private OffsetDateTime start;
    private OffsetDateTime finish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_sending_policy_id")
    private SurveySendingPolicy surveySendingPolicy;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

}
