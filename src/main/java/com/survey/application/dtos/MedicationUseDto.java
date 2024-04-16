package com.survey.application.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedicationUseDto {
    @NotEmpty
    private Integer id;
    private String display;
    private Long rowVersion;
}
