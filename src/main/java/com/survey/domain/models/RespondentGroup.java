package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "respondents_group")
@Accessors(chain = true)
public class RespondentGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    @ManyToMany(mappedBy = "respondentGroups", fetch = FetchType.LAZY)
    private Set<RespondentData> respondentData = new HashSet<>();
}
