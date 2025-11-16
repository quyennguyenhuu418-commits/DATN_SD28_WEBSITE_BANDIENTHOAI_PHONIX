package com.example.datn_sd28_2025.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

 public interface CloudinaryService {
     Map<String, Object> uploadImage(MultipartFile file);
     Map<String, Object> deleteImage(String publicId);
     boolean isValidImageFile(MultipartFile file);
     boolean isValidFileSize(MultipartFile file, long maxSizeInBytes);
 }
