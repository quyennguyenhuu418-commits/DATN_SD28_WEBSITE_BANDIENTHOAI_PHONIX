package com.example.datn_sd28_2025.controller;

<<<<<<< HEAD
// import com.example.datn_sd28_2025.service.CloudinaryService;
=======
import com.example.datn_sd28_2025.entity.DanhMuc;
import com.example.datn_sd28_2025.entity.HinhAnh;
import com.example.datn_sd28_2025.repository.DanhMucRepository;
import com.example.datn_sd28_2025.repository.HinhAnhRepository;
import com.example.datn_sd28_2025.service.CloudinaryService;
>>>>>>> origin/Huan
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

<<<<<<< HEAD
import java.util.HashMap;
=======
import java.time.LocalDateTime;
>>>>>>> origin/Huan
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

<<<<<<< HEAD
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
=======
    @Autowired
    private CloudinaryService cloudinaryService;
    
    @Autowired
    private DanhMucRepository danhMucRepository;
    
    @Autowired
    private HinhAnhRepository hinhAnhRepository;

    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, Object> error = Map.of("error", "File không được để trống");
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> result = cloudinaryService.uploadImage(file);
        
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        
        return ResponseEntity.ok(result);
    }

    @PostMapping("/image/danh-muc")
    public ResponseEntity<Map<String, Object>> uploadImageForDanhMuc(
            @RequestParam("file") MultipartFile file,
            @RequestParam("idDanhMuc") Integer idDanhMuc) {
        if (file.isEmpty()) {
            Map<String, Object> error = Map.of("error", "File không được để trống");
            return ResponseEntity.badRequest().body(error);
        }

        DanhMuc danhMuc = danhMucRepository.findById(idDanhMuc)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        Map<String, Object> uploadResult = cloudinaryService.uploadImage(file);
        
        if (uploadResult.containsKey("error")) {
            return ResponseEntity.badRequest().body(uploadResult);
        }
        
        // Lưu thông tin hình ảnh vào database
        HinhAnh hinhAnh = HinhAnh.builder()
                .danhMuc(danhMuc)
                .urlAnh((String) uploadResult.get("url"))
                .ngayTao(LocalDateTime.now())
                .trangThai(1)
                .build();
        
        hinhAnhRepository.save(hinhAnh);
        
        Map<String, Object> result = Map.of(
            "url", uploadResult.get("url"),
            "publicId", uploadResult.get("public_id"),
            "message", "Upload hình ảnh cho danh mục thành công"
        );
        
        return ResponseEntity.ok(result);
>>>>>>> origin/Huan
    }

    @DeleteMapping("/image/{publicId}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable String publicId) {
<<<<<<< HEAD
        // Temporarily return a simple response until Cloudinary is properly configured
        Map<String, Object> result = new HashMap<>();
        result.put("message", "File delete temporarily disabled - Cloudinary not configured");
        result.put("success", false);
        result.put("error", "Cloudinary service not available");
        return ResponseEntity.badRequest().body(result);
=======
        Map<String, Object> result = cloudinaryService.deleteImage(publicId);
        
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        
        return ResponseEntity.ok(result);
>>>>>>> origin/Huan
    }
}
