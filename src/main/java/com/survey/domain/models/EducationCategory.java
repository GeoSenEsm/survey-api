package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class EducationCategory {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Integer id;
    private String display;
    @Column(name = "row_version", insertable = false)
    private byte[] rowVersion;

    public EducationCategory(String display) {
        this.display = display;
    }
}
