package com.survey.application.services;

import com.survey.application.dtos.MedicationUseDto;

import java.util.List;

public interface MedicationUseService {
    List<MedicationUseDto> getMedicationUse();
}
