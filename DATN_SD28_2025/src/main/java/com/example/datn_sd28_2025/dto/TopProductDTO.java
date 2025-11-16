package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {
    private Long id;
    private String name;
    private String category;
    private String brand;
    private Long quantitySold;
    private BigDecimal revenue;
    private BigDecimal profit;
    private Integer rank;
    private String imageUrl;
}


