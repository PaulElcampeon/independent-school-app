package com.paulo.independentschooldata.repos;

import com.paulo.independentschooldata.domain.SchoolEmail;
import com.paulo.independentschooldata.domain.enums.EmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SchoolEmailRepository extends JpaRepository<SchoolEmail, Long> {

    @Query("SELECT s FROM SchoolEmail s WHERE " +
            "(:schoolId IS NULL OR s.schoolId = :schoolId) AND " +
            "(:schoolUuid IS NULL OR s.schoolUuid = :schoolUuid) AND " +
            "(:subject IS NULL OR s.subject = :subject) AND " +
            "(:emailType IS NULL OR s.emailType = :emailType) AND " +
            "(:delivered IS NULL OR s.delivered = :delivered) AND " +
            "(:bounced IS NULL OR s.bounced = :bounced)")
    Page<SchoolEmail> findWithFilters(
            @Param("schoolId") Long schoolId,
            @Param("schoolUuid") String schoolUuid,
            @Param("subject") String subject,
            @Param("emailType") EmailType emailType,
            @Param("delivered") Boolean delivered,
            @Param("bounced") Boolean bounced,
            Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE SchoolEmail s SET s.delivered = true, s.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE s.subject = :subject AND s.schoolEmail = :email")
    void markEmailAsSentWhereSubjectAndEmail(@Param("subject") String subject, @Param("email") String email);

    @Modifying
    @Transactional
    @Query("UPDATE SchoolEmail s SET " +
            "s.bounced = true, " +
            "s.delivered = false, " +
            "s.bounceReason = :reason, " +
            "s.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE s.subject = :subject AND s.schoolEmail = :email")
    void markEmailAsBouncedWhereSubjectAndEmail(
            @Param("subject") String subject,
            @Param("email") String email,
            @Param("reason") String reason);

    boolean existsBySubjectAndSchoolEmailAndDeliveredTrue(String subject, String schoolEmail);

    List<SchoolEmail> findBySchoolId(Long schoolId);

    List<SchoolEmail> findBySchoolUuid(String schoolUuid);

    Optional<SchoolEmail> findBySchoolIdAndSubject(Long schoolId, String subject);

    Optional<SchoolEmail> findBySchoolUuidAndSubject(String schoolUuid, String subject);

    List<SchoolEmail> findAllBySubject(String subject);
}
