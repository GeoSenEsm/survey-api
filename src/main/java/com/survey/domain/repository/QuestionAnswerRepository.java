package com.survey.domain.repository;

import com.survey.domain.models.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionAnswerRepository
extends JpaRepository<QuestionAnswer, UUID> {
}
