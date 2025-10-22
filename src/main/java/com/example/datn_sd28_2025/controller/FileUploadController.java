package com.example.datn_sd28_2025.controller;

// import com.example.datn_sd28_2025.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    // @Autowired
    // private CloudinaryService cloudinaryService;

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        // Temporarily return a simple response until Cloudinary is properly configured
        Map<String, Object> result = new HashMap<>();
        result.put("message", "File upload temporarily disabled - Cloudinary not configured");
        result.put("success", false);
        result.put("error", "Cloudinary service not available");
        return ResponseEntity.badRequest().body(result);
    }

    @DeleteMapping("/image/{publicId}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable String publicId) {
        // Temporarily return a simple response until Cloudinary is properly configured
        Map<String, Object> result = new HashMap<>();
        result.put("message", "File delete temporarily disabled - Cloudinary not configured");
        result.put("success", false);
        result.put("error", "Cloudinary service not available");
        return ResponseEntity.badRequest().body(result);
    }
}
