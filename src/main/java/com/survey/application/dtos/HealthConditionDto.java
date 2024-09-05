package com.survey.application.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class HealthConditionDto {
    @NotEmpty
    private Integer id;
    private String display;
    private Long rowVersion;

}
