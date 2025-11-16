package com.example.datn_sd28_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham")
    @JsonIgnore
    private SanPham sanPham;

    @Column(name = "id_nguoi_dung")
    private Integer idNguoiDung;

    @Column(name = "ten_nguoi_dung", length = 100)
    private String tenNguoiDung;

    @Column(name = "rating")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "trang_thai")
    private Integer trangThai; // 0: CHO_DUYET, 1: DA_DUYET, 2: TU_CHOI

    @Column(name = "ngay_duyet")
    private LocalDateTime ngayDuyet;

    @Column(name = "ghi_chu_duyet", length = 500)
    private String ghiChuDuyet;

    // Transient field for JSON mapping
    @Transient
    @JsonProperty("idSanPham")
    private Integer idSanPham;
}







