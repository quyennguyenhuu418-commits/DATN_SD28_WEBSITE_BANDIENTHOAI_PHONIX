package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.AdministrativeDivisionDTO;
import com.example.datn_sd28_2025.service.VietnamAdministrativeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vietnam-administrative")
@CrossOrigin(origins = "*")
public class VietnamAdministrativeController {

    private final VietnamAdministrativeService administrativeService;

    @Autowired
    public VietnamAdministrativeController(VietnamAdministrativeService administrativeService) {
        this.administrativeService = administrativeService;
    }

    @GetMapping("/provinces")
    public ResponseEntity<List<AdministrativeDivisionDTO>> getProvinces() {
        try {
            List<AdministrativeDivisionDTO> provinces = administrativeService.getProvinces();
            return ResponseEntity.ok(provinces);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/wards/{provinceCode}")
    public ResponseEntity<List<AdministrativeDivisionDTO>> getWards(@PathVariable String provinceCode) {
        try {
            List<AdministrativeDivisionDTO> wards = administrativeService.getWards(provinceCode);
            return ResponseEntity.ok(wards);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

