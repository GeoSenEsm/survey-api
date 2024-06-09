package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class HistogramDataDto {

    private String title;
    private List<ChartDataPointDto> series;
}
