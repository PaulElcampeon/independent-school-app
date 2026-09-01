package com.paulo.independentschooldata.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SchoolAdminDto {

    private Long id;
    private UUID uuid;
    private String name;
    private String address;
    private String postCode;
    private String regionName;
    private String areaCode;
    private String phone;
    private String email;
    private String website;
    private String tagLine;
    private String tagLineImgUrl;

    private String genderProfile;
    private String size;
    private String religiousAffiliation;
    private String politicalAffiliation;

    private String headName;
    private String associations;
    private String partnershipsLink;
    private String iscRef;
    private String dfeRef;
    private String mpUrl;

    private String description;
    private List<String> schoolType;
    private String ethos;
    private boolean sendSchool;
    private String sendProvision;

    private String profileUrl;
    private String schoolImgUrl;

    private String scholarships;
    private String scholarshipsDescription;
    private String scholarshipsUrl;
    private String scholarshipsAvailability;

    private String latitude;
    private String longitude;

    private String boardingFees;
    private String boardingFeeFrom;
    private String boardingFeeTo;

    private String dayFees;
    private String dayFeeFrom;
    private String dayFeeTo;

    private String girlsInfo;
    private String girlsDayAgeFrom;
    private String girlsDayAgeTo;
    private String girlsDayCount;
    private String girlsBoardingAgeFrom;
    private String girlsBoardingAgeTo;
    private String girlsBoardingCount;
    private String girlsSixthFormCount;

    private String boysInfo;
    private String boysDayAgeFrom;
    private String boysDayAgeTo;
    private String boysDayCount;
    private String boysBoardingAgeFrom;
    private String boysBoardingAgeTo;
    private String boysBoardingCount;
    private String boysSixthFormCount;

    private BigDecimal averageHousePrice;
    private boolean featured;

    private List<String> boardingTypes;
    private List<String> stages;
    private List<String> schoolYears;

    private Instant openDay;

    private String feeDescription;
    private String feeSources;
    private String boardingTypeDescription;
    private String ofstedLink;

    private HashMap<String, BigDecimal> ageRangeToFee;
    private HashMap<String, BigDecimal> feeDescriptionV3;

    private Double year13Candidates;

    private Double percentageWithResultsAt9OrBetter;
    private Double percentageWithResultsAt8OrBetter;
    private Double percentageWithResultsAt7OrBetter;
    private Double percentageWithResultsAt6OrBetter;
    private Double percentageWithResultsAt5OrBetter;
    private Double percentageWithResultsAt4OrBetter;

    private Double igcse9OrBetter;
    private Double igcse8OrBetter;
    private Double igcse7OrBetter;
    private Double igcse6OrBetter;
    private Double igcse5OrBetter;
    private Double igcse4OrBetter;
    private Double igcseAOrBetter;
    private Double igcseBOrBetter;
    private Double igcseCOrBetter;

    private Double alevelAstar;
    private Double alevelA_Astar;
    private Double alevelA_B;
    private Double alevelA_C;

    private Double ibPoints;
    private Double national5sA;
    private Double national5sAB;
    private Double national5sAc;
    private Double highersA;
    private Double highersAB;
    private Double highersAC;
    private Double advancedHighersA;
    private Double advancedHighersAB;
    private Double advancedHighersAC;
    private Double advancedPlacement;

    private boolean deleted;
}
