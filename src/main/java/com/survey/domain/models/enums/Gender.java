package com.survey.domain.models.enums;

import java.util.Arrays;

public enum Gender {
    male(1),
    female(2);

    private final int value;

    Gender(int value){
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Gender fromValue(int value){
        return Arrays.stream(Gender.values())
                .filter(gender -> gender.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown enum value: " + value));
    }

}
