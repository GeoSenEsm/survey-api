package com.survey.domain.repository;

import com.survey.domain.models.MedicationUse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationUseRepository extends JpaRepository<MedicationUse, Integer> {

}
