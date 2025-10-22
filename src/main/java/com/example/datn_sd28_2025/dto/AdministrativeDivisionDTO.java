package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdministrativeDivisionDTO {
    private String code;
    private String name;
    private String type;
    private String parentCode;
    private String fullName;
    private String level; // PROVINCE, DISTRICT, WARD
}

