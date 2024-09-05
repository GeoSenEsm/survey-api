package com.survey.application.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class OccupationCategoryDto {
    private Integer id;
    private String display;
    private Long rowVersion;
}
