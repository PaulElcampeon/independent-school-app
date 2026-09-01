package com.paulo.independentschooldata.controller;

import com.paulo.independentschooldata.dto.SchoolAdminDto;
import com.paulo.independentschooldata.dto.SchoolDto;
import com.paulo.independentschooldata.dto.TokenGenerationResponse;
import com.paulo.independentschooldata.service.SchoolService;
import com.paulo.independentschooldata.service.SchoolTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
@Slf4j
public class SchoolController {

    private final SchoolService schoolService;
    private final SchoolTokenService schoolTokenService;

    // Create school
    @PostMapping("/admin")
    public SchoolAdminDto createSchool(@RequestBody SchoolAdminDto dto) {
        return schoolService.saveSchoolAdmin(dto);
    }

    @GetMapping("/featured")
    public List<SchoolDto> getFeaturedSchools() {
        return schoolService.getFeatured();
    }

    @GetMapping("/types")
    public Map<String, Set<String>> getTypes() {
        return schoolService.getTypes();
    }

    // Get all schools with pagination
    @GetMapping("/all")
    public Page<SchoolDto> getAllSchools(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String examType,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) BigDecimal minFee,
            @RequestParam(required = false) BigDecimal maxFee,
            @RequestParam(required = false) String religion,
            @RequestParam(required = false) String boarding,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Boolean isSend,
            @RequestParam(required = false) String feeFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        if (size > 48) {
            size = 48;
        }

        Pageable pageable = PageRequest.of(page,
                size,
                direction.equalsIgnoreCase("asc")
                        ? org.springframework.data.domain.Sort.by(sortBy).ascending()
                        : org.springframework.data.domain.Sort.by(sortBy).descending());
        return schoolService.getAllSchools(minFee, maxFee, examType, name, isSend, gender, religion, boarding, region, feeFilter, pageable);
    }

    @GetMapping("/admin")
    public Page<SchoolAdminDto> getAllSchoolsAdmin(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        if (size > 200) {
            size = 200;
        }

        Pageable pageable = PageRequest.of(page,
                size,
                direction.equalsIgnoreCase("asc")
                        ? org.springframework.data.domain.Sort.by(sortBy).ascending()
                        : org.springframework.data.domain.Sort.by(sortBy).descending());
        return schoolService.getAllSchoolsAdmin(name, pageable);
    }

    // Get school by UUID
    @GetMapping("/{uuid}")
    public Optional<SchoolDto> getSchoolByUuid(@PathVariable UUID uuid) {
        return schoolService.getSchoolByUuid(uuid);
    }

    @PutMapping("/admin/code/{code}/uuid/{uuid}")
    public ResponseEntity<SchoolAdminDto> updateSchool(@PathVariable String code, @PathVariable UUID uuid, @RequestBody SchoolAdminDto dto) {

        Optional<Long> schoolIdOpt = schoolTokenService.validateAccessCode(code);

        if (schoolIdOpt.isEmpty()) {
            log.warn("Invalid school admin code attempted: {}", code);
            return ResponseEntity.status(401).build();
        }

        if (!schoolIdOpt.get().equals(dto.getId())) {
            log.warn("Access code does not match correct school id: {}", dto.getId());
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(schoolService.updateAdminSchool(uuid, dto));
    }

    // Update school
    @PutMapping("/admin/{uuid}")
    public SchoolAdminDto updateSchool(@PathVariable UUID uuid, @RequestBody SchoolAdminDto dto) {
        return schoolService.updateAdminSchool(uuid, dto);
    }

    // Delete school
    @DeleteMapping("/admin/{uuid}")
    public void deleteSchool(@PathVariable UUID uuid) {
        schoolService.deleteSchool(uuid);
    }

    // Search by name
    @GetMapping("/admin/search")
    public Page<SchoolDto> searchSchoolsAdmin(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return schoolService.searchSchoolsByName(name, pageable);
    }

    // Search by name
    @GetMapping("/search")
    public Page<SchoolDto> searchSchools(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return schoolService.searchSchoolsByName(name, pageable);
    }

    // Filter by gender profile
    @GetMapping("/filter/gender")
    public Page<SchoolDto> filterByGenderProfile(
            @RequestParam String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return schoolService.filterByGenderProfile(gender, pageable);
    }

    // Filter by religious affiliation
    @GetMapping("/filter/religion")
    public Page<SchoolDto> filterByReligion(
            @RequestParam String religion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return schoolService.filterByReligiousAffiliation(religion, pageable);
    }

    @GetMapping("/filter")
    public Page<SchoolDto> filterSchools(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String religion,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return schoolService.filterSchools(name, gender, religion, pageable);
    }

    /**
     * Generate tokens for school admin access
     *
     * @param schoolId The ID of the school
     * @return TokenGenerationResponse with access code, URL code, and full URL
     */
    @PostMapping("/{schoolId}/generate-token")
    public ResponseEntity<TokenGenerationResponse> generateTokenForSchool(@PathVariable Long schoolId) {
        var tokenDto = schoolTokenService.generateTokens(schoolId);

//        String url = "http://localhost:8080/school-admin";
        String url = "https://idsuk-management.website/school-admin";

        TokenGenerationResponse response = TokenGenerationResponse.builder()
                .accessCode(tokenDto.getAccessCode())
                .urlCode(tokenDto.getUrlCode())
                .url(url)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter/empty")
    public Page<SchoolAdminDto> filterSchoolsWhereFieldIsEmpty(
            @RequestParam String field,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return schoolService.filterByEmptyField(field, pageable);
    }

    @PostMapping("/uploadImage/{schoolUuid}")
    public ResponseEntity<String> handleImageUpload(@RequestParam("file") MultipartFile file,
                                                    @PathVariable("schoolUuid") UUID schoolUuid,
                                                    @RequestParam("featured") Optional<Boolean> featured) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
            String imageUrl = schoolService.uploadImage(file, schoolUuid, featured);

            return ResponseEntity.ok(imageUrl);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
        }
    }
}
