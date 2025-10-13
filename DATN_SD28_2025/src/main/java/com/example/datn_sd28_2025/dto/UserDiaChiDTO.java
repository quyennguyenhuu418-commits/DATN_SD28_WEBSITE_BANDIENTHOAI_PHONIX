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
public class UserDiaChiDTO {
    private Integer id;
    private Integer idDiaChi;
    // Related entities
    private KhachHangDTO khachHang;

    // Transient field for JSON mapping
    private Integer idUser;
    private String loaiDiaChi;
    private Boolean macDinh;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;
}
