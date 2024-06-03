package com.survey.domain.repository;

import com.survey.domain.models.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OptionRepository extends JpaRepository<Option, UUID> {
    @Query("SELECT o FROM Option o WHERE o.question.id = :questionId AND o.id IN :optionIds")
    List<Option> findAllByIds(UUID questionId, List<UUID> optionIds);

}
