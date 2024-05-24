package com.survey.domain.models.enums;

import java.util.Arrays;

public enum QuestionType {
    single_text_selection(0),
    discrete_number_selection(1);

    private final int value;

    QuestionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static QuestionType fromValue(int value) {
        return Arrays.stream(QuestionType.values())
                .filter(questionType -> questionType.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum value: " + value));
    }
}
