package com.paulo.independentschooldata.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class RateLimiterService {

    private static final int MAX_REQUESTS = 15;               // per 10 seconds
    private static final int BAN_MINUTES = 60;                 // ban duration

    private final MailService mailService;

    // Counts requests for each IP, expiring after 10 seconds
    private final Cache<String, AtomicInteger> requestCounts = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .maximumSize(1000)
            .build();

    // Ban list: auto-expires after BAN_MINUTES
    private final Cache<String, Instant> bannedIps = Caffeine.newBuilder()
            .expireAfterWrite(BAN_MINUTES, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    public RateLimiterService(MailService mailService) {
        this.mailService = mailService;
    }

    /**
     * Check if an IP is currently banned
     */
    public boolean isBanned(String ip) {
        return bannedIps.getIfPresent(ip) != null;
    }

    /**
     * Record an IP request; ban if requests exceed limit
     */
    public void recordRequest(String ip) {
        AtomicInteger count = requestCounts.get(ip, k -> new AtomicInteger(0));
        int newCount = count.incrementAndGet();
        log.info("IP: {}, Count: {}", ip, newCount);
        if (newCount > MAX_REQUESTS) {
            bannedIps.put(ip, Instant.now());
        }
    }

    /**
     * Clear all banned IPs manually
     */
    public void clearBannedIps() {
        bannedIps.invalidateAll();
    }

    /**
     * Get list of currently banned IPs
     */
    public List<String> bannedIps() {
        return bannedIps.asMap().keySet().stream().toList();
    }

    /**
     * Remove a specific IP from the ban list
     */
    public void removeBannedIp(String ip) {
        bannedIps.invalidate(ip);
    }

    /**
     * Manually add an IP to the ban list
     */
    public void addBannedIp(String ip) {
        bannedIps.put(ip, Instant.now());
        mailService.sendSimpleEmailToOurSelves("Banned IP's", bannedIps.asMap().keySet().toString());

    }
}
