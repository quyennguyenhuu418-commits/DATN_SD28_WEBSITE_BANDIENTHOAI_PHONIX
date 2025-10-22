package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.AdministrativeDivisionDTO;

import java.util.List;

public interface VietnamAdministrativeService {
    List<AdministrativeDivisionDTO> getProvinces();
    List<AdministrativeDivisionDTO> getWards(String districtCode);
}

