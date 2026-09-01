package com.paulo.independentschooldata.config;

import com.paulo.independentschooldata.service.RateLimiterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Apply rate-limiting if the path contains any of these segments
        boolean shouldRateLimit =
                path.contains("/api/v1/schools") ||
                        path.contains("/api/v1/admin")   ||
                        path.contains("/api/auth");

        if (shouldRateLimit) {
            String ip = request.getRemoteAddr();

            if (rateLimiterService.isBanned(ip)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests — temporarily banned");
                return;
            }

            rateLimiterService.recordRequest(ip);
        }

        filterChain.doFilter(request, response);
    }
}
