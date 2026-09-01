package com.paulo.independentschooldata.controller;

import com.paulo.independentschooldata.domain.SubscriptionRecord;
import com.paulo.independentschooldata.domain.User;
import com.paulo.independentschooldata.domain.enums.UserRole;
import com.paulo.independentschooldata.dto.*;
import com.paulo.independentschooldata.config.jwt.JwtTokenProvider;
import com.paulo.independentschooldata.exceptions.BadRequestException;
import com.paulo.independentschooldata.exceptions.ResourceNotFoundException;
import com.paulo.independentschooldata.exceptions.UnauthorizedException;
import com.paulo.independentschooldata.repos.SubscriptionRepository;
import com.paulo.independentschooldata.service.ForgotPasswordService;
import com.paulo.independentschooldata.service.SchoolService;
import com.paulo.independentschooldata.service.SchoolTokenService;
import com.paulo.independentschooldata.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final SchoolTokenService schoolTokenService;
    private final SchoolService schoolService;
    private final ForgotPasswordService forgotPasswordService;
    private final SubscriptionRepository subscriptionRepository;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserRequest userRequest) {
        UserDto userResponse = userService.createUser(userRequest);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/register/school")
    public ResponseEntity<UserDto> registerSchool(@RequestBody SchoolAdminUserRequest schoolAdminUserRequest) {
        UserDto userResponse = userService.createSchoolAdminUser(schoolAdminUserRequest);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody UserRequest userRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userRequest.getEmail(),
                        userRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        log.info("User {} {} logged in successfully", authentication.getName(), authentication.getPrincipal().toString());

        log.info("User {} logged in successfully", userRequest.getEmail());

        User user = userService.findUserByEmail(userRequest.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("userId", user.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/school")
    public ResponseEntity<Map<String, Object>> loginSchoolAdmin(@RequestBody UserRequest userRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userRequest.getEmail(),
                        userRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        log.info("User {} {} logged in successfully", authentication.getName(), authentication.getPrincipal().toString());

        log.info("User {} logged in successfully", userRequest.getEmail());

        User user = userService.findUserByEmail(userRequest.getEmail());

        if (user.getSchoolId() == null) {
            throw new BadRequestException("User is not associated with any school");
        }

        SchoolAdminDto schoolAdminDto = null;
        if (user.getRole() == UserRole.SCHOOL) {
            schoolAdminDto = schoolService.getSchoolAdminById(user.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("School", "id", user.getSchoolId()));
        }

        List<SubscriptionRecord> subscriptionRecords = subscriptionRepository.findBySchoolId(schoolAdminDto.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("userId", user.getId());
        response.put("school", schoolAdminDto);
        response.put("subscriptions", subscriptionRecords);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/school-admin")
    public ResponseEntity<SchoolAdminDto> validateSchoolAdminCode(@RequestBody CodeValidationRequest request) {
        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            throw new BadRequestException("Access code is required");
        }

        Optional<Long> schoolIdOpt = schoolTokenService.validateAccessCode(code);

        if (schoolIdOpt.isEmpty()) {
            schoolIdOpt = schoolTokenService.validateUrlCode(code);
        }

        if (schoolIdOpt.isEmpty()) {
            log.warn("Invalid school admin code attempted: {}", code);
            throw new UnauthorizedException("Invalid access code");
        }

        Long schoolId = schoolIdOpt.get();

        return schoolService.getSchoolAdminById(schoolId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("School", "id", schoolId));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<UserDto> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        forgotPasswordService.sendResetPasswordLink(forgotPasswordRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<UserDto> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest) {
        forgotPasswordService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody ValidateTokenRequest validateTokenRequest) {

        boolean isValidToken = tokenProvider.validateToken(validateTokenRequest.getJwtToken());

        if (!isValidToken) {
            throw new UnauthorizedException("Invalid or expired token");
        }

        String userEmail = tokenProvider.getUsernameFromToken(validateTokenRequest.getJwtToken());

        if (!userEmail.equalsIgnoreCase(validateTokenRequest.getEmail())) {
            log.warn("Token email mismatch: expected {} but got {}", validateTokenRequest.getEmail(), userEmail);
            throw new UnauthorizedException("Token does not match the provided email");
        }

        User user = userService.findUserByEmail(userEmail);

        if (user.getSchoolId() == null) {
            throw new BadRequestException("User is not associated with any school");
        }

        SchoolAdminDto schoolAdminDto = null;
        if (user.getRole() == UserRole.SCHOOL) {
            schoolAdminDto = schoolService.getSchoolAdminById(user.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("School", "id", user.getSchoolId()));
        }

        List<SubscriptionRecord> subscriptionRecords = subscriptionRepository.findBySchoolId(schoolAdminDto.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("token", validateTokenRequest.getJwtToken());
        response.put("type", "Bearer");
        response.put("userId", user.getId());
        response.put("school", schoolAdminDto);
        response.put("subscriptions", subscriptionRecords);

        return ResponseEntity.ok().body(response);
    }
}
