package com.survey.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public class SurveySendingPolicyTimesDto {

    @Schema(description = "UUID of the time slot.")
    private UUID id;

    @Schema(description = "Date time in UTC when given time slot starts.")
    private OffsetDateTime start;

    @Schema(description = "Date time in UTC when given time slot ends.")
    private OffsetDateTime finish;

    @Schema(description = "Flag for soft delete.",
            example = "false")
    private boolean isDeleted;

    @Schema(example = "2001")
    private Long rowVersion;
}
