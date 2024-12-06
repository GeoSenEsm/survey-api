package com.survey.domain.repository;

import com.survey.domain.models.LocalizationData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LocalizationDataRepository extends JpaRepository<LocalizationData, UUID> {

}
