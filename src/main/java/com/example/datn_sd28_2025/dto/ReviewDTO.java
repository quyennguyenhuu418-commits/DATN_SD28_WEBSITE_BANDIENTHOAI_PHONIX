package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {
    private Integer reviewId;
    private Integer idSanPham;
    private Integer idNguoiDung;
    private Integer rating;
    private String comment;
    private LocalDateTime ngayTao;
    private String tenNguoiDung; // Tên người dùng (nếu có)
    private Integer trangThai; // Trạng thái đánh giá (0: CHO_DUYET, 1: DA_DUYET, 2: TU_CHOI)
    private LocalDateTime ngayDuyet; // Ngày duyệt
    private String ghiChuDuyet; // Ghi chú duyệt
}
