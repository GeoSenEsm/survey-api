package com.survey.domain.models;

import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.Visibility;
import com.survey.domain.models.enums.VisibilityConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "survey_section", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order", "survey_id"})
})
public class SurveySection {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "[order]", nullable = false)
    private Integer order;

    private String name;

    @ManyToOne
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(nullable = false)
    @Convert(converter = VisibilityConverter.class)
    private Visibility visibility;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions = new HashSet<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SectionToUserGroup> sectionToUserGroups = new HashSet<>();
}
