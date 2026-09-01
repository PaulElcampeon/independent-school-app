package com.paulo.independentschooldata.mappers;

import com.paulo.independentschooldata.domain.School;
import com.paulo.independentschooldata.dto.SchoolDto;
import com.paulo.independentschooldata.dto.SchoolMapDto;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;

public class SchoolMapper {

    public static SchoolDto toDto(School entity) {
        if (entity == null) return null;


        HashMap<String, BigDecimal> cleaned = null;
        if (entity.getFeeDescriptionV3() != null) {
            cleaned = new HashMap<>(entity.getFeeDescriptionV3());
            cleaned.values().removeIf(v -> v != null && v.compareTo(BigDecimal.valueOf(-1)) == 0);
        }

        return SchoolDto.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .name(entity.getName())
                .address(entity.getAddress())
//                .postCode(entity.getPostCode())
                .regionName(entity.getRegionName())
//                .areaCode(entity.getAreaCode())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .website(entity.getWebsite())
                .genderProfile(entity.getGenderProfile())
                .size(entity.getSize())
                .boardingTypes(Arrays.stream(entity.getBoardingTypes().split(",")).toList())
//                .stages(Arrays.stream(entity.getStages().split(",")).toList())
                .schoolYears(Arrays.stream(entity.getSchoolYears().split(",")).toList())
                .religiousAffiliation(entity.getReligiousAffiliation())
                .politicalAffiliation(entity.getPoliticalAffiliation())
                .headName(entity.getHeadName())
                .feeDescription(entity.getFeeDescription())
                .ofstedLink(entity.getOfstedLink())
//                .associations(entity.getAssociations())
//                .partnershipsLink(entity.getPartnershipsLink())
//                .iscRef(entity.getIscRef())
//                .dfeRef(entity.getDfeRef())
//                .mpUrl(entity.getMpUrl())
                .description(entity.getDescription())
                .ethos(entity.getEthos())
                .sendProvision(entity.getSendProvision())
                .sendSchool(entity.isSend())
//                .profileUrl(entity.getProfileUrl())
                .scholarships(entity.getScholarships())
                .scholarshipsDescription(entity.getScholarshipsDescription())
                .scholarshipsUrl(entity.getScholarshipsUrl())
                .scholarshipsAvailability(entity.getScholarshipsAvailability())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
//                .boardingFees(entity.getBoardingFees())
//                .boardingFeeFrom(entity.getBoardingFeeFrom())
//                .boardingFeeTo(entity.getBoardingFeeTo())
//                .dayFees(entity.getDayFees())
//                .dayFeeFrom(entity.getDayFeeFrom())
//                .dayFeeTo(entity.getDayFeeTo())
//                .girlsInfo(entity.getGirlsInfo())
//                .girlsDayAgeFrom(entity.getGirlsDayAgeFrom())
//                .girlsDayAgeTo(entity.getGirlsDayAgeTo())
                .girlsDayCount(entity.getGirlsDayCount())
//                .girlsBoardingAgeFrom(entity.getGirlsBoardingAgeFrom())
//                .girlsBoardingAgeTo(entity.getGirlsBoardingAgeTo())
                .girlsBoardingCount(entity.getGirlsBoardingCount())
                .girlsSixthFormCount(entity.getGirlsSixthFormCount())
//                .boysInfo(entity.getBoysInfo())
//                .boysDayAgeFrom(entity.getBoysDayAgeFrom())
//                .boysDayAgeTo(entity.getBoysDayAgeTo())
                .boysDayCount(entity.getBoysDayCount())
//                .boysBoardingAgeFrom(entity.getBoysBoardingAgeFrom())
//                .boysBoardingAgeTo(entity.getBoysBoardingAgeTo())
                .boysBoardingCount(entity.getBoysBoardingCount())
                .boysSixthFormCount(entity.getBoysSixthFormCount())
                .averageHousePrice(entity.getAverageHousePrice())
                .featured(entity.isFeatured())
//                .ageRangeToFee(entity.getAgeRangeToFee())
                .schoolType(Arrays.stream(entity.getSchoolType().split(",")).toList())
                .schoolImgUrl(entity.getSchoolImgUrl())

                // Performance metrics
                .year13Candidates(entity.getYear13Candidates())
                .percentageWithResultsAt9OrBetter(entity.getPercentageWithResultsAt9OrBetter())
                .percentageWithResultsAt8OrBetter(entity.getPercentageWithResultsAt8OrBetter())
                .percentageWithResultsAt7OrBetter(entity.getPercentageWithResultsAt7OrBetter())
                .percentageWithResultsAt6OrBetter(entity.getPercentageWithResultsAt6OrBetter())
                .percentageWithResultsAt5OrBetter(entity.getPercentageWithResultsAt5OrBetter())
                .percentageWithResultsAt4OrBetter(entity.getPercentageWithResultsAt4OrBetter())

                // IGCSE results
                .igcse9OrBetter(entity.getIgcse9OrBetter())
                .igcse8OrBetter(entity.getIgcse8OrBetter())
                .igcse7OrBetter(entity.getIgcse7OrBetter())
                .igcse6OrBetter(entity.getIgcse6OrBetter())
                .igcse5OrBetter(entity.getIgcse5OrBetter())
                .igcse4OrBetter(entity.getIgcse4OrBetter())
                .igcseAOrBetter(entity.getIgcseAOrBetter())
                .igcseBOrBetter(entity.getIgcseBOrBetter())
                .igcseCOrBetter(entity.getIgcseCOrBetter())

                // A Level results
                .alevelAstar(entity.getAlevelAstar())
                .alevelAAstar(entity.getAlevelA_Astar())
                .alevelAB(entity.getAlevelA_B())
                .alevelAC(entity.getAlevelA_C())

                // IB, AP, Scottish
                .ibPoints(entity.getIbPoints())
                .national5sA(entity.getNational5sA())
                .national5sAB(entity.getNational5sAB())
                .national5sAC(entity.getNational5sAc())
                .highersA(entity.getHighersA())
                .highersAB(entity.getHighersAB())
                .highersAC(entity.getHighersAC())
                .advancedHighersA(entity.getAdvancedHighersA())
                .advancedHighersAB(entity.getAdvancedHighersAB())
                .advancedHighersAC(entity.getAdvancedHighersAC())
                .advancedPlacement(entity.getAdvancedPlacement())
                .tagline(entity.getTagLine())
                .tagLineImgUrl(entity.getTagLineImgUrl())
                .feeDescriptionV3(cleaned)
                .openDay(entity.getOpenDay())
                .build();
    }

    public static SchoolMapDto toMapDto(School entity) {
        if (entity == null) return null;


        HashMap<String, BigDecimal> cleaned = null;
        if (entity.getFeeDescriptionV3() != null) {
            cleaned = new HashMap<>(entity.getFeeDescriptionV3());
            cleaned.values().removeIf(v -> v != null && v.compareTo(BigDecimal.valueOf(-1)) == 0);
        }

        return SchoolMapDto.builder()
                .id(entity.getId())
                .uuid(entity.getUuid())
                .name(entity.getName())
                .address(entity.getAddress())
                .regionName(entity.getRegionName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .website(entity.getWebsite())
                .genderProfile(entity.getGenderProfile())
                .size(entity.getSize())
                .boardingTypes(Arrays.stream(entity.getBoardingTypes().split(",")).toList())
                .schoolYears(Arrays.stream(entity.getSchoolYears().split(",")).toList())
                .religiousAffiliation(entity.getReligiousAffiliation())
                .politicalAffiliation(entity.getPoliticalAffiliation())
                .headName(entity.getHeadName())
                .ofstedLink(entity.getOfstedLink())
                .description(entity.getDescription())
                .ethos(entity.getEthos())
                .sendProvision(entity.getSendProvision())
                .sendSchool(entity.isSend())
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .averageHousePrice(entity.getAverageHousePrice())
                .featured(entity.isFeatured())
                .schoolType(Arrays.stream(entity.getSchoolType().split(",")).toList())
                .schoolImgUrl(entity.getSchoolImgUrl())
                .tagline(entity.getTagLine())
                .tagLineImgUrl(entity.getTagLineImgUrl())
                .openDay(entity.getOpenDay())
                .build();
    }
}
