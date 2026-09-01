package com.paulo.independentschooldata.controller;

import com.paulo.independentschooldata.domain.enums.EmailType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/constants")
@RequiredArgsConstructor
@Slf4j
public class ConstantsController {

    @GetMapping("/email-types")
    public ResponseEntity<List<EmailType>> getEmailTypes() {
        log.info("Fetching email types");
        return ResponseEntity.ok(List.of(EmailType.values()));
    }
}
