package com.survey.domain.models;

import com.survey.domain.models.enums.Visibility;
import com.survey.domain.models.enums.VisibilityConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Data
@NoArgsConstructor
@Table(name = "survey_section", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"order", "survey_id"})
})
public class SurveySection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SectionToUserGroup> sectionToUserGroups = new ArrayList<>();


/*    This method is ok for now, because each survey section can have only one groupId
    when section visibility is "group_specific"*/
    public UUID getGroupId() {
        return sectionToUserGroups.isEmpty() ?
                null : sectionToUserGroups.iterator().next().getGroup().getId();
    }

}
