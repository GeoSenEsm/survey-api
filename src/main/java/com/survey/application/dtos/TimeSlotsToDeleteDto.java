package com.survey.application.dtos;

import com.survey.api.validation.ValidTimeSlotIds;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class TimeSlotsToDeleteDto {
    @ValidTimeSlotIds
    private List<UUID> ids;
}
