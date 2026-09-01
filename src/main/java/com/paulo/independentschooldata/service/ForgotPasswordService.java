package com.paulo.independentschooldata.service;

import com.paulo.independentschooldata.domain.ForgotPasswordNotification;
import com.paulo.independentschooldata.domain.User;
import com.paulo.independentschooldata.domain.enums.UserRole;
import com.paulo.independentschooldata.dto.ForgotPasswordRequest;
import com.paulo.independentschooldata.dto.ResetPasswordRequest;
import com.paulo.independentschooldata.exceptions.ResourceNotFoundException;
import com.paulo.independentschooldata.repos.ForgotPasswordNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordService {

    private final ForgotPasswordNotificationRepository forgotPasswordNotificationRepository;
    private final UserService userService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    public void sendResetPasswordLink(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userService.findUserByEmail(forgotPasswordRequest.getEmail());

        if (!user.isEnabled()) {
            return;
        }

        if (!user.getRole().equals(UserRole.SCHOOL)) {
            return;
        }

        if (!user.getSchoolId().equals(forgotPasswordRequest.getSchoolId())) {
            return;
        }

        ForgotPasswordNotification forgotPasswordNotification = ForgotPasswordNotification.builder()
                .email(forgotPasswordRequest.getEmail())
                .isValid(true)
                .schoolName(forgotPasswordRequest.getSchoolName())
                .schoolId(forgotPasswordRequest.getSchoolId())
                .build();

        forgotPasswordNotification = forgotPasswordNotificationRepository.save(forgotPasswordNotification);

        String resetLink = "https://independent-schools-uk/reset-password?token="
                + forgotPasswordNotification.getCode();

        String subject = "Independent Schools UK – Password Reset Request";

        String body = String.format("""
                Hello,
                
                We received a request to reset the password for your Independent Schools UK account.
                
                To reset your password, please use the secure link below:
                %s
                
                If you did not request this reset, you can safely ignore this email. 
                For security reasons, the link will expire shortly.
                
                Kind regards,
                Independent Schools UK Support Team
                """, resetLink);

        mailService.sendSimpleEmail(subject, forgotPasswordNotification.getEmail(), body);
    }

    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        ForgotPasswordNotification forgotPasswordNotification = forgotPasswordNotificationRepository.findByCode(resetPasswordRequest.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("Password reset request", "code", resetPasswordRequest.getCode()));

        if (forgotPasswordNotification.isValid()) {
//            if (forgotPasswordNotification.getEmail().equalsIgnoreCase(resetPasswordRequest.getEmail())) {
            User user = userService.findUserByEmail(forgotPasswordNotification.getEmail());
            user.setPassword(passwordEncoder.encode(resetPasswordRequest.getPassword()));
            userService.updateUser(user);

            forgotPasswordNotification.setValid(false);

            forgotPasswordNotificationRepository.save(forgotPasswordNotification);

            return;
//            }
        }
        log.info("No valid forgot password notification was found with code: {}", resetPasswordRequest.getCode());
    }
}
