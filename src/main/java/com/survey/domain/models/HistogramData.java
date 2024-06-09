package com.survey.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class HistogramData {
    private String title;
    private List<ChartDataPoint> series;

    public void increaseAnswerNumbers(String label){
        this.series.stream()
                .filter(chartDataPoint -> chartDataPoint.getLabel().equals(label))
                .findFirst()
                .ifPresent(chartDataPoint -> chartDataPoint.setValue(chartDataPoint.getValue() + 1));
    }
}
