package com.paulo.independentschooldata.controller;

import com.paulo.independentschooldata.dto.EnableUserAccountRequest;
import com.paulo.independentschooldata.dto.EnquiryRequest;
import com.paulo.independentschooldata.service.RateLimiterService;
import com.paulo.independentschooldata.service.SchoolService;
import com.paulo.independentschooldata.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final RateLimiterService rateLimiterService;
    private final UserService userService;
    private final SchoolService schoolService;

    @GetMapping("/reload-school-cache")
    public ResponseEntity reloadSchoolCache(Principal principal) {
        log.info("Reloading school cache initialized by: {}", principal.getName());
        schoolService.reloadCache();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/enquire")
    public ResponseEntity enquire(@RequestBody EnquiryRequest enquiryRequest) {
        log.info("Enquiry: {}", enquiryRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/banned-ips/{ip}")
    public ResponseEntity addBannedIp(@PathVariable String ip) {
        rateLimiterService.addBannedIp(ip);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/banned-ips/{ip}")
    public ResponseEntity removeBannedIp(@PathVariable String ip) {
        rateLimiterService.removeBannedIp(ip);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/banned-ips")
    public List<String> getBannedIps() {
        return rateLimiterService.bannedIps();
    }

    @GetMapping("/banned-ips/clear")
    public ResponseEntity clearBannedIps() {
        rateLimiterService.clearBannedIps();
        return ResponseEntity.ok().build();
    }

    @PutMapping("/enable/user")
    public ResponseEntity enableUserAccount(@RequestBody EnableUserAccountRequest enableUserAccountRequest) {
        userService.enableUserAccount(enableUserAccountRequest);
        return ResponseEntity.ok().build();
    }
}
