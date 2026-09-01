package com.paulo.independentschooldata.dto;

import lombok.Data;

@Data
public class ValidateTokenRequest {

    private String email;
    private String jwtToken;
}
