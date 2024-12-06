package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "section_to_user_group")
public class SectionToUserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "section_id", nullable = false)
    private SurveySection section;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private RespondentGroup group;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;
}
