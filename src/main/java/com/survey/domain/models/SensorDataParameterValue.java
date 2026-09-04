package com.survey.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "sensor_data_parameter_value")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorDataParameterValue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "sensor_data_id", nullable = false)
    private SensorData sensorData;

    @ManyToOne
    @JoinColumn(name = "parameter_definition_id", nullable = false)
    private SensorParameterDefinition parameterDefinition;

    @Column(name = "value", length = 256, nullable = false)
    private String value;
}
