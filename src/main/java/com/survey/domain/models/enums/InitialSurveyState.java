package com.survey.domain.models.enums;


import lombok.Getter;

import java.util.Arrays;

@Getter
public enum InitialSurveyState {
    created(0),
    published(1);

    private final int value;

    InitialSurveyState(int value) {this.value = value;}

    public static InitialSurveyState fromValue(int value){
        return Arrays.stream(InitialSurveyState.values())
                .filter(initialSurveyState -> initialSurveyState.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum value: " + value));
    }
}
