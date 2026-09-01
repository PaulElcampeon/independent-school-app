package com.paulo.independentschooldata.dto;

import com.paulo.independentschooldata.domain.enums.EmailType;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolEmailDto {
    private Long schoolId;
    private String schoolUuid;
    private String subject;
    private String schoolName;
    private String schoolEmail;
    private boolean delivered;
    private boolean bounced;
    private String bounceReason;
    private EmailType emailType;
    private Instant createdAt;
    private Instant updatedAt;
}