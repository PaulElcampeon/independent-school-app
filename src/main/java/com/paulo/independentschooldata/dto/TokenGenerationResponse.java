package com.paulo.independentschooldata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenGenerationResponse {
    private String accessCode;
    private String urlCode;
    private String url;
}

