package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "thong_bao")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tieu_de", nullable = false, length = 255)
    private String tieuDe;
    
    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;
    
    @Column(name = "loai_thong_bao", nullable = false, length = 50)
    private String loaiThongBao; // ORDER_NEW, PRODUCT_NEW, CUSTOMER_NEW, etc.
    
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai = 0; // 0: chưa đọc, 1: đã đọc
    
    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;
    
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
    
    @Column(name = "id_tham_chieu")
    private Long idThamChieu; // ID của đơn hàng, sản phẩm, etc.
    
    @Column(name = "duong_dan")
    private String duongDan; // Link đến trang chi tiết
    
    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}
























