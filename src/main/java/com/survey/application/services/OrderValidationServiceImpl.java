package com.survey.application.services;

import com.survey.application.dtos.surveyDtos.CreateOptionDto;
import com.survey.application.dtos.surveyDtos.CreateQuestionDto;
import com.survey.application.dtos.surveyDtos.CreateSurveyDto;
import com.survey.application.dtos.surveyDtos.CreateSurveySectionDto;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderValidationServiceImpl implements OrderValidationService{

    @Override
    public boolean validateOrders(List<CreateSurveySectionDto> createSurveySectionDtos) {
        Set<Integer> sectionOrders = new HashSet<>();

        for (CreateSurveySectionDto sectionDto : createSurveySectionDtos) {
            if (!sectionOrders.add(sectionDto.getOrder())) {
                return false;
            }

            Set<Integer> questionOrders = new HashSet<>();

            for (CreateQuestionDto questionDto : sectionDto.getQuestions()) {
                if (!questionOrders.add(questionDto.getOrder())) {
                    return false;
                }

                Set<Integer> optionOrders = new HashSet<>();

                for (CreateOptionDto optionDto : questionDto.getOptions()) {
                    if (!optionOrders.add(optionDto.getOrder())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
