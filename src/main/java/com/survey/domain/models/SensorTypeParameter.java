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

/**
 * A sensor type's own raw parameter catalog: what that sensor type can possibly produce,
 * independent of whether it has been wired into the study yet. Unlike
 * {@link SensorParameterDefinition}, {@code (name, unit)} is not globally unique here — the
 * same reading (e.g. "Temperature"/"C") can appear under several sensor types without
 * conflict. Setting {@link #usedParameter} is the explicit admin action ("use") that promotes
 * a raw candidate into the globally-unique "used sensor data" list.
 */
@Entity
@Table(name = "sensor_type_parameter")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorTypeParameter {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "sensor_type_id", nullable = false)
    private SensorType sensorType;

    @Column(name = "code", length = 64, nullable = false)
    private String code;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "data_type", length = 32, nullable = false)
    private String dataType;

    @Column(name = "unit", length = 32)
    private String unit;

    @ManyToOne
    @JoinColumn(name = "used_parameter_id")
    private SensorParameterDefinition usedParameter;
}
