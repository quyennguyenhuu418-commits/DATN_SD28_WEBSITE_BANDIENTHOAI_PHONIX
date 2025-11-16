package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.CauHinhGiaoCaDTO;
import com.example.datn_sd28_2025.service.CauHinhGiaoCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cau-hinh-giao-ca")
@CrossOrigin(origins = "*")
public class CauHinhGiaoCaController {

    @Autowired
    private CauHinhGiaoCaService cauHinhGiaoCaService;

    @GetMapping
    public ResponseEntity<List<CauHinhGiaoCaDTO>> getAll() {
        try {
            List<CauHinhGiaoCaDTO> cauHinhList = cauHinhGiaoCaService.getAll();
            return ResponseEntity.ok(cauHinhList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CauHinhGiaoCaDTO> getById(@PathVariable Integer id) {
        try {
            Optional<CauHinhGiaoCaDTO> cauHinh = cauHinhGiaoCaService.getById(id);
            return cauHinh.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<CauHinhGiaoCaDTO> create(@Valid @RequestBody CauHinhGiaoCaDTO cauHinhGiaoCaDTO) {
        try {
            CauHinhGiaoCaDTO createdCauHinh = cauHinhGiaoCaService.save(cauHinhGiaoCaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCauHinh);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CauHinhGiaoCaDTO> update(@PathVariable Integer id, @Valid @RequestBody CauHinhGiaoCaDTO cauHinhGiaoCaDTO) {
        try {
            CauHinhGiaoCaDTO updatedCauHinh = cauHinhGiaoCaService.update(id, cauHinhGiaoCaDTO);
            return ResponseEntity.ok(updatedCauHinh);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            cauHinhGiaoCaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Query endpoints
    @GetMapping("/ten-cau-hinh/{tenCauHinh}")
    public ResponseEntity<CauHinhGiaoCaDTO> getByTenCauHinh(@PathVariable String tenCauHinh) {
        try {
            Optional<CauHinhGiaoCaDTO> cauHinh = cauHinhGiaoCaService.getByTenCauHinh(tenCauHinh);
            return cauHinh.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<CauHinhGiaoCaDTO>> getByTrangThai(@PathVariable Integer trangThai) {
        try {
            List<CauHinhGiaoCaDTO> cauHinhList = cauHinhGiaoCaService.getByTrangThai(trangThai);
            return ResponseEntity.ok(cauHinhList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/loai-cau-hinh/{loaiCauHinh}")
    public ResponseEntity<List<CauHinhGiaoCaDTO>> getByLoaiCauHinh(@PathVariable String loaiCauHinh) {
        try {
            List<CauHinhGiaoCaDTO> cauHinhList = cauHinhGiaoCaService.getByLoaiCauHinh(loaiCauHinh);
            return ResponseEntity.ok(cauHinhList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<CauHinhGiaoCaDTO>> getActiveConfigurations() {
        try {
            List<CauHinhGiaoCaDTO> cauHinhList = cauHinhGiaoCaService.getActiveConfigurations();
            return ResponseEntity.ok(cauHinhList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<CauHinhGiaoCaDTO>> searchByTenCauHinh(@RequestParam String tenCauHinh) {
        try {
            List<CauHinhGiaoCaDTO> cauHinhList = cauHinhGiaoCaService.searchByTenCauHinh(tenCauHinh);
            return ResponseEntity.ok(cauHinhList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/loai-cau-hinh/{loaiCauHinh}/active")
    public ResponseEntity<List<CauHinhGiaoCaDTO>> getActiveByLoaiCauHinh(@PathVariable String loaiCauHinh) {
        try {
            List<CauHinhGiaoCaDTO> cauHinhList = cauHinhGiaoCaService.getActiveByLoaiCauHinh(loaiCauHinh);
            return ResponseEntity.ok(cauHinhList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Configuration value endpoints
    @GetMapping("/value/string/{tenCauHinh}")
    public ResponseEntity<String> getStringValue(
            @PathVariable String tenCauHinh,
            @RequestParam(required = false) String defaultValue) {
        try {
            String value = cauHinhGiaoCaService.getStringValue(tenCauHinh, defaultValue);
            return ResponseEntity.ok(value);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/value/int/{tenCauHinh}")
    public ResponseEntity<Integer> getIntValue(
            @PathVariable String tenCauHinh,
            @RequestParam(required = false) Integer defaultValue) {
        try {
            Integer value = cauHinhGiaoCaService.getIntValue(tenCauHinh, defaultValue);
            return ResponseEntity.ok(value);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/value/boolean/{tenCauHinh}")
    public ResponseEntity<Boolean> getBooleanValue(
            @PathVariable String tenCauHinh,
            @RequestParam(required = false) Boolean defaultValue) {
        try {
            Boolean value = cauHinhGiaoCaService.getBooleanValue(tenCauHinh, defaultValue);
            return ResponseEntity.ok(value);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/value/{tenCauHinh}")
    public ResponseEntity<Void> setValue(
            @PathVariable String tenCauHinh,
            @RequestParam String value) {
        try {
            cauHinhGiaoCaService.setValue(tenCauHinh, value);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Check existence endpoints
    @GetMapping("/exists/ten-cau-hinh/{tenCauHinh}")
    public ResponseEntity<Boolean> existsByTenCauHinh(@PathVariable String tenCauHinh) {
        try {
            boolean exists = cauHinhGiaoCaService.existsByTenCauHinh(tenCauHinh);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}








































