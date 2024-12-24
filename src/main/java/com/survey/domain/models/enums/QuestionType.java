package com.survey.domain.models.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum QuestionType {
    single_choice(0),
    linear_scale(1),
    yes_no_choice(2),
    multiple_choice(3),
    number_input(4),
    image_choice(5),
    text_input(6);

    private final int value;

    QuestionType(int value) {
        this.value = value;
    }

    public static QuestionType fromValue(int value) {
        return Arrays.stream(QuestionType.values())
                .filter(questionType -> questionType.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum value: " + value));
    }
}
