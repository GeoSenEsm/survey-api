package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PhoneNumberDtoOut {
    private UUID id;
    private String name;
    private String number;
}
