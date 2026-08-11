package com.survey.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SensorMac {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uniqueidentifier", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "sensor_id", length = 16, nullable = false, unique = true)
    private String sensorId;

    @Column(name = "sensor_mac", length = 17)
    private String sensorMac;

    @Column(name = "respondent_id", columnDefinition = "uniqueidentifier")
    private UUID respondentId;

    @Column(name = "sensor_type_id", columnDefinition = "uniqueidentifier", nullable = false)
    private UUID sensorTypeId;

    @Version
    @Column(name = "row_version", insertable = false, updatable = false)
    private byte[] rowVersion;
}
