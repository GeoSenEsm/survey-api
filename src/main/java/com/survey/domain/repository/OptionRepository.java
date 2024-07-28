package com.survey.domain.repository;

import com.survey.domain.models.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OptionRepository extends JpaRepository<Option, UUID> {
    List<Option> findByQuestionIdIn(List<UUID> questionIds);
    List<Option> findByIdIn(List<UUID> ids);
}
