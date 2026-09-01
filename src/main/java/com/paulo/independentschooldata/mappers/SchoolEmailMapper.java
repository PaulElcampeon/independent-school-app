package com.paulo.independentschooldata.mappers;

import com.paulo.independentschooldata.domain.SchoolEmail;
import com.paulo.independentschooldata.dto.SchoolEmailDto;

public class SchoolEmailMapper {

    public static SchoolEmailDto toDto(SchoolEmail entity) {
        if (entity == null) {
            return null;
        }

        return SchoolEmailDto.builder()
                .schoolId(entity.getSchoolId())
                .schoolUuid(entity.getSchoolUuid())
                .subject(entity.getSubject())
                .delivered(entity.isDelivered())
                .schoolName(entity.getSchoolName())
                .schoolEmail(entity.getSchoolEmail())
                .bounced(entity.isBounced())
                .bounceReason(entity.getBounceReason())
                .emailType(entity.getEmailType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static SchoolEmail toEntity(SchoolEmailDto dto) {
        if (dto == null) {
            return null;
        }

        return SchoolEmail.builder()
                .schoolId(dto.getSchoolId())
                .schoolUuid(dto.getSchoolUuid())
                .subject(dto.getSubject())
                .schoolName(dto.getSchoolName())
                .schoolEmail(dto.getSchoolEmail())
                .delivered(dto.isDelivered())
                .bounced(dto.isBounced())
                .bounceReason(dto.getBounceReason())
                .emailType(dto.getEmailType())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
