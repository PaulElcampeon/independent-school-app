package com.paulo.independentschooldata.dto;

import com.paulo.independentschooldata.domain.enums.EmailType;

import java.util.List;

public record BatchSchoolEmailDto(EmailType emailType, String subject, String body, List<Long> schoolIds) {
}
