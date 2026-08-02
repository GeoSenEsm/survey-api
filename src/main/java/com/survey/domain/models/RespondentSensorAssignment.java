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
@Table(name = "respondent_sensor_assignment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RespondentSensorAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "respondent_id", nullable = false)
    private IdentityUser respondent;

    @ManyToOne
    @JoinColumn(name = "sensor_type_id", nullable = false)
    private SensorType sensorType;

    @ManyToOne
    @JoinColumn(name = "sensor_mac_id")
    private SensorMac sensorMac;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "priority_order", nullable = false)
    private int priorityOrder;
}
