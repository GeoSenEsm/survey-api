package com.survey.domain.repository;

import com.survey.domain.models.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SensorTypeRepository extends JpaRepository<SensorType, UUID> {
    Optional<SensorType> findByCode(String code);

    List<SensorType> findAllByCodeIn(Collection<String> codes);

    @Query("SELECT st FROM SensorType st ORDER BY st.name")
    List<SensorType> findAllOrderByName();
}
