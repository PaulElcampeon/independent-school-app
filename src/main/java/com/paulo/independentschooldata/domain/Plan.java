package com.paulo.independentschooldata.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code; // e.g. "basic", "pro", "premium"
    private String name;
    private String description;
    private Long defaultPricePence;
    private String billingPeriod;
    private String stripePriceId;
    private String features;
    @Builder.Default
    private boolean active = true;
}
