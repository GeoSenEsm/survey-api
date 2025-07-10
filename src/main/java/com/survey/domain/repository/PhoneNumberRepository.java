package com.survey.domain.repository;

import com.survey.domain.models.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PhoneNumberRepository extends JpaRepository<PhoneNumber, UUID> {
}
