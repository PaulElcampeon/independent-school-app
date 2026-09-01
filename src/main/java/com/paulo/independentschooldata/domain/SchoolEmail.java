package com.paulo.independentschooldata.domain;

import com.paulo.independentschooldata.domain.enums.EmailType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "school_email")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SchoolEmail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "school_uuid", nullable = false)
    private String schoolUuid;

    private String schoolName;

    private String schoolEmail;

    private String subject;

    private boolean delivered;

    private boolean bounced;

    private String bounceReason;

    @Enumerated(EnumType.STRING)
    private EmailType emailType;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PrePersist
    private void onCreate() {
        updatedAt = Instant.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }
}
