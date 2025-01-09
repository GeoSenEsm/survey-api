package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class SurveySendingPolicyTimesDto {
    private UUID id;
    private OffsetDateTime start;
    private OffsetDateTime finish;
    private boolean isDeleted;

    @Schema(example = "2001")
    private Long rowVersion;
}
