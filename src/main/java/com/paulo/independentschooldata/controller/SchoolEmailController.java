package com.paulo.independentschooldata.controller;

import com.paulo.independentschooldata.dto.BatchSchoolEmailDto;
import com.paulo.independentschooldata.dto.SchoolEmailDto;
import com.paulo.independentschooldata.service.SchoolEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/school-emails")
@RequiredArgsConstructor
@Slf4j
public class SchoolEmailController {

    private final SchoolEmailService schoolEmailService;

    @GetMapping("/remaining-limit")
    public ResponseEntity<Integer> getRemainingLimit() {
        return ResponseEntity.ok(schoolEmailService.getRemainingEmails());
    }

    @GetMapping
    public ResponseEntity<Page<SchoolEmailDto>> getSchoolEmails(
            @RequestParam(required = false) String emailType,
            @RequestParam(required = false) Boolean delivered,
            @RequestParam(required = false) Boolean bounced,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(schoolEmailService.getEmails(delivered, bounced, emailType, pageable));
    }

    @GetMapping("/subject/{subject}")
    public ResponseEntity<Page<SchoolEmailDto>> getSchoolEmailsBySubject(
            @PathVariable String subject,
            @RequestParam(required = false) Boolean delivered,
            @RequestParam(required = false) Boolean bounced,
            Pageable pageable) {
        return ResponseEntity.ok(schoolEmailService.getEmailsBySubject(subject, delivered, bounced, pageable));
    }

    @GetMapping("/uuid/{schoolUuid}")
    public ResponseEntity<Page<SchoolEmailDto>> getSchoolEmailsBySchoolUuid(
            @PathVariable String schoolUuid,
            @RequestParam(required = false) Boolean delivered,
            @RequestParam(required = false) Boolean bounced,
            Pageable pageable) {
        return ResponseEntity.ok(schoolEmailService.getEmailsBySchoolUuid(schoolUuid, delivered, bounced, pageable));
    }

    @GetMapping("/id/{schoolId}")
    public ResponseEntity<Page<SchoolEmailDto>> getSchoolEmailsBySchoolId(
            @PathVariable Long schoolId,
            @RequestParam(required = false) Boolean delivered,
            @RequestParam(required = false) Boolean bounced,
            Pageable pageable) {
        return ResponseEntity.ok(schoolEmailService.getEmailsBySchoolId(schoolId, delivered, bounced, pageable));
    }


//    @GetMapping
//    public ResponseEntity<List<SchoolEmailDto>> getSchoolEmails(
//            @RequestParam(required = false) String emailType,
//            @RequestParam(required = false) Boolean delivered,
//            @RequestParam(required = false) Boolean bounced) {
//        return ResponseEntity.ok(schoolEmailService.getEmails(delivered, bounced, emailType));
//    }
//
//    @GetMapping("/subject/{subject}")
//    public ResponseEntity<List<SchoolEmailDto>> getSchoolEmailsBySubject(
//            @PathVariable String subject,
//            @RequestParam(required = false) Boolean delivered,
//            @RequestParam(required = false) Boolean bounced) {
//        return ResponseEntity.ok(schoolEmailService.getEmailsBySubject(subject, delivered, bounced));
//    }
//
//    @GetMapping("/uuid/{schoolUuid}")
//    public ResponseEntity<List<SchoolEmailDto>> getSchoolEmailsBySchoolUuid(
//            @PathVariable String schoolUuid,
//            @RequestParam(required = false) Boolean delivered,
//            @RequestParam(required = false) Boolean bounced) {
//        return ResponseEntity.ok(schoolEmailService.getEmailsBySchoolUuid(schoolUuid, delivered, bounced));
//    }
//
//    @GetMapping("/id/{schoolId}")
//    public ResponseEntity<List<SchoolEmailDto>> getSchoolEmailsBySchoolUuid(
//            @PathVariable Long schoolId,
//            @RequestParam(required = false) Boolean delivered,
//            @RequestParam(required = false) Boolean bounced) {
//        return ResponseEntity.ok(schoolEmailService.getEmailsBySchoolId(schoolId, delivered, bounced));
//    }

    @PostMapping("/batch")
    public ResponseEntity sendSchoolEmail(@RequestBody BatchSchoolEmailDto batchSchoolEmailDto) {
        schoolEmailService.sendEmailToSchools(batchSchoolEmailDto);
        return ResponseEntity.ok().build();
    }
}
