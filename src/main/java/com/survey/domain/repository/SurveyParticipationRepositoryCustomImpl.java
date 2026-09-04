package com.survey.domain.repository;

import com.survey.domain.models.SurveyParticipation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Repository
public class SurveyParticipationRepositoryCustomImpl implements SurveyParticipationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    // Safe batch size for IN clause - well under SQL Server's 2100 parameter limit
    private static final int SAFE_BATCH_SIZE = 1000;

    @Override
    public List<SurveyParticipation> findByFiltersWithFetch(
            UUID surveyId,
            UUID identityUserId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Boolean outsideResearchArea) {

        // Two-step approach with safe batching:
        // 1. Find matching IDs
        // 2. Fetch entities in batches with safe IN clause size
        List<UUID> ids = findIdsByFilters(surveyId, identityUserId, dateFrom, dateTo, outsideResearchArea);
        if (ids.isEmpty()) {
            return List.of();
        }

        // Batch the fetch operations to avoid exceeding parameter limits
        List<SurveyParticipation> result = new ArrayList<>(ids.size());
        Set<UUID> seen = new HashSet<>(Math.min(ids.size(), 65536));

        for (int i = 0; i < ids.size(); i += SAFE_BATCH_SIZE) {
            List<UUID> batchIds = ids.subList(i, Math.min(i + SAFE_BATCH_SIZE, ids.size()));
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

    @Override
    public List<SurveyParticipation> findByFiltersWithFetchBatch(
            UUID surveyId,
            UUID identityUserId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Boolean outsideResearchArea,
            int offset,
            int limit) {

        // Get IDs with pagination first
        List<UUID> ids = findIdsByFiltersWithPagination(surveyId, identityUserId, dateFrom, dateTo, outsideResearchArea, offset, limit);
        if (ids.isEmpty()) {
            return List.of();
        }

        // Fetch entities with relations - this batch is already limited by the 'limit' parameter
        // but we still need to ensure it doesn't exceed SAFE_BATCH_SIZE
        if (ids.size() <= SAFE_BATCH_SIZE) {
            return fetchWithRelationsByIds(ids);
        } else {
            // If somehow we get more IDs than safe, batch them
            List<SurveyParticipation> result = new ArrayList<>();
            for (int i = 0; i < ids.size(); i += SAFE_BATCH_SIZE) {
                List<UUID> batchIds = ids.subList(i, Math.min(i + SAFE_BATCH_SIZE, ids.size()));
                result.addAll(fetchWithRelationsByIds(batchIds));
            }
            return result;
        }
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

        List<Predicate> predicates = buildPredicates(cb, sp, surveyId, identityUserId, dateFrom, dateTo, outsideResearchArea);

        q.select(sp.get("id")).distinct(true);
        if (!predicates.isEmpty()) {
            q.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        TypedQuery<UUID> tq = entityManager.createQuery(q);
        tq.setHint("jakarta.persistence.query.timeout", 60000);
        tq.setHint("org.hibernate.readOnly", true);

        return tq.getResultList();
    }

    private List<UUID> findIdsByFiltersWithPagination(
            UUID surveyId,
            UUID identityUserId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Boolean outsideResearchArea,
            int offset,
            int limit) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UUID> q = cb.createQuery(UUID.class);
        Root<SurveyParticipation> sp = q.from(SurveyParticipation.class);

        List<Predicate> predicates = buildPredicates(cb, sp, surveyId, identityUserId, dateFrom, dateTo, outsideResearchArea);

        // No need for distinct when selecting IDs (they are already unique by definition)
        q.select(sp.get("id"));
        if (!predicates.isEmpty()) {
            q.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Order by date for consistent pagination
        q.orderBy(cb.asc(sp.get("date")));

        TypedQuery<UUID> tq = entityManager.createQuery(q);
        tq.setFirstResult(offset);
        tq.setMaxResults(limit);
        tq.setHint("jakarta.persistence.query.timeout", 60000);
        tq.setHint("org.hibernate.readOnly", true);

        return tq.getResultList();
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<SurveyParticipation> sp,
            UUID surveyId,
            UUID identityUserId,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo,
            Boolean outsideResearchArea) {

        List<Predicate> predicates = new ArrayList<>();

        if (surveyId != null) {
            predicates.add(cb.equal(sp.get("survey").get("id"), surveyId));
        }
        if (identityUserId != null) {
            predicates.add(cb.equal(sp.get("identityUser").get("id"), identityUserId));
        }

        if (dateFrom != null || dateTo != null) {
            predicates.add(localDateTimeRange(cb, sp, dateFrom, dateTo));
        }

        if (outsideResearchArea != null) {
            Join<Object, Object> ldJoin = sp.join("localizationData", JoinType.LEFT);
            predicates.add(cb.equal(ldJoin.get("outsideResearchArea"), outsideResearchArea));
        }

        return predicates;
    }

    /**
     * Interprets {@code dateFrom}/{@code dateTo} LocalDateTime faces as study
     * wall-clock bounds and matches denormalized {@code local_date}/{@code local_time}.
     */
    private static Predicate localDateTimeRange(
            CriteriaBuilder cb,
            Root<SurveyParticipation> sp,
            OffsetDateTime dateFrom,
            OffsetDateTime dateTo) {
        Predicate fromPredicate = null;
        if (dateFrom != null) {
            LocalDateTime from = dateFrom.toLocalDateTime();
            fromPredicate = cb.or(
                    cb.greaterThan(sp.get("localDate"), from.toLocalDate()),
                    cb.and(
                            cb.equal(sp.get("localDate"), from.toLocalDate()),
                            cb.greaterThanOrEqualTo(sp.get("localTime"), from.toLocalTime().withNano(0))
                    )
            );
        }
        Predicate toPredicate = null;
        if (dateTo != null) {
            LocalDateTime to = dateTo.toLocalDateTime();
            toPredicate = cb.or(
                    cb.lessThan(sp.get("localDate"), to.toLocalDate()),
                    cb.and(
                            cb.equal(sp.get("localDate"), to.toLocalDate()),
                            cb.lessThanOrEqualTo(sp.get("localTime"), to.toLocalTime().withNano(0))
                    )
            );
        }
        if (fromPredicate != null && toPredicate != null) {
            return cb.and(fromPredicate, toPredicate);
        }
        return fromPredicate != null ? fromPredicate : toPredicate;
    }

    private List<SurveyParticipation> fetchWithRelationsByIds(List<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }

        // Ensure we never exceed safe batch size
        if (ids.size() > SAFE_BATCH_SIZE) {
            throw new IllegalArgumentException("Batch size " + ids.size() + " exceeds safe limit " + SAFE_BATCH_SIZE);
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SurveyParticipation> q = cb.createQuery(SurveyParticipation.class);
        Root<SurveyParticipation> sp = q.from(SurveyParticipation.class);

        // Eager fetch related entities
        sp.fetch("localizationData", JoinType.LEFT);
        sp.fetch("survey", JoinType.LEFT);
        sp.fetch("identityUser", JoinType.LEFT);

        q.select(sp).distinct(true);
        q.where(sp.get("id").in(ids));

        TypedQuery<SurveyParticipation> tq = entityManager.createQuery(q);
        tq.setHint("jakarta.persistence.query.timeout", 60000);
        tq.setHint("org.hibernate.fetchSize", 5000);
        tq.setHint("org.hibernate.readOnly", true);

        return tq.getResultList();
    }
}
