package com.survey.application.services;

import com.survey.domain.models.enums.Visibility;
import org.springframework.stereotype.Service;

import java.util.Dictionary;

@Service
public class ShowSectionWithOrderValidationServiceImpl implements ShowSectionWithOrderValidationService{
    @Override
    public void validateSectionExistsAndIsAnswerTriggered(Integer showSectionOrder, Dictionary<Integer, String> sectionOrderAndVisibilityDict, Integer currentSectionOrder) {
        if (showSectionOrder != null){

            String sectionVisibility = sectionOrderAndVisibilityDict.get(showSectionOrder);
            if (sectionVisibility == null){
                throw new IllegalArgumentException("showSection: " + showSectionOrder + ". Section with this order does not exist.");
            }

            if (showSectionOrder.equals(currentSectionOrder)){
                throw new IllegalArgumentException("ShowSection can not point to the current section.");
            }

            if (!sectionVisibility.equals(Visibility.answer_triggered.name())){
                throw new IllegalArgumentException("Section with order: " + showSectionOrder + " must have answer_triggered visibility");
            }

        }
    }

}
