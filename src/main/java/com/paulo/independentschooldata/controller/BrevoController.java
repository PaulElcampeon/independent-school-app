package com.paulo.independentschooldata.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paulo.independentschooldata.dto.BrevoEventDto;
import com.paulo.independentschooldata.service.SchoolEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brevo")
@Slf4j
public class BrevoController {

    private final SchoolEmailService schoolEmailService;
    private final ObjectMapper objectMapper;

    public BrevoController(ObjectMapper objectMapper, SchoolEmailService schoolEmailService) {
        this.objectMapper = objectMapper;
        this.schoolEmailService = schoolEmailService;
    }

    @PostMapping("/email/events")
    public ResponseEntity<Void> handleEmailEvent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody String rawJson) {

        log.info("RAW PAYLOAD FROM BREVO: {}", rawJson);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized! Authorization header is missing or invalid.");
            return ResponseEntity.status(401).build();
        }

        String receivedToken = authHeader.substring(7);

        if (!receivedToken.equals("019b8fa5-4864-755f-859b-a019850e3040")) {
            log.warn("Invalid Token received: {}", receivedToken);
            return ResponseEntity.status(403).build();
        }

        try {
            BrevoEventDto payload = objectMapper.readValue(rawJson, BrevoEventDto.class);
            String eventType = payload.getEvent();

            log.info("Converted DTO: {}", payload);
            log.info("Event Type: {}, Email: {}", eventType, payload.getEmail());

            switch (eventType) {
                case "request":
                    log.info("Email request accepted for: {}", payload.getEmail());
                    break;

                case "delivered":
                    log.info("✅ Email DELIVERED to: {}", payload.getEmail());
                    schoolEmailService.markEmailSent(payload.getSubject(), payload.getEmail());
                    break;

                case "hard_bounce":
                    log.error("❌ HARD BOUNCE: {} does not exist. Stop emailing this user!", payload.getEmail());
                    schoolEmailService.markEmailBounced(payload.getSubject(), payload.getEmail(), payload.getReason());
                    break;

                case "soft_bounce":
                    log.warn("⚠️ SOFT BOUNCE for: {}. Reason: {}", payload.getEmail(), payload.getReason());
                    schoolEmailService.markEmailBounced(payload.getSubject(), payload.getEmail(), payload.getReason());
                    // Inbox might be full, try again later
                    break;

                case "opened":
                    log.info("👀 Email OPENED by: {}", payload.getEmail());
                    break;

                    case "unique_opened":
                    log.info("👀 Email UNIQUE OPENED by: {}", payload.getEmail());
                    break;

                case "click":
                    log.info("🔗 Link CLICKED by: {}", payload.getEmail());
                    break;

                case "spam":
                    log.error("🛑 SPAM COMPLAINT from: {}. Unsubscribe them immediately!", payload.getEmail());
                    break;

                case "unsubscribed":
                    log.info("Bye! User {} unsubscribed.", payload.getEmail());
                    break;

                default:
                    log.info("Other Event Received: {} for {}", eventType, payload.getEmail());
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Brevo JSON", e);
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().build();
    }
}
