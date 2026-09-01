package com.paulo.independentschooldata.controller;

import com.paulo.independentschooldata.dto.SchoolMapDto;
import com.paulo.independentschooldata.service.SchoolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@Slf4j
public class MapController {

    private final SchoolService schoolService;

    @GetMapping
    public Page<SchoolMapDto> getAllSchools(
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
            @RequestParam(defaultValue = "1500") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {


        Pageable pageable = PageRequest.of(page,
                size,
                direction.equalsIgnoreCase("asc")
                        ? org.springframework.data.domain.Sort.by(sortBy).ascending()
                        : org.springframework.data.domain.Sort.by(sortBy).descending());
        return schoolService.getAllSchoolsForMap(minFee, maxFee, examType, name, isSend, gender, religion, boarding, region, feeFilter, pageable);
    }
}
