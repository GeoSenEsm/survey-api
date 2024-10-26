package com.survey.domain.repository;

import com.survey.domain.models.InitialSurveyOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InitialSurveyOptionRepository extends JpaRepository<InitialSurveyOption, UUID> {
    List<InitialSurveyOption> findByQuestionIdIn(List<UUID> questionIds);

}
