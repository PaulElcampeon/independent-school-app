package com.paulo.independentschooldata.repos;

import com.paulo.independentschooldata.domain.SubscriptionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionRecord, Long> {
    Optional<SubscriptionRecord> findByStripeSessionId(String sessionId);
    Optional<SubscriptionRecord> findByStripeSubscriptionId(String subscriptionId);
    List<SubscriptionRecord> findBySchoolId(Long schoolId);
}