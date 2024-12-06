package com.survey.domain.repository;

import com.survey.domain.models.IdentityUser;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdentityUserRepository extends JpaRepository<IdentityUser, UUID> {
    Optional<IdentityUser> findByUsername(String username);
    @Query("select count(u) from IdentityUser u where u.role = 'Respondent'")
    int countRespondents();

    @Query("SELECT count(u) > 0 FROM IdentityUser u where u.id = :id and u.role = 'Respondent'")
    boolean existsById(@NonNull UUID id);
}
