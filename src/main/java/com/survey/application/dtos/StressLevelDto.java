package com.survey.application.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StressLevelDto {
    @NotEmpty
    private Integer id;
    private String display;
    private Long rowVersion;
}
