package com.paulo.independentschooldata.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EnableUserAccountRequest {

    private UUID userId;
}
