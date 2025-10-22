package com.example.datn_sd28_2025.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
public class UploadController {

    private static final String UPLOAD_DIR = "uploads/avatar/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Kích thước file không được vượt quá 5MB");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chỉ chấp nhận file ảnh");
                return ResponseEntity.badRequest().body(error);
            }

            // Create upload directory if not exists
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            String filename = UUID.randomUUID().toString() + extension;
            String filePath = UPLOAD_DIR + filename;

            // Save file
            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            // Return response
            Map<String, String> response = new HashMap<>();
            response.put("url", "/uploads/avatar/" + filename);
            response.put("filename", filename);
            response.put("message", "Upload thành công");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi lưu file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi upload: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/avatar/{filename}")
    public ResponseEntity<?> deleteAvatar(@PathVariable String filename) {
        try {
            String filePath = UPLOAD_DIR + filename;
            File file = new File(filePath);
            
            if (file.exists()) {
                if (file.delete()) {
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Xóa file thành công");
                    return ResponseEntity.ok(response);
                } else {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Không thể xóa file");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                }
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File không tồn tại");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi xóa file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}





