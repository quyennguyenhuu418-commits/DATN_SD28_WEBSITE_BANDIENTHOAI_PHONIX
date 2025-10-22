package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_giao_ca")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuGiaoCa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Giao ca không được để trống")
    @Column(name = "giao_ca_id", nullable = false)
    private Integer giaoCaId;

    @NotBlank(message = "Hành động không được để trống")
    @Size(max = 50, message = "Hành động không được quá 50 ký tự")
    @Column(name = "hanh_dong", length = 50)
    private String hanhDong; // TAO_MOI, CAP_NHAT, XAC_NHAN, HUY

    @NotNull(message = "Người thực hiện không được để trống")
    @Column(name = "nguoi_thuc_hien_id", nullable = false)
    private Integer nguoiThucHienId;

    @Size(max = 1000, message = "Nội dung thay đổi không được quá 1000 ký tự")
    @Column(name = "noi_dung_thay_doi", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String noiDungThayDoi;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giao_ca_id", insertable = false, updatable = false)
    private GiaoCa giaoCa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_thuc_hien_id", insertable = false, updatable = false)
    private NhanVien nguoiThucHien;

    @PrePersist
    protected void onCreate() {
        if (thoiGian == null) {
            thoiGian = LocalDateTime.now();
        }
    }
}

