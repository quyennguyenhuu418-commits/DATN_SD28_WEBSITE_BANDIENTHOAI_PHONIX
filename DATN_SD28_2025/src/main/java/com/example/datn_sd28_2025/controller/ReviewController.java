package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.ReviewDTO;
import com.example.datn_sd28_2025.dto.ReviewRequestDTO;
import com.example.datn_sd28_2025.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @PostMapping
    public ResponseEntity<?> createReview(@Valid @RequestBody ReviewRequestDTO requestDTO) {
        try {
            ReviewDTO review = reviewService.createReview(requestDTO);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra khi tạo đánh giá");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @GetMapping("/san-pham/{idSanPham}")
    public ResponseEntity<List<ReviewDTO>> getReviewsBySanPham(@PathVariable Integer idSanPham) {
        try {
            List<ReviewDTO> reviews = reviewService.getReviewsBySanPhamId(idSanPham);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of()); // Return empty list on error
        }
    }
    
    @GetMapping("/san-pham/{idSanPham}/rating")
    public ResponseEntity<Map<String, Object>> getRatingInfo(@PathVariable Integer idSanPham) {
        try {
            Double averageRating = reviewService.getAverageRating(idSanPham);
            Long reviewCount = reviewService.getReviewCount(idSanPham);
            
            Map<String, Object> response = new HashMap<>();
            response.put("averageRating", averageRating != null ? averageRating : 0.0);
            response.put("reviewCount", reviewCount != null ? reviewCount : 0);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("averageRating", 0.0);
            error.put("reviewCount", 0);
            return ResponseEntity.ok(error);
        }
    }
    
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // API quản lý đánh giá cho admin
    @GetMapping("/admin/cho-duyet")
    public ResponseEntity<List<ReviewDTO>> getReviewsChoDuyet() {
        try {
            List<ReviewDTO> reviews = reviewService.getReviewsChoDuyet();
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
    
    @GetMapping("/admin/all")
    public ResponseEntity<List<ReviewDTO>> getAllReviewsForAdmin() {
        try {
            List<ReviewDTO> reviews = reviewService.getAllReviewsForAdmin();
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
    
    @PostMapping("/admin/{reviewId}/duyet")
    public ResponseEntity<?> approveReview(@PathVariable Integer reviewId, @RequestBody Map<String, String> request) {
        try {
            String ghiChu = request.getOrDefault("ghiChu", "");
            reviewService.approveReview(reviewId, ghiChu);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra khi duyệt đánh giá");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @PostMapping("/admin/{reviewId}/tu-choi")
    public ResponseEntity<?> rejectReview(@PathVariable Integer reviewId, @RequestBody Map<String, String> request) {
        try {
            String ghiChu = request.getOrDefault("ghiChu", "");
            reviewService.rejectReview(reviewId, ghiChu);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra khi từ chối đánh giá");
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    @PostMapping("/admin/{reviewId}/reset")
    public ResponseEntity<?> resetReview(@PathVariable Integer reviewId, @RequestBody Map<String, String> request) {
        try {
            String ghiChu = request.getOrDefault("ghiChu", "");
            reviewService.resetReview(reviewId, ghiChu);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Có lỗi xảy ra khi đặt lại đánh giá");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}







