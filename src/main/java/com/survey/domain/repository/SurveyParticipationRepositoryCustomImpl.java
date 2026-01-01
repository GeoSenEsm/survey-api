package com.survey.domain.repository;

import com.survey.domain.models.SurveyParticipation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;

@Repository
public class SurveyParticipationRepositoryCustomImpl implements SurveyParticipationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<SurveyParticipation> findByFiltersWithFetch(
            UUID surveyId,
            UUID identityUserId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Boolean outsideResearchArea) {

        List<UUID> ids = findIdsByFilters(surveyId, identityUserId, dateFrom, dateTo, outsideResearchArea);
        if (ids.isEmpty()) {
            return List.of();
        }

        int batchSize = 5000;

        List<SurveyParticipation> result = new ArrayList<>(ids.size());
        Set<UUID> seen = new HashSet<>(Math.min(ids.size(), 65536));

        for (int i = 0; i < ids.size(); i += batchSize) {
            List<UUID> batchIds = ids.subList(i, Math.min(i + batchSize, ids.size()));
            List<SurveyParticipation> batch = fetchWithRelationsByIds(batchIds);

            for (SurveyParticipation sp : batch) {
                UUID id = sp.getId();
                if (id != null && seen.add(id)) {
                    result.add(sp);
                }
            }
        }

        return result;
    }

    private List<UUID> findIdsByFilters(
            UUID surveyId,
            UUID identityUserId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Boolean outsideResearchArea) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UUID> q = cb.createQuery(UUID.class);
        Root<SurveyParticipation> sp = q.from(SurveyParticipation.class);

        List<Predicate> predicates = new ArrayList<>();

        if (surveyId != null) {
            predicates.add(cb.equal(sp.get("survey").get("id"), surveyId));
        }
        if (identityUserId != null) {
            predicates.add(cb.equal(sp.get("identityUser").get("id"), identityUserId));
        }

        if (dateFrom != null && dateTo != null) {
            predicates.add(cb.between(sp.get("date"), dateFrom, dateTo));
        } else if (dateFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(sp.get("date"), dateFrom));
        } else if (dateTo != null) {
            predicates.add(cb.lessThanOrEqualTo(sp.get("date"), dateTo));
        }

        if (outsideResearchArea != null) {
            Join<Object, Object> ldJoin = sp.join("localizationData", JoinType.LEFT);
            predicates.add(cb.equal(ldJoin.get("outsideResearchArea"), outsideResearchArea));
            q.distinct(true);
        }

        q.select(sp.get("id"));
        if (!predicates.isEmpty()) {
            q.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<UUID> tq = entityManager.createQuery(q);
        tq.setHint("jakarta.persistence.query.timeout", 60000);
        tq.setHint("org.hibernate.readOnly", true);

        return tq.getResultList();
    }

    private List<SurveyParticipation> fetchWithRelationsByIds(List<UUID> ids) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SurveyParticipation> q = cb.createQuery(SurveyParticipation.class);
        Root<SurveyParticipation> sp = q.from(SurveyParticipation.class);

        sp.fetch("localizationData", JoinType.LEFT);
        sp.fetch("sensorData", JoinType.LEFT);

        q.select(sp).distinct(true);
        q.where(sp.get("id").in(ids));

        TypedQuery<SurveyParticipation> tq = entityManager.createQuery(q);
        tq.setHint("jakarta.persistence.query.timeout", 60000);
        tq.setHint("org.hibernate.fetchSize", 5000);
        tq.setHint("org.hibernate.readOnly", true);

        return tq.getResultList();
    }
}
