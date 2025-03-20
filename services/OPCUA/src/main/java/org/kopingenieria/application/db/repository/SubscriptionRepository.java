package org.kopingenieria.application.db.repository;

import org.kopingenieria.application.db.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Subscription findByNodeId(String nodeId);
    List<Subscription> findByPublishingEnabled(Boolean enabled);
    @Query("SELECT s FROM Subscription s WHERE s.publishingInterval = :interval")
    List<Subscription> findByPublishingInterval(@Param("interval") Double interval);
}
