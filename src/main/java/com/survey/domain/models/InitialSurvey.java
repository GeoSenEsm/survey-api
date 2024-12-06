package com.survey.domain.models;

import com.survey.domain.models.enums.SurveyState;
import com.survey.domain.models.enums.SurveyStateConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "initial_survey")
public class InitialSurvey {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "initialSurvey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InitialSurveyQuestion> questions = new ArrayList<>();

    @Column(nullable = false)
    @Convert(converter = SurveyStateConverter.class)
    private SurveyState state;

    @Version
    @Column(name = "row_version", insertable = false, updatable = false)
    private byte[] rowVersion;
}
