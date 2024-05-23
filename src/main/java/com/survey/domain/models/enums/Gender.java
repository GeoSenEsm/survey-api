package com.survey.domain.models.enums;

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
        for (Gender gender : values()){
            if (gender.value == value){
                return gender;
            }
        }
        throw new IllegalArgumentException("Unknown enum value: " + value);
    }

}
