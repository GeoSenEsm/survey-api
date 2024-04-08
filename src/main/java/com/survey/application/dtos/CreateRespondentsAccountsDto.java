package com.survey.application.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRespondentsAccountsDto {
    @Min(1)
    @Max(400)
    private int amount;
}
