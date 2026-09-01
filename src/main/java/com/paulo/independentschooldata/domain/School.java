package com.paulo.independentschooldata.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "schools")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Builder.Default
    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    private UUID uuid = UUID.randomUUID();

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "post_code")
    private String postCode;

    @Column(name = "region_name")
    private String regionName;

    @Column(name = "area_code")
    private String areaCode;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "website")
    private String website;

    @Column(name = "gender_profile_v3")
    private String genderProfile;

    @Column(name = "size")
    private String size;

    @Column(name = "religious_affiliation")
    private String religiousAffiliation;

    @Column(name = "political_affiliation")
    private String politicalAffiliation;

    @Column(name = "head_name")
    private String headName;

    @Column(name = "associations")
    private String associations;

    @Column(name = "partnerships_link")
    private String partnershipsLink;

    @Column(name = "isc_ref")
    private String iscRef;

    @Column(name = "dfe_ref")
    private String dfeRef;

    @Column(name = "mp_url")
    private String mpUrl;

    @Column(name = "description_v2", columnDefinition = "TEXT")
    private String description;

    @Column(name = "school_type")
    private String schoolType;

    @Column(name = "ethos", columnDefinition = "TEXT")
    private String ethos;

    @Column(name = "is_send")
    private boolean isSend;

    @Column(name = "send_provision", columnDefinition = "TEXT")
    private String sendProvision;

    @Column(name = "profile_url", columnDefinition = "TEXT")
    private String profileUrl;

    @Column(name = "school_img_url")
    private String schoolImgUrl;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "isTest")
    private boolean isTest;

    @Column(name = "scholarships", columnDefinition = "TEXT")
    private String scholarships;

    @Column(name = "scholarships_description", columnDefinition = "TEXT")
    private String scholarshipsDescription;

    @Column(name = "scholarships_url")
    private String scholarshipsUrl;

    @Column(name = "scholarships_availability")
    private String scholarshipsAvailability;

    @Column(name = "latitude")
    private String latitude;

    @Column(name = "longitude")
    private String longitude;

    @Column(name = "boarding_fees")
    private String boardingFees;

    @Column(name = "boarding_fee_from")
    private String boardingFeeFrom;

    @Column(name = "boarding_fee_to")
    private String boardingFeeTo;

    @Column(name = "day_fees")
    private String dayFees;

    @Column(name = "day_fee_from")
    private String dayFeeFrom;

    @Column(name = "day_fee_to")
    private String dayFeeTo;

    @Column(name = "girls_info")
    private String girlsInfo;

    @Column(name = "girls_day_age_from")
    private String girlsDayAgeFrom;

    @Column(name = "girls_day_age_to")
    private String girlsDayAgeTo;

    @Column(name = "girls_day_count")
    private String girlsDayCount;

    @Column(name = "girls_boarding_age_from")
    private String girlsBoardingAgeFrom;

    @Column(name = "girls_boarding_age_to")
    private String girlsBoardingAgeTo;

    @Column(name = "girls_boarding_count")
    private String girlsBoardingCount;

    @Column(name = "girls_sixth_form_count")
    private String girlsSixthFormCount;

    @Column(name = "boys_info")
    private String boysInfo;

    @Column(name = "boys_day_age_from")
    private String boysDayAgeFrom;

    @Column(name = "boys_day_age_to")
    private String boysDayAgeTo;

    @Column(name = "boys_day_count")
    private String boysDayCount;

    @Column(name = "boys_boarding_age_from")
    private String boysBoardingAgeFrom;

    @Column(name = "boys_boarding_age_to")
    private String boysBoardingAgeTo;

    @Column(name = "boys_boarding_count")
    private String boysBoardingCount;

    @Column(name = "boys_sixth_form_count")
    private String boysSixthFormCount;

    @Column(name = "average_house_price")
    private BigDecimal averageHousePrice;

    @Column(name = "is_featured")
    private boolean isFeatured;

    @Column(name = "tag_line")
    private String tagLine;

    @Column(name = "tag_line_img_url")
    private String tagLineImgUrl;

    @Column(name = "boarding_types_v3")
    private String boardingTypes;

    @Column(name = "fee_description", columnDefinition = "TEXT")
    private String feeDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fee_description_v3", columnDefinition = "jsonb")
    private HashMap<String, BigDecimal> feeDescriptionV3;

    @Column(name = "fee_sources", columnDefinition = "TEXT")
    private String feeSources;

    @Column(name = "boarding_type_description", columnDefinition = "TEXT")
    private String boardingTypeDescription;

    @Column(name = "ofsted_link")
    private String ofstedLink;

//    @Column(name = "school_type_v4")
//    private String stages;

    @Column(name = "school_years")
    private String schoolYears;

    @Column(name = "open_day")
    private Instant openDay;

    // JSON field
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "age_range_to_fee", columnDefinition = "jsonb")
    private HashMap<String, BigDecimal> ageRangeToFee;

//    @JdbcTypeCode(SqlTypes.JSON)
//    @Column(name = "fees", columnDefinition = "jsonb")
//    private HashMap<String, BigDecimal> fees;

    // Performance metrics
    @Column(name = "year13candidates")
    private Double year13Candidates;

    @Column(name = "percentage_with_results_at9or_better")
    private Double percentageWithResultsAt9OrBetter;

    @Column(name = "percentage_with_results_at8or_better")
    private Double percentageWithResultsAt8OrBetter;

    @Column(name = "percentage_with_results_at7or_better")
    private Double percentageWithResultsAt7OrBetter;

    @Column(name = "percentage_with_results_at6or_better")
    private Double percentageWithResultsAt6OrBetter;

    @Column(name = "percentage_with_results_at5or_better")
    private Double percentageWithResultsAt5OrBetter;

    @Column(name = "percentage_with_results_at4or_better")
    private Double percentageWithResultsAt4OrBetter;

    // IGCSE results
    @Column(name = "igcse_9_or_better")
    private Double igcse9OrBetter;

    @Column(name = "igcse_8_or_better")
    private Double igcse8OrBetter;

    @Column(name = "igcse_7_or_better")
    private Double igcse7OrBetter;

    @Column(name = "igcse_6_or_better")
    private Double igcse6OrBetter;

    @Column(name = "igcse_5_or_better")
    private Double igcse5OrBetter;

    @Column(name = "igcse_4_or_better")
    private Double igcse4OrBetter;

    @Column(name = "igcse_a_or_better")
    private Double igcseAOrBetter;

    @Column(name = "igcse_b_or_better")
    private Double igcseBOrBetter;

    @Column(name = "igcse_c_or_better")
    private Double igcseCOrBetter;

    // A Level results
    @Column(name = "alevel_astar")
    private Double alevelAstar;

    @Column(name = "alevel_a_astar")
    private Double alevelA_Astar;

    @Column(name = "alevel_a_b")
    private Double alevelA_B;

    @Column(name = "alevel_a_c")
    private Double alevelA_C;

    // IB, AP, and Scottish qualifications
    @Column(name = "ib_points")
    private Double ibPoints;

    @Column(name = "national_5s_a")
    private Double national5sA;

    @Column(name = "national_5s_a_b")
    private Double national5sAB;

    @Column(name = "national_5s_a_c")
    private Double national5sAc;

    @Column(name = "highers_a")
    private Double highersA;

    @Column(name = "highers_a_b")
    private Double highersAB;

    @Column(name = "highers_a_c")
    private Double highersAC;

    @Column(name = "advanced_highers_a")
    private Double advancedHighersA;

    @Column(name = "advanced_highers_a_b")
    private Double advancedHighersAB;

    @Column(name = "advanced_highers_a_c")
    private Double advancedHighersAC;

    @Column(name = "advanced_placement")
    private Double advancedPlacement;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    private void onCreate() {
        updatedAt = Instant.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }
}
