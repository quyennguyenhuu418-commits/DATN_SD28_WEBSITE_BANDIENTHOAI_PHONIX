package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
<<<<<<< HEAD
=======
import java.util.List;
>>>>>>> origin/Huan

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhMucDTO {
    private Integer id;
    private String maDanhMuc;
    private String tenDanhMuc;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;
<<<<<<< HEAD
=======
    private List<HinhAnhDTO> hinhAnhs;
>>>>>>> origin/Huan
}
