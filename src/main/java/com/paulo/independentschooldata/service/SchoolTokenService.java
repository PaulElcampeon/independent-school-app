package com.paulo.independentschooldata.service;

import com.paulo.independentschooldata.dto.SchoolTokenDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class SchoolTokenService {

    private final Map<Long, SchoolTokenDto> tokenStore = new HashMap<>();
    private final Random random = new Random();
    private static final int TOKEN_LENGTH = 10;
    private static final int TTL_MINUTES = 10;

    /**
     * Generate two random codes for a school:
     * - accessCode: used for authentication
     * - urlCode: used in the URL path
     */
    public SchoolTokenDto generateTokens(Long schoolId) {
        String accessCode = generateRandomCode();
        String urlCode = generateRandomCode();
        
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TTL_MINUTES);
        
        SchoolTokenDto tokenDto = SchoolTokenDto.builder()
                .accessCode(accessCode)
                .urlCode(urlCode)
                .schoolId(schoolId)
                .expiresAt(expiresAt)
                .build();
        
        tokenStore.put(schoolId, tokenDto);
        log.info("Generated tokens for school ID: {}, expires at: {}", schoolId, expiresAt);
        
        return tokenDto;
    }

    /**
     * Validate access code and return school ID if valid
     */
    public Optional<Long> validateAccessCode(String accessCode) {
        return tokenStore.entrySet().stream()
                .filter(entry -> {
                    SchoolTokenDto token = entry.getValue();
                    return token.getAccessCode().equals(accessCode) 
                            && token.getExpiresAt().isAfter(LocalDateTime.now());
                })
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Validate URL code and return school ID if valid
     */
    public Optional<Long> validateUrlCode(String urlCode) {
        return tokenStore.entrySet().stream()
                .filter(entry -> {
                    SchoolTokenDto token = entry.getValue();
                    return token.getUrlCode().equals(urlCode) 
                            && token.getExpiresAt().isAfter(LocalDateTime.now());
                })
                .map(Map.Entry::getKey)
                .findFirst();
    }

    /**
     * Get token by school ID
     */
    public Optional<SchoolTokenDto> getTokenBySchoolId(Long schoolId) {
        SchoolTokenDto token = tokenStore.get(schoolId);
        if (token != null && token.getExpiresAt().isAfter(LocalDateTime.now())) {
            return Optional.of(token);
        }
        return Optional.empty();
    }

    /**
     * Clean up expired tokens (runs every minute)
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenStore.entrySet().removeIf(entry -> 
                entry.getValue().getExpiresAt().isBefore(now)
        );
        log.debug("Cleaned up expired tokens. Remaining: {}", tokenStore.size());
    }

    /**
     * Generate a random alphanumeric code
     */
    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    public boolean isValidAccessCodeForSchool(Long schoolId, String accessCode) {
        SchoolTokenDto token = tokenStore.get(schoolId);

        if (token == null) {
            return false;
        }

        boolean notExpired = token.getExpiresAt().isAfter(LocalDateTime.now());
        boolean codeMatches = token.getAccessCode().equals(accessCode);

        return codeMatches && notExpired;
    }
}

