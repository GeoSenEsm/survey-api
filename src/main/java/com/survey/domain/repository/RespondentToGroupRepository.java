package com.survey.domain.repository;

import com.survey.domain.models.RespondentToGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface RespondentToGroupRepository extends JpaRepository<RespondentToGroup, UUID> {
    @Query("SELECT rtg FROM RespondentToGroup rtg JOIN FETCH rtg.respondentGroup WHERE rtg.respondentData.identityUserId = :identityUserId")
    List<RespondentToGroup> findGroupsByIdentityUserId(UUID identityUserId);

    @Transactional
    @Modifying
    @Query("DELETE FROM RespondentToGroup rtg WHERE rtg.respondentData.id =:respondentDataId")
    void deleteAllByRespondentDataId(UUID respondentDataId);

}
