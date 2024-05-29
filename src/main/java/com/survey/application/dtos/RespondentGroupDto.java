package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RespondentGroupDto {
    private UUID id;
    private String name;

}
