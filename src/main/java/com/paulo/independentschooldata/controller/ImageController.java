package com.paulo.independentschooldata.controller;

import com.paulo.independentschooldata.service.ImageStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageStorageService imageStorageService;

    public ImageController(ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
    }

    @PostMapping("/uploadImage")
    public ResponseEntity<String> handleImageUpload(@RequestParam("file") MultipartFile file, Principal principal) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a file to upload.");
        }

        try {
            // 1. Upload to S3/R2 and get the public URL
            String imageUrl = imageStorageService.uploadImage(file, principal.getName());

            // 3. Return the URL to the client
            return ResponseEntity.ok(imageUrl);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
        }
    }

//    @PostMapping("/uploadImage/{schoolUuid}")
//    public ResponseEntity<String> handleImageUpload(@RequestParam("file") MultipartFile file,
//                                                    @PathVariable("schoolUuid") UUID schoolUuid,
//                                                    @RequestParam("featured") Optional<Boolean> featured) {
//        if (file.isEmpty()) {
//            return ResponseEntity.badRequest().body("Please select a file to upload.");
//        }
//
//        try {
//            // 1. Upload to S3/R2 and get the public URL
//            String imageUrl = imageStorageService.uploadImage(file, schoolUuid, featured);
//
//            // 3. Return the URL to the client
//            return ResponseEntity.ok(imageUrl);
//
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
//        }
//    }

    @GetMapping()
    public void handleImageUpload() {

        // 1. Upload to S3/R2 and get the public URL
        String test = imageStorageService.testConnection();
        System.out.println(test);
        // 3. Return the URL to the client

    }
}