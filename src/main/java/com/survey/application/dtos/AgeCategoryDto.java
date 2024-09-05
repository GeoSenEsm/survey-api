package com.survey.application.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.sql.Time;
import java.sql.Timestamp;

@Getter
@Setter
@Accessors(chain = true)
public class AgeCategoryDto {
    private Integer id;
    private String display;
    private Long rowVersion;
}
