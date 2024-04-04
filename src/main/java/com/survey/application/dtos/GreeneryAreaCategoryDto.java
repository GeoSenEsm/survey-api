package com.survey.application.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.sql.Time;
import java.sql.Timestamp;

@Getter
@Setter
public class GreeneryAreaCategoryDto {
    @NotEmpty
    private Integer id;
    private String display;
    private String rowVersion;

}
