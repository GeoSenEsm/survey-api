package com.survey.domain.models.enums;


import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SurveyState {
    created(0),
    published(1);

    private final int value;

    SurveyState(int value) {this.value = value;}

    public static SurveyState fromValue(int value){
        return Arrays.stream(SurveyState.values())
                .filter(initialSurveyState -> initialSurveyState.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum value: " + value));
    }
}
