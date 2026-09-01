package com.paulo.independentschooldata.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SchoolMapDto {

    private Long id;
    private UUID uuid;
    private String name;
    private String address;
    private String regionName;
    private String phone;
    private String email;
    private String website;
    private String tagline;
    private String tagLineImgUrl;
    private String genderProfile;
    private String size;
    private List<String> boardingTypes;
    private List<String> stages;
    private List<String> schoolYears;
    private String religiousAffiliation;
    private String politicalAffiliation;
    private String headName;
    private String description;
    private String ethos;
    private String sendProvision;
    private boolean sendSchool;
    private Instant openDay;
    private String schoolImgUrl;
    private String latitude;
    private String longitude;
    private String ofstedLink;
    private BigDecimal averageHousePrice;
    private boolean featured;
    private List<String> schoolType;
}
