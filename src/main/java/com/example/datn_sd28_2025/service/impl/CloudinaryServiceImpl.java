package com.example.datn_sd28_2025.service.impl;

import com.cloudinary.Cloudinary;
import com.example.datn_sd28_2025.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    @Override
    public Map<String, Object> uploadImage(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Validate file
            if (!isValidImageFile(file)) {
                result.put("error", "Chỉ được upload file ảnh (JPEG, PNG, GIF, WebP)");
                return result;
            }

            if (!isValidFileSize(file, MAX_FILE_SIZE)) {
                result.put("error", "File quá lớn! Kích thước tối đa là 10MB");
                return result;
            }

            // Generate unique public ID
            String publicId = "phonex_" + UUID.randomUUID().toString();
            
            // Upload to Cloudinary
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("public_id", publicId);
            uploadParams.put("resource_type", "image");
            uploadParams.put("folder", "phonex_images"); // Organize images in folder
            
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            
            // Prepare response
            result.put("url", uploadResult.get("secure_url"));
            result.put("public_id", uploadResult.get("public_id"));
            result.put("filename", file.getOriginalFilename());
            result.put("message", "Upload thành công");
            result.put("size", file.getSize());
            
        } catch (IOException e) {
            result.put("error", "Lỗi upload file: " + e.getMessage());
        } catch (Exception e) {
            result.put("error", "Lỗi không xác định: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> deleteImage(String publicId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Map<String, Object> deleteParams = new HashMap<>();
            deleteParams.put("resource_type", "image");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, deleteParams);
            
            if ("ok".equals(deleteResult.get("result"))) {
                result.put("message", "Xóa ảnh thành công");
                result.put("deleted", true);
            } else {
                result.put("error", "Không thể xóa ảnh");
                result.put("deleted", false);
            }
            
        } catch (Exception e) {
            result.put("error", "Lỗi xóa ảnh: " + e.getMessage());
            result.put("deleted", false);
        }
        
        return result;
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean isValidFileSize(MultipartFile file, long maxSizeInBytes) {
        return file != null && file.getSize() <= maxSizeInBytes;
    }
}
