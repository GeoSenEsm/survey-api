package com.survey.application.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GreeneryDto {
    @NotEmpty
    private String display;
}
