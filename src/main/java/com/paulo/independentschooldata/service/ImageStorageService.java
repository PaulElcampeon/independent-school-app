package com.paulo.independentschooldata.service;

import com.paulo.independentschooldata.domain.School;
import com.paulo.independentschooldata.domain.User;
import com.paulo.independentschooldata.exceptions.ResourceNotFoundException;
import com.paulo.independentschooldata.exceptions.StorageException;
import com.paulo.independentschooldata.repos.SchoolRepository;
import com.paulo.independentschooldata.repos.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageStorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String publicUrl;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    public ImageStorageService(
            @Value("${cloudflare.r2.access-key}") String accessKey,
            @Value("${cloudflare.r2.secret-key}") String secretKey,
            @Value("${cloudflare.r2.endpoint}") String endpoint,
            @Value("${cloudflare.r2.bucket-name}") String bucketName,
            @Value("${cloudflare.r2.public-url}") String publicUrl, SchoolRepository schoolRepository, UserRepository userRepository) {

        this.bucketName = bucketName;
        this.publicUrl = publicUrl;
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;

        // Initialize S3Client for R2 (S3-compatible)
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }

    public String testConnection() {
        try {
            var buckets = s3Client.listBuckets().buckets();
            return "SUCCESS! Buckets: " + buckets.stream()
                    .map(b -> b.name())
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }


    public String uploadImage(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String key = "uploads/" + UUID.randomUUID() + "_" + originalFilename;

        // Read the file into memory first
        byte[] fileBytes = file.getBytes();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength((long) fileBytes.length)  // Use byte array length
                .build();

        // Upload using fromBytes instead of fromInputStream
        s3Client.putObject(putObjectRequest,
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(fileBytes));

//        School school = schoolRepository.findByUuid(schoolUuid)
//                .orElseThrow(() -> new RuntimeException("No School with UUID found"));
//
//        if (isFeaturedImg.isPresent()) {
//            if (isFeaturedImg.get()) {
//                school.setTagLineImgUrl(publicUrl + key);
//            }
//        } else {
//            if (school.getSchoolImgUrl() != null) {
//                deleteImage(school.getSchoolImgUrl());
//            }
//            school.setSchoolImgUrl(publicUrl + key);
//        }
//        schoolRepository.save(school);

        log.info("Uploaded image with url: {}", publicUrl + key);

        return publicUrl + key;
    }

    public String uploadImage(MultipartFile file, String userEmail) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String key = "uploads/" + UUID.randomUUID() + "_" + originalFilename;

        // Read the file into memory first
        byte[] fileBytes = file.getBytes();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .contentLength((long) fileBytes.length)  // Use byte array length
                .build();

        // Upload using fromBytes instead of fromInputStream
        s3Client.putObject(putObjectRequest,
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(fileBytes));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        School school = schoolRepository.findById(user.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School", "id", user.getSchoolId()));

        if (school.getSchoolImgUrl() != null) {
            deleteImage(school.getSchoolImgUrl());
        }

        school.setSchoolImgUrl(publicUrl + key);
        schoolRepository.save(school);

        return publicUrl + key;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            // 1. Clean the publicUrl to ensure it doesn't have a trailing slash for consistent parsing
            String baseUri = publicUrl.endsWith("/")
                    ? publicUrl.substring(0, publicUrl.length() - 1)
                    : publicUrl;

            // 2. Extract the key by removing the base URL and the leading slash
            // This turns "https://...r2.dev/profiles/..." into "profiles/..."
            String key = imageUrl.replace(baseUri, "");
            if (key.startsWith("/")) {
                key = key.substring(1);
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("Deleting image with url: {}", imageUrl);

        } catch (Exception e) {
            log.error("Failed to delete object from R2 storage: {}", e.getMessage(), e);
            throw new StorageException("Could not delete image from R2 storage", e);
        }
    }
}