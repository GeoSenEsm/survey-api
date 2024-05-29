package com.survey.domain.repository;

import com.survey.domain.models.RespondentToGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface RespondentToGroupRepository extends JpaRepository<RespondentToGroup, UUID> {
    @Query("SELECT rtg FROM RespondentToGroup rtg JOIN FETCH rtg.respondentGroup WHERE rtg.respondentData.id = :respondentId")
    List<RespondentToGroup> findGroupsByRespondentDataId(UUID respondentId);

}
