package com.survey.domain.repository;

import com.survey.domain.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    @Query("SELECT q FROM Question q WHERE q.section.survey.id = :surveyId AND q.id IN :questionIds")
   List<Question> findAllByIds(UUID surveyId, List<UUID> questionIds);
}
