package com.paulo.independentschooldata.service;

import com.paulo.independentschooldata.domain.School;
import com.paulo.independentschooldata.domain.User;
import com.paulo.independentschooldata.domain.enums.UserRole;
import com.paulo.independentschooldata.dto.EnableUserAccountRequest;
import com.paulo.independentschooldata.dto.SchoolAdminUserRequest;
import com.paulo.independentschooldata.dto.UserDto;
import com.paulo.independentschooldata.dto.UserRequest;
import com.paulo.independentschooldata.exceptions.BadRequestException;
import com.paulo.independentschooldata.exceptions.DuplicateEmailException;
import com.paulo.independentschooldata.exceptions.ResourceNotFoundException;
import com.paulo.independentschooldata.mappers.UserMapper;
import com.paulo.independentschooldata.repos.SchoolRepository;
import com.paulo.independentschooldata.repos.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SchoolRepository schoolRepository;
    private final MailService mailService;

    public UserDto createUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        User user = new User();
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setEmail(userRequest.getEmail());
        user.setRole(UserRole.USER);

        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    public UserDto createSchoolAdminUser(SchoolAdminUserRequest schoolAdminUserRequest) {
        if (userRepository.existsByEmail(schoolAdminUserRequest.getEmail())) {
            throw new DuplicateEmailException("Email already exists");
        }

        School school = schoolRepository.findByUuid(schoolAdminUserRequest.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School", "uuid", schoolAdminUserRequest.getSchoolId()));

        if (!school.getName().equalsIgnoreCase(schoolAdminUserRequest.getSchoolName())) {
            throw new BadRequestException("School name does not match the registered school");
        }

        User user = new User();
        user.setPassword(passwordEncoder.encode(schoolAdminUserRequest.getPassword()));
        user.setEmail(schoolAdminUserRequest.getEmail());
        user.setRole(UserRole.SCHOOL);
        user.setEnabled(false);
        user.setSchoolId(school.getId());

        User savedUser = userRepository.save(user);

        mailService.sendSimpleEmailToOurSelves("New School register", "School: " + schoolAdminUserRequest.getSchoolName() + "\nEmail: " + schoolAdminUserRequest.getEmail());

        String body = "Dear School Administrator\n\n" +
                "Thank you for submitting your registration. We are writing to let you know that we have successfully received your application.\n\n" +
                "Our team is currently performing a standard validation of the information provided. Once completed, we will send a formal confirmation email to finalize the process.\n\n" +
                "Best regards,\n\n" +
                "Paul\n" +
                "ISchoolSearch Team";

        mailService.sendSimpleEmail(schoolAdminUserRequest.getSchoolName() + " Registration Received - ISchoolSearch", schoolAdminUserRequest.getEmail(), body);

        return userMapper.toDto(savedUser);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    public void enableUserAccount(EnableUserAccountRequest enableUserAccountRequest) {
        User user = userRepository.findById(enableUserAccountRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", enableUserAccountRequest.getUserId()));
        user.setEnabled(true);
        userRepository.save(user);

        School school = schoolRepository.findById(user.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School", "id", user.getSchoolId()));

        String subject = "Registration Approved - " + school.getName() + " | ISchoolSearch";

        String body = "Dear School Administrator,\n\n" +
                "We are pleased to inform you that your registration for " + school.getName() + " has been successfully validated and approved.\n\n" +
                "Your account is now active. You can log in to the ISchoolSearch portal using your registered email address to begin managing your school's profile.\n\n" +
                "Welcome to our platform! If you have any questions, feel free to reach out to our support team.\n\n" +
                "Best regards,\n\n" +
                "Paul\n" +
                "ISchoolSearch Team";

        mailService.sendSimpleEmail(subject, user.getEmail(), body);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }
}