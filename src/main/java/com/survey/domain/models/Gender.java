package com.survey.domain.models;

public enum Gender {
    male(1),
    female(2);

    private final int id;

    Gender(int id){
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
