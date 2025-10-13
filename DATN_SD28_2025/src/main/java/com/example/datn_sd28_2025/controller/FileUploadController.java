package com.example.datn_sd28_2025.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads/";

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "File không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate file size (max 10MB)
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "File quá lớn! Kích thước tối đa là 10MB");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Chỉ được upload file ảnh");
                return ResponseEntity.badRequest().body(error);
            }

            // Tạo thư mục uploads nếu chưa có
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? 
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // Lưu file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Return response
            Map<String, Object> response = new HashMap<>();
            response.put("url", "/uploads/" + filename);
            response.put("filename", filename);
            response.put("message", "Upload thành công");
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi upload file: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/image/{filename}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Xóa ảnh thành công");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "File không tồn tại");
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi xóa ảnh: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
