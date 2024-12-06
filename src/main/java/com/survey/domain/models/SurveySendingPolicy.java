package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class SurveySendingPolicy {
    @Id
    @GeneratedValue(strategy =  GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id")
    private Survey survey;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    @OneToMany(mappedBy = "surveySendingPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyParticipationTimeSlot> timeSlots = new ArrayList<>();


}
