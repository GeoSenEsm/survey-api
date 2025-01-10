package com.survey.application.dtos;

import com.survey.api.validation.ValidTimeSlotIds;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Contains a list of survey participation time slot ids that are to be soft deleted.")
public class TimeSlotsToDeleteDto {
    @ValidTimeSlotIds
    private List<UUID> ids;
}
