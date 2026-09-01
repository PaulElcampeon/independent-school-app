package com.paulo.independentschooldata.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "forgot_password_notification")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ForgotPasswordNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Builder.Default
    @Column(name = "code", updatable = false, nullable = false, unique = true)
    private UUID code = UUID.randomUUID();

    @Column(name = "name", nullable = false)
    private String email;

    @Column(name = "school_name", nullable = false)
    private String schoolName;

    @Column(name = "school_id", nullable = false)
    private Long schoolId;

    @Column(name = "is_valid", nullable = false)
    private boolean isValid = true;

    @Column(name = "updated_at")
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
