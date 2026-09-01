package com.paulo.independentschooldata.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.paulo.independentschooldata.domain.School;
import com.paulo.independentschooldata.dto.SchoolAdminDto;
import com.paulo.independentschooldata.dto.SchoolDto;
import com.paulo.independentschooldata.dto.SchoolMapDto;
import com.paulo.independentschooldata.mappers.SchoolAdminMapper;
import com.paulo.independentschooldata.mappers.SchoolMapper;
import com.paulo.independentschooldata.repos.SchoolRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static com.paulo.independentschooldata.repos.SchoolSpecifications.*;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final ImageStorageService imageStorageService;

    private final Cache<UUID, School> schoolCache = Caffeine.newBuilder()
            .maximumSize(2000)
            .build();

    private static final Set<String> GENDER_TYPES = Set.of("boys", "girls", "co-educational");
    private static final Set<String> BOARDING_TYPES = Set.of("day", "flexi", "full", "unknown", "weekly");
    private static Set<String> REGION_TYPES = new HashSet<>();
    private static Set<String> RELIGION_TYPES = new HashSet<>();

    @PostConstruct
    public void init() {
        loadCache();
    }

    public void reloadCache() {
        log.info("Manual cache reload triggered. Invalidating existing entries...");
        schoolCache.invalidateAll();
        // Force cleanup to help GC, though Caffeine handles this eventually
        schoolCache.cleanUp();
        loadCache();
        log.info("Cache reload complete.");
    }

    private void loadCache() {
        log.info("Loading schools into cache in batches...");

        int pageSize = 200;
        int pageNumber = 0;
        Page<School> page;

        do {
            page = schoolRepository.findAll(PageRequest.of(pageNumber, pageSize));
            log.info("Loaded batch {} with {} schools", pageNumber, page.getNumberOfElements());

            page.getContent().forEach(school -> {
                schoolCache.put(school.getUuid(), school);
            });

            pageNumber++;
        } while (page.hasNext());

        schoolCache.asMap().values().forEach(school -> {
            if (!ObjectUtils.isEmpty(school.getRegionName())) {
                REGION_TYPES.add(school.getRegionName());
            }
            if (!ObjectUtils.isEmpty(school.getReligiousAffiliation())) {
                RELIGION_TYPES.add(school.getReligiousAffiliation());
            }
        });

        log.info("Finished loading cache. Total schools: {}", schoolCache.estimatedSize());
    }

    public SchoolAdminDto saveSchoolAdmin(SchoolAdminDto dto) {
        School entity = SchoolAdminMapper.toEntity(dto);
        entity.setUuid(UUID.randomUUID());

        School saved = schoolRepository.save(entity);
        schoolCache.put(saved.getUuid(), saved);

        return SchoolAdminMapper.toDto(saved);
    }

    public SchoolAdminDto updateAdminSchool(UUID uuid, SchoolAdminDto dto) {
        School retrievedSchool = schoolRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("School not found"));
        dto.setId(retrievedSchool.getId());
        retrievedSchool = SchoolAdminMapper.toEntity(dto);
        retrievedSchool = schoolRepository.save(retrievedSchool);

        schoolCache.put(retrievedSchool.getUuid(), retrievedSchool);

        return SchoolAdminMapper.toDto(retrievedSchool);
    }

    public Page<SchoolMapDto> getAllSchoolsForMap(BigDecimal minFee, BigDecimal maxFee, String examType, String name, Boolean isSend, String gender, String religion, String boarding,
                                                  String region, String feeFilter, Pageable pageable) {
        // 1. Corrected Filtering Logic (Optional Filters)
        List<School> filtered = schoolCache.asMap().values().stream()
                .filter(s -> !s.isDeleted() && !s.isTest())
                .filter(s -> name == null || (s.getName() != null && s.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(s -> examType == null || (hasExamResults(s, examType)))
                .filter(s -> gender == null || (s.getGenderProfile() != null && s.getGenderProfile().equalsIgnoreCase(gender)))
                .filter(s -> religion == null || (s.getReligiousAffiliation() != null && s.getReligiousAffiliation().equalsIgnoreCase(religion)))
                .filter(s -> boarding == null || (s.getBoardingTypes() != null && s.getBoardingTypes().toLowerCase().contains(boarding.toLowerCase())))
                .filter(s -> region == null || (s.getRegionName() != null && s.getRegionName().equalsIgnoreCase(region)))
                .filter(s -> isSend == null || (s.isSend() == isSend))
                .filter(s -> {
                    // If no fee filter is selected, we don't filter by fee
                    if (feeFilter == null) return true;

                    // If the school doesn't have the requested fee type, exclude it
                    if (s.getFeeDescriptionV3() == null || !s.getFeeDescriptionV3().containsKey(feeFilter)) {
                        return false;
                    }

                    BigDecimal schoolFee = s.getFeeDescriptionV3().get(feeFilter);

                    // Exclude schools with negative/invalid placeholder fees (-1)
                    if (schoolFee.compareTo(BigDecimal.valueOf(-1)) <= 0) {
                        return false;
                    }

                    // Apply Min Range Check
                    if (minFee != null && schoolFee.compareTo(minFee) < 0) {
                        return false;
                    }

                    // Apply Max Range Check
                    if (maxFee != null && schoolFee.compareTo(maxFee) > 0) {
                        return false;
                    }

                    return true;
                })
                .collect(toList());

        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Comparator<School> comp = sort.stream()
                    .map(order -> getComparator(order, feeFilter))
                    .reduce(Comparator::thenComparing)
                    .orElse(Comparator.comparing(School::getName));
            filtered.sort(comp);
        }

        // Pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<SchoolMapDto> pageContent = (start > end)
                ? List.of()
                : filtered.subList(start, end).stream()
                .map(SchoolMapper::toMapDto)
                .toList();

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    public Page<SchoolDto> getAllSchools(BigDecimal minFee, BigDecimal maxFee, String examType, String name, Boolean isSend, String gender, String religion, String boarding,
                                         String region, String feeFilter, Pageable pageable) {

        // 1. Corrected Filtering Logic (Optional Filters)
        List<School> filtered = schoolCache.asMap().values().stream()
                .filter(s -> !s.isDeleted() && !s.isTest())
                .filter(s -> name == null || (s.getName() != null && s.getName().toLowerCase().contains(name.toLowerCase())))
                .filter(s -> examType == null || (hasExamResults(s, examType)))
                .filter(s -> gender == null || (s.getGenderProfile() != null && s.getGenderProfile().equalsIgnoreCase(gender)))
                .filter(s -> religion == null || (s.getReligiousAffiliation() != null && s.getReligiousAffiliation().equalsIgnoreCase(religion)))
                .filter(s -> boarding == null || (s.getBoardingTypes() != null && s.getBoardingTypes().toLowerCase().contains(boarding.toLowerCase())))
                .filter(s -> region == null || (s.getRegionName() != null && s.getRegionName().equalsIgnoreCase(region)))
                .filter(s -> isSend == null || (s.isSend() == isSend))
                .filter(s -> {
                    // If no fee filter is selected, we don't filter by fee
                    if (feeFilter == null) return true;

                    // If the school doesn't have the requested fee type, exclude it
                    if (s.getFeeDescriptionV3() == null || !s.getFeeDescriptionV3().containsKey(feeFilter)) {
                        return false;
                    }

                    BigDecimal schoolFee = s.getFeeDescriptionV3().get(feeFilter);

                    // Exclude schools with negative/invalid placeholder fees (-1)
                    if (schoolFee.compareTo(BigDecimal.valueOf(-1)) <= 0) {
                        return false;
                    }

                    // Apply Min Range Check
                    if (minFee != null && schoolFee.compareTo(minFee) < 0) {
                        return false;
                    }

                    // Apply Max Range Check
                    if (maxFee != null && schoolFee.compareTo(maxFee) > 0) {
                        return false;
                    }

                    return true;
                })
                .collect(toList());

        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Comparator<School> comp = sort.stream()
                    .map(order -> getComparator(order, feeFilter))
                    .reduce(Comparator::thenComparing)
                    .orElse(Comparator.comparing(School::getName));
            filtered.sort(comp);
        }

        // Pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<SchoolDto> pageContent = (start > end)
                ? List.of()
                : filtered.subList(start, end).stream()
                .map(SchoolMapper::toDto)
                .toList();

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    private Comparator<School> getComparator(Sort.Order order, String feeFilter) {
        String property = order.getProperty();
        Comparator<School> comparator;

        comparator = switch (property) {
            case "name" -> Comparator.comparing(School::getName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "regionName" ->
                    Comparator.comparing(School::getRegionName, Comparator.nullsLast(String::compareToIgnoreCase));
            // NEW: Fee Sorting Logic
            case "fees", "fee" -> Comparator.comparing(s -> {
                if (s.getFeeDescriptionV3() == null || feeFilter == null) return BigDecimal.ZERO;
                return s.getFeeDescriptionV3().getOrDefault(feeFilter, BigDecimal.ZERO);
            }, Comparator.nullsLast(BigDecimal::compareTo));

            // 2. Default to checking if the property matches an exam field
            default -> getExamFieldComparator(property);
        };

        return order.isAscending() ? comparator : comparator.reversed();
    }

    /**
     * Maps the sortBy string to the specific getter in the School entity.
     */
    private Comparator<School> getExamFieldComparator(String property) {
        // We use a Function to extract the Double, then wrap it in a null-safe comparator
        java.util.function.Function<School, Double> extractor = switch (property) {
            // GCSEs
            case "percentageWithResultsAt9OrBetter" -> School::getPercentageWithResultsAt9OrBetter;
            case "percentageWithResultsAt8OrBetter" -> School::getPercentageWithResultsAt8OrBetter;
            case "percentageWithResultsAt7OrBetter" -> School::getPercentageWithResultsAt7OrBetter;
            case "percentageWithResultsAt6OrBetter" -> School::getPercentageWithResultsAt6OrBetter;
            case "percentageWithResultsAt5OrBetter" -> School::getPercentageWithResultsAt5OrBetter;
            case "percentageWithResultsAt4OrBetter" -> School::getPercentageWithResultsAt4OrBetter;

            // IGCSEs
            case "igcse9OrBetter" -> School::getIgcse9OrBetter;
            case "igcse8OrBetter" -> School::getIgcse8OrBetter;
            case "igcse7OrBetter" -> School::getIgcse7OrBetter;
            case "igcse6OrBetter" -> School::getIgcse6OrBetter;
            case "igcse5OrBetter" -> School::getIgcse5OrBetter;
            case "igcse4OrBetter" -> School::getIgcse4OrBetter;
            case "igcseAOrBetter" -> School::getIgcseAOrBetter;
            case "igcseBOrBetter" -> School::getIgcseBOrBetter;
            case "igcseCOrBetter" -> School::getIgcseCOrBetter;

            // A-Levels
            case "alevelAstar" -> School::getAlevelAstar;
            case "alevelA_Astar" -> School::getAlevelA_Astar;
            case "alevelA_B" -> School::getAlevelA_B;
            case "alevelA_C" -> School::getAlevelA_C;

            // IB & Others
            case "ibPoints" -> School::getIbPoints;
            case "advancedPlacement" -> School::getAdvancedPlacement;

            // Scottish Qualifications
            case "national5sA" -> School::getNational5sA;
            case "national5sAB" -> School::getNational5sAB;
            case "national5sAc" -> School::getNational5sAc;
            case "highersA" -> School::getHighersA;
            case "highersAB" -> School::getHighersAB;
            case "highersAC" -> School::getHighersAC;
            case "advancedHighersA" -> School::getAdvancedHighersA;
            case "advancedHighersAB" -> School::getAdvancedHighersAB;
            case "advancedHighersAC" -> School::getAdvancedHighersAC;

            // Fallback: Sort by name if field is unrecognized
            default -> s -> null;
        };

        return Comparator.comparing(extractor, Comparator.nullsFirst(Double::compareTo));
    }


    public Page<SchoolAdminDto> getAllSchoolsAdmin(String name, Pageable pageable) {
        Page<School> schoolPage;

        if (name != null && !name.isBlank()) {
            // This will perform a WHERE name LIKE %name% (case-insensitive)
            schoolPage = schoolRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            // Returns the default list if no search term is provided
            schoolPage = schoolRepository.findAll(pageable);
        }

        return schoolPage.map(SchoolAdminMapper::toDto);
    }

    public Optional<SchoolDto> getSchoolByUuid(UUID uuid) {
        School cached = schoolCache.getIfPresent(uuid);
        if (cached != null) {
            return Optional.of(SchoolMapper.toDto(cached));
        }

        return schoolRepository.findByUuid(uuid).map(school -> {
            schoolCache.put(uuid, school);
            return SchoolMapper.toDto(school);
        });
    }

    public Optional<SchoolAdminDto> getSchoolAdminById(Long id) {
        return schoolRepository.findById(id).map(school -> {
            schoolCache.put(school.getUuid(), school);
            return SchoolAdminMapper.toDto(school);
        });
    }

    public List<SchoolDto> findAllSchoolsByIdIn(List<Long> schoolIds) {
        return schoolRepository.findAllById(schoolIds).stream().map(SchoolMapper::toDto).toList();
    }

    public void deleteSchool(UUID uuid) {
        schoolRepository.findByUuid(uuid).ifPresent(school -> {
            schoolRepository.delete(school);
            schoolCache.invalidate(uuid);
        });
    }


    // DB BACKED FILTERS
    public Page<SchoolDto> searchSchoolsByName(String name, Pageable pageable) {
        return schoolRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(SchoolMapper::toDto);
    }

    public Page<SchoolDto> filterByGenderProfile(String genderProfile, Pageable pageable) {
        return schoolRepository.findByGenderProfileIgnoreCase(genderProfile, pageable)
                .map(SchoolMapper::toDto);
    }

    public Page<SchoolDto> filterByReligiousAffiliation(String affiliation, Pageable pageable) {
        return schoolRepository.findByReligiousAffiliationIgnoreCase(affiliation, pageable)
                .map(SchoolMapper::toDto);
    }

    public Page<SchoolDto> filterSchools(String name, String gender, String religion, Pageable pageable) {
        Specification<School> spec = Specification.<School>unrestricted()
                .and(nameContains(name))
                .and(genderEquals(gender))
                .and(religionEquals(religion));

        return schoolRepository.findAll(spec, pageable).map(SchoolMapper::toDto);
    }

    public Page<SchoolAdminDto> filterByEmptyField(String field, Pageable pageable) {
        return schoolRepository.findAll((root, query, cb) -> {
            Path path;
            if (field.equalsIgnoreCase("genderProfile")) {
                path = root.get("genderProfile");
            } else {
                path = root.get(field);
            }
            // For String fields → empty or null
            return cb.or(
                    cb.isNull(path),
                    cb.equal(path, ""),
                    cb.equal(path, " ")
            );
        }, pageable).map(SchoolAdminMapper::toDto);
    }

    private List<School> getAllValidSchools() {
        return schoolCache.asMap().values().stream()
                .filter(s -> !s.isDeleted() && !s.isTest())
                .toList();
    }

    public List<SchoolDto> getFeatured() {
        return getAllValidSchools().stream()
                .filter((school) -> !school.isTest())
                .filter(School::isFeatured)
                .map(SchoolMapper::toDto)
                .toList();
    }

    public Map<String, Set<String>> getTypes() {
        return Map.of(
                "gender", GENDER_TYPES,
                "boarding", BOARDING_TYPES,
                "region", REGION_TYPES,
                "religion", RELIGION_TYPES
        );
    }

    public String uploadImage(MultipartFile file, UUID schoolUuid, Optional<Boolean> isFeaturedImg) throws IOException {
        School school = schoolRepository.findByUuid(schoolUuid)
                .orElseThrow(() -> new RuntimeException("No School with UUID found"));

        String imageUrl = imageStorageService.uploadImage(file);

        if (isFeaturedImg.isPresent()) {
            if (isFeaturedImg.get()) {
                if (school.getTagLineImgUrl() != null) {
                    imageStorageService.deleteImage(school.getTagLineImgUrl());
                }
                school.setTagLineImgUrl(imageUrl);
            }
        } else {
            if (school.getSchoolImgUrl() != null) {
                imageStorageService.deleteImage(school.getSchoolImgUrl());
            }

            school.setSchoolImgUrl(imageUrl);
        }

        schoolRepository.save(school);

        schoolCache.put(schoolUuid, school);

        return imageUrl;
    }

    public boolean hasExamResults(School school, String examType) {
        if (school == null || examType == null) {
            return false;
        }

        return switch (examType) {
            case "gcse" -> anyNotNull(
                    school.getPercentageWithResultsAt9OrBetter(),
                    school.getPercentageWithResultsAt8OrBetter(),
                    school.getPercentageWithResultsAt7OrBetter(),
                    school.getPercentageWithResultsAt6OrBetter(),
                    school.getPercentageWithResultsAt5OrBetter(),
                    school.getPercentageWithResultsAt4OrBetter()
            );

            case "igcse" -> anyNotNull(
                    school.getIgcse9OrBetter(), school.getIgcse8OrBetter(),
                    school.getIgcse7OrBetter(), school.getIgcse6OrBetter(),
                    school.getIgcse5OrBetter(), school.getIgcse4OrBetter(),
                    school.getIgcseAOrBetter(), school.getIgcseBOrBetter(),
                    school.getIgcseCOrBetter()
            );

            case "aLevel" -> anyNotNull(
                    school.getAlevelAstar(), school.getAlevelA_Astar(),
                    school.getAlevelA_B(), school.getAlevelA_C()
            );

            case "national5s" -> anyNotNull(
                    school.getNational5sA(), school.getNational5sAB(), school.getNational5sAc()
            );

            case "highers" -> anyNotNull(
                    school.getHighersA(), school.getHighersAB(), school.getHighersAC()
            );

            case "advancedHigher" -> anyNotNull(
                    school.getAdvancedHighersA(), school.getAdvancedHighersAB(), school.getAdvancedHighersAC()
            );

            case "advancedPlacement" -> school.getAdvancedPlacement() != null;

            case "ibPoints" -> school.getIbPoints() != null;

            default -> false;
        };
    }

    private boolean anyNotNull(Object... fields) {
        return Stream.of(fields).anyMatch(Objects::nonNull);
    }
}
