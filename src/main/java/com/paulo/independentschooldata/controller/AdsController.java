package com.paulo.independentschooldata.controller;

import com.paulo.independentschooldata.service.AdsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@Slf4j
public class AdsController {

    private final AdsService adsService;

    @GetMapping("/is-enabled")
    public ResponseEntity<Boolean> isEnabled() {
        return ResponseEntity.ok(adsService.getCurrentStateOfAdds());
    }

    @PutMapping("/enable")
    public ResponseEntity<Boolean> enable() {
        return ResponseEntity.ok(adsService.enable());
    }

    @PutMapping("/disable")
    public ResponseEntity<Boolean> disable() {
        return ResponseEntity.ok(adsService.disable());
    }
}
