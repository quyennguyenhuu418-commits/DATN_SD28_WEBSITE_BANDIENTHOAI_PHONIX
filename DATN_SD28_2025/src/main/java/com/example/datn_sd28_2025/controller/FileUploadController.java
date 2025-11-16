package com.example.datn_sd28_2025.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    @Value("${cloudinary.cloud-name:}")
    private String cloudinaryCloudName;
    
    @Value("${cloudinary.api-key:}")
    private String cloudinaryApiKey;
    
    @Value("${cloudinary.api-secret:}")
    private String cloudinaryApiSecret;

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                result.put("success", false);
                result.put("error", "File is empty");
                return ResponseEntity.badRequest().body(result);
            }
            
            if (file.getSize() > MAX_FILE_SIZE) {
                result.put("success", false);
                result.put("error", "File size exceeds 10MB limit");
                return ResponseEntity.badRequest().body(result);
            }
            
            // Check if Cloudinary is configured
            if (!isCloudinaryConfigured()) {
                result.put("success", false);
                result.put("error", "Cloudinary not configured. Please set cloudinary.cloud-name, cloudinary.api-key, and cloudinary.api-secret in application.properties");
                return ResponseEntity.badRequest().body(result);
            }
            
            // Upload to Cloudinary
            return uploadToCloudinary(file);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    private boolean isCloudinaryConfigured() {
        return cloudinaryCloudName != null && !cloudinaryCloudName.isEmpty() &&
               cloudinaryApiKey != null && !cloudinaryApiKey.isEmpty() &&
               cloudinaryApiSecret != null && !cloudinaryApiSecret.isEmpty();
    }
    
    private ResponseEntity<Map<String, Object>> uploadToCloudinary(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        
        // Configure Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudinaryCloudName);
        config.put("api_key", cloudinaryApiKey);
        config.put("api_secret", cloudinaryApiSecret);
        
        Cloudinary cloudinary = new Cloudinary(config);
        
        // Upload options
        Map<String, Object> uploadOptions = ObjectUtils.asMap(
            "folder", "datn_sd28_2025/products",
            "use_filename", true,
            "unique_filename", true,
            "overwrite", false
        );
        
        // Upload file
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
        
        // Return success response
        result.put("success", true);
        result.put("url", uploadResult.get("secure_url"));
        result.put("public_id", uploadResult.get("public_id"));
        result.put("originalName", file.getOriginalFilename());
        result.put("size", file.getSize());
        result.put("storage", "cloudinary");
        
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/image/{publicId}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable String publicId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check if Cloudinary is configured
            if (!isCloudinaryConfigured()) {
                result.put("success", false);
                result.put("error", "Cloudinary not configured");
                return ResponseEntity.badRequest().body(result);
            }
            
            // Configure Cloudinary
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloudinaryCloudName);
            config.put("api_key", cloudinaryApiKey);
            config.put("api_secret", cloudinaryApiSecret);
            
            Cloudinary cloudinary = new Cloudinary(config);
            
            // Delete from Cloudinary
            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            if ("ok".equals(deleteResult.get("result"))) {
                result.put("success", true);
                result.put("message", "File deleted successfully from Cloudinary");
                result.put("storage", "cloudinary");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("error", "Failed to delete file from Cloudinary");
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Failed to delete file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
