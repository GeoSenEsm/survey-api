package com.survey.domain.models;

import com.survey.domain.models.enums.SurveyState;
import com.survey.domain.models.enums.SurveyStateConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "survey")
@Accessors(chain = true)
public class Survey {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    @Convert(converter = SurveyStateConverter.class)
    private SurveyState state;

    @Column(name = "row_version", insertable = false, updatable = false)
    private byte[] rowVersion;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveySection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveySendingPolicy> policies = new ArrayList<>();

    @Column(name = "creation_date", nullable = false, updatable = false)
    private OffsetDateTime creationDate;

    @PrePersist
    public void generateUUID() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
