package com.survey.domain.models;

import com.survey.domain.models.enums.QuestionType;
import com.survey.domain.models.enums.QuestionTypeConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "question", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order", "section_id",}),
        @UniqueConstraint(columnNames = {"label", "section_id"})
})
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "[order]", nullable = false)
    private Integer order;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private SurveySection section;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Convert(converter = QuestionTypeConverter.class)
    private QuestionType questionType;

    @Column(nullable = false)
    private Boolean required;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> options = new ArrayList<>();

}
