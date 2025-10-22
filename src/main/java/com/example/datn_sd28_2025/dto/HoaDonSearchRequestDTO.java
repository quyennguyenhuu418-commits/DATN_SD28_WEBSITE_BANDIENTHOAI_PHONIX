package com.example.datn_sd28_2025.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonSearchRequestDTO {
    private String keyword;
    private Integer trangThai;
    private String loaiHoaDon;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime tuNgay;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime denNgay;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}

