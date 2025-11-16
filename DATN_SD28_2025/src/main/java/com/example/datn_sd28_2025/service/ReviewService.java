package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.ReviewDTO;
import com.example.datn_sd28_2025.dto.ReviewRequestDTO;
import com.example.datn_sd28_2025.entity.Review;
import com.example.datn_sd28_2025.entity.SanPham;
import com.example.datn_sd28_2025.repository.ReviewRepository;
import com.example.datn_sd28_2025.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    public ReviewDTO createReview(ReviewRequestDTO requestDTO) {
        SanPham sanPham = sanPhamRepository.findById(requestDTO.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        
        // Validate guest information if idNguoiDung is null
        if (requestDTO.getIdNguoiDung() == null) {
            if (requestDTO.getTenNguoiDung() == null || requestDTO.getTenNguoiDung().trim().isEmpty()) {
                throw new RuntimeException("Tên người dùng không được để trống cho khách");
            }
        }
        
        Review review = Review.builder()
                .sanPham(sanPham)
                .idNguoiDung(requestDTO.getIdNguoiDung())
                .tenNguoiDung(requestDTO.getTenNguoiDung())
                .rating(requestDTO.getRating())
                .comment(requestDTO.getComment())
                .ngayTao(LocalDateTime.now())
                .trangThai(0) // 0: CHO_DUYET (Mặc định chờ duyệt)
                .build();
        
        Review savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview, requestDTO);
    }
    
    public List<ReviewDTO> getReviewsBySanPhamId(Integer idSanPham) {
        List<Review> reviews = reviewRepository.findBySanPhamIdAndTrangThaiOrderByNgayTaoDesc(idSanPham, 1); // 1: DA_DUYET
        return reviews.stream()
                .map(review -> convertToDTO(review, null))
                .collect(Collectors.toList());
    }
    
    public Double getAverageRating(Integer idSanPham) {
        return reviewRepository.getAverageRatingBySanPhamIdAndTrangThai(idSanPham, 1); // 1: DA_DUYET
    }
    
    public Long getReviewCount(Integer idSanPham) {
        return reviewRepository.countBySanPhamIdAndTrangThai(idSanPham, 1); // 1: DA_DUYET
    }
    
    public void deleteReview(Integer reviewId) {
        reviewRepository.deleteById(reviewId);
    }
    
    // Lấy tất cả đánh giá chờ duyệt
    public List<ReviewDTO> getReviewsChoDuyet() {
        List<Review> reviews = reviewRepository.findByTrangThaiOrderByNgayTaoDesc(0); // 0: CHO_DUYET
        return reviews.stream()
                .map(review -> convertToDTO(review, null))
                .collect(Collectors.toList());
    }
    
    // Duyệt đánh giá
    public void approveReview(Integer reviewId, String ghiChu) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        
        review.setTrangThai(1); // 1: DA_DUYET
        review.setNgayDuyet(LocalDateTime.now());
        review.setGhiChuDuyet(ghiChu);
        
        reviewRepository.save(review);
    }
    
    // Từ chối đánh giá
    public void rejectReview(Integer reviewId, String ghiChu) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        
        review.setTrangThai(2); // 2: TU_CHOI
        review.setNgayDuyet(LocalDateTime.now());
        review.setGhiChuDuyet(ghiChu);
        
        reviewRepository.save(review);
    }
    
    // Đặt lại chờ duyệt
    public void resetReview(Integer reviewId, String ghiChu) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        
        review.setTrangThai(0); // 0: CHO_DUYET
        review.setNgayDuyet(null);
        review.setGhiChuDuyet(ghiChu);
        
        reviewRepository.save(review);
    }
    
    // Lấy tất cả đánh giá cho admin (bao gồm tất cả trạng thái)
    public List<ReviewDTO> getAllReviewsForAdmin() {
        List<Review> reviews = reviewRepository.findAllByOrderByNgayTaoDesc();
        
        // Update old reviews that don't have trangThai
        for (Review review : reviews) {
            if (review.getTrangThai() == null) {
                review.setTrangThai(0); // 0: CHO_DUYET
                reviewRepository.save(review);
                System.out.println("Updated review " + review.getReviewId() + " with default status CHO_DUYET");
            }
        }
        
        return reviews.stream()
                .map(review -> convertToDTO(review, null))
                .collect(Collectors.toList());
    }
    
    private ReviewDTO convertToDTO(Review review, ReviewRequestDTO requestDTO) {
        String tenNguoiDung;
        
        if (review.getIdNguoiDung() == null) {
            // Guest user - use name from database or default
            tenNguoiDung = (review.getTenNguoiDung() != null && !review.getTenNguoiDung().trim().isEmpty()) 
                ? review.getTenNguoiDung() 
                : "Khách";
        } else {
            // Registered user - use placeholder for now
            tenNguoiDung = "Người dùng " + review.getIdNguoiDung();
        }
        
        return ReviewDTO.builder()
                .reviewId(review.getReviewId())
                .idSanPham(review.getSanPham().getId())
                .idNguoiDung(review.getIdNguoiDung())
                .tenNguoiDung(tenNguoiDung)
                .rating(review.getRating())
                .comment(review.getComment())
                .ngayTao(review.getNgayTao())
                .trangThai(review.getTrangThai() != null ? review.getTrangThai() : 0) // 0: CHO_DUYET
                .ngayDuyet(review.getNgayDuyet())
                .ghiChuDuyet(review.getGhiChuDuyet())
                .build();
    }
}







