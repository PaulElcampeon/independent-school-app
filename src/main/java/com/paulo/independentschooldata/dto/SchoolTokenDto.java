package com.paulo.independentschooldata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchoolTokenDto {
    private String accessCode;
    private String urlCode;
    private Long schoolId;
    private LocalDateTime expiresAt;
    private String schoolUuid;
}

