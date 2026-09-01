package com.paulo.independentschooldata.repos;

import com.paulo.independentschooldata.domain.ForgotPasswordNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ForgotPasswordNotificationRepository extends JpaRepository<ForgotPasswordNotification, Long> {

    Optional<ForgotPasswordNotification> findByEmail(String email);
    Optional<ForgotPasswordNotification> findByCode(UUID code);
}
