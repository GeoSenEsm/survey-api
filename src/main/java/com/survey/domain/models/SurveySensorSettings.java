package com.survey.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "survey_sensor_settings")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SurveySensorSettings {
    @Id
    private Integer id;

    @Column(name = "mode", length = 32, nullable = false)
    private String mode;
}
