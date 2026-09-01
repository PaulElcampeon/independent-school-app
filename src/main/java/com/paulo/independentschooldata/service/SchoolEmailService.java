package com.paulo.independentschooldata.service;

import com.paulo.independentschooldata.domain.SchoolEmail;
import com.paulo.independentschooldata.domain.enums.EmailType;
import com.paulo.independentschooldata.dto.BatchSchoolEmailDto;
import com.paulo.independentschooldata.dto.SchoolDto;
import com.paulo.independentschooldata.dto.SchoolEmailDto;
import com.paulo.independentschooldata.mappers.SchoolEmailMapper;
import com.paulo.independentschooldata.repos.SchoolEmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchoolEmailService {

    private final SchoolEmailRepository schoolEmailRepository;
    private final MailService mailService;
    private final SchoolService schoolService;

    private final AtomicInteger dailyLimit = new AtomicInteger(290);

    public int getRemainingEmails() {
        return dailyLimit.get();
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyLimit() {
        log.info("Resetting daily email limit to 290");
        dailyLimit.set(290);
    }

    public Page<SchoolEmailDto> getEmails(Boolean delivered, Boolean bounced, String emailType, Pageable pageable) {
        EmailType type = (emailType != null) ? EmailType.valueOf(emailType) : null;
        return schoolEmailRepository.findWithFilters(null, null, null, type, delivered, bounced, pageable)
                .map(SchoolEmailMapper::toDto);
    }

    public Page<SchoolEmailDto> getEmailsBySubject(String subject, Boolean delivered, Boolean bounced, Pageable pageable) {
        return schoolEmailRepository.findWithFilters(null, null, subject, null, delivered, bounced, pageable)
                .map(SchoolEmailMapper::toDto);
    }

    public Page<SchoolEmailDto> getEmailsBySchoolUuid(String schoolUuid, Boolean delivered, Boolean bounced, Pageable pageable) {
        return schoolEmailRepository.findWithFilters(null, schoolUuid, null, null, delivered, bounced, pageable)
                .map(SchoolEmailMapper::toDto);
    }

    public Page<SchoolEmailDto> getEmailsBySchoolId(Long schoolId, Boolean delivered, Boolean bounced, Pageable pageable) {
        return schoolEmailRepository.findWithFilters(schoolId, null, null, null, delivered, bounced, pageable)
                .map(SchoolEmailMapper::toDto);
    }

//    public List<SchoolEmailDto> getEmailsBySchoolUuid(String schoolUuid, Boolean delivered, Boolean bounced) {
//        return schoolEmailRepository.findBySchoolUuid(schoolUuid)
//                .stream()
//                .filter(schoolEmail -> {
//                    if (delivered != null) {
//                        if (schoolEmail.isDelivered() != delivered) {
//                            return false;
//                        }
//                    }
//
//                    if (bounced != null) {
//                        if (schoolEmail.isBounced() != bounced) {
//                            return false;
//                        }
//                    }
//
//                    return true;
//                })
//                .map(SchoolEmailMapper::toDto)
//                .toList();
//    }
//
//    public List<SchoolEmailDto> getEmailsBySchoolId(Long schoolId, Boolean delivered, Boolean bounced) {
//        return schoolEmailRepository.findBySchoolId(schoolId)
//                .stream()
//                .filter(schoolEmail -> {
//                    if (delivered != null) {
//                        if (schoolEmail.isDelivered() != delivered) {
//                            return false;
//                        }
//                    }
//
//                    if (bounced != null) {
//                        if (schoolEmail.isBounced() != bounced) {
//                            return false;
//                        }
//                    }
//
//                    return true;
//                })
//                .map(SchoolEmailMapper::toDto)
//                .toList();
//    }
//
//    public List<SchoolEmailDto> getEmails(Boolean delivered, Boolean bounced, String emailType) {
//        return schoolEmailRepository.findAll()
//                .stream()
//                .filter(schoolEmail -> {
//                    if (delivered != null) {
//                        if (schoolEmail.isDelivered() != delivered) {
//                            return false;
//                        }
//                    }
//
//                    if (bounced != null) {
//                        if (schoolEmail.isBounced() != bounced) {
//                            return false;
//                        }
//                    }
//
//                    if (emailType != null) {
//                        if (schoolEmail.getEmailType() != EmailType.valueOf(emailType)) {
//                            return false;
//                        }
//                    }
//
//                    return true;
//                })
//                .map(SchoolEmailMapper::toDto)
//                .toList();
//    }
//
//    public List<SchoolEmailDto> getEmailsBySubject(String subject, Boolean delivered, Boolean bounced) {
//        return schoolEmailRepository.findAllBySubject(subject)
//                .stream()
//                .filter(schoolEmail -> {
//                    if (delivered != null) {
//                        if (schoolEmail.isDelivered() != delivered) {
//                            return false;
//                        }
//                    }
//
//                    if (bounced != null) {
//                        if (schoolEmail.isBounced() != bounced) {
//                            return false;
//                        }
//                    }
//
//                    return true;
//                })
//                .map(SchoolEmailMapper::toDto)
//                .toList();
//    }

    public void sendEmailToSchools(BatchSchoolEmailDto batchSchoolEmailDto) {
        List<SchoolDto> schoolDtos = schoolService.findAllSchoolsByIdIn(batchSchoolEmailDto.schoolIds());
        String subject = batchSchoolEmailDto.subject();
        String body = batchSchoolEmailDto.body();
        EmailType emailType = batchSchoolEmailDto.emailType();

        for (SchoolDto schoolDto : schoolDtos) {
            if (dailyLimit.get() <= 0) {
                log.error("Daily email limit reached! Cannot send email to school ID: {}", schoolDto.getId());
                break;
            }

            String toEmail = schoolDto.getEmail();
            if (toEmail != null && !toEmail.isBlank()) {
                if (hasEmailBeenDelivered(subject, toEmail)) {
                    log.info("Already sent confirmed delivered email to School: {}, with email:{} and subject: {}", schoolDto.getName(), toEmail, subject);
                    continue;
                }
                log.info("Preparing to send email of type: {} to school: {} with email: {} and subject: {}", emailType, schoolDto.getName(), schoolDto.getEmail(), subject);
                boolean emailSent = mailService.sendSimpleEmail(subject, toEmail, body);
                if (emailSent) {
                    log.info("Sent send email of type: {} to school: {} with email: {} and subject: {}", emailType, schoolDto.getName(), schoolDto.getEmail(), subject);

                    dailyLimit.decrementAndGet();

                    SchoolEmail schoolEmail = SchoolEmail.builder()
                            .emailType(batchSchoolEmailDto.emailType())
                            .schoolId(schoolDto.getId())
                            .subject(subject)
                            .schoolEmail(schoolDto.getEmail())
                            .schoolUuid(schoolDto.getUuid().toString())
                            .schoolName(schoolDto.getName())
                            .build();

                    schoolEmailRepository.save(schoolEmail);
                } else {
                    log.info("Failed to send email of type: {} to school: {} with email: {} and subject: {}", emailType, schoolDto.getName(), schoolDto.getEmail(), subject);
                }
            }
        }
    }

    public void markEmailSent(String subject, String email) {
        log.info("Marking email: {} as delivered with subject: {}", email, subject);
        schoolEmailRepository.markEmailAsSentWhereSubjectAndEmail(subject, email);
    }

    public boolean hasEmailBeenDelivered(String subject, String email) {
        return schoolEmailRepository.existsBySubjectAndSchoolEmailAndDeliveredTrue(subject, email);
    }

    @Transactional
    public void markEmailBounced(String subject, String email, String reason) {
        log.info("Email bounced for recipient: {}. Reason: {}", email, reason);

        String truncatedReason = reason;
        if (reason != null && reason.length() > 250) {
            truncatedReason = reason.substring(0, 250);
            log.info("Truncated long bounce reason for recipient: {}", email);
        }

        schoolEmailRepository.markEmailAsBouncedWhereSubjectAndEmail(subject, email, truncatedReason);
    }
}
