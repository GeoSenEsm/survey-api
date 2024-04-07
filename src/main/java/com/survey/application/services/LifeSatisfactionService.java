package com.survey.application.services;


import com.survey.application.dtos.LifeSatisfactionDto;

import java.util.List;

public interface LifeSatisfactionService {
    List<LifeSatisfactionDto> getAllLifeSatisfactionValues();
}
