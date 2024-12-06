package com.survey.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChartDataPoint {
    private Integer value;
    private String label;
}
