package com.survey.domain.repository;

import com.survey.domain.models.TextAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TextAnswerRepository extends JpaRepository<TextAnswer, UUID> {
}
