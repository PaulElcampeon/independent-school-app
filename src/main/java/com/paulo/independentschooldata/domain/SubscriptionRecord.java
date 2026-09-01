package com.paulo.independentschooldata.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name="subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRecord {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private Long schoolId;
    private Long planId;
    private String stripeSessionId;
    private String stripePaymentIntentId;
    private String stripeSubscriptionId;
    private String stripeCustomerId;
    private Instant currentPeriodStart;
    private Instant currentPeriodEnd;
    private Boolean cancelAtPeriodEnd = false;
    private Instant cancelAt;
    private String latestInvoiceId; // ID of the latest invoice
    private String status; // "created","completed","failed"
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    private void onCreate() {
        updatedAt = Instant.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }

}
