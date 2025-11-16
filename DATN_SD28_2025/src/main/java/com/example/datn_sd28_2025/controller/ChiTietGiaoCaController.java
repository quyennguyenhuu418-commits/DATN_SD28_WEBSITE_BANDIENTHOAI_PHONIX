package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.ChiTietGiaoCaDTO;
import com.example.datn_sd28_2025.service.ChiTietGiaoCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chi-tiet-giao-ca")
@CrossOrigin(origins = "*")
public class ChiTietGiaoCaController {

    @Autowired
    private ChiTietGiaoCaService chiTietGiaoCaService;

    @GetMapping
    public ResponseEntity<List<ChiTietGiaoCaDTO>> getAll() {
        try {
            List<ChiTietGiaoCaDTO> chiTietList = chiTietGiaoCaService.getAll();
            return ResponseEntity.ok(chiTietList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChiTietGiaoCaDTO> getById(@PathVariable Integer id) {
        try {
            Optional<ChiTietGiaoCaDTO> chiTiet = chiTietGiaoCaService.getById(id);
            return chiTiet.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<ChiTietGiaoCaDTO> create(@Valid @RequestBody ChiTietGiaoCaDTO chiTietGiaoCaDTO) {
        try {
            ChiTietGiaoCaDTO createdChiTiet = chiTietGiaoCaService.save(chiTietGiaoCaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdChiTiet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChiTietGiaoCaDTO> update(@PathVariable Integer id, @Valid @RequestBody ChiTietGiaoCaDTO chiTietGiaoCaDTO) {
        try {
            ChiTietGiaoCaDTO updatedChiTiet = chiTietGiaoCaService.update(id, chiTietGiaoCaDTO);
            return ResponseEntity.ok(updatedChiTiet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            chiTietGiaoCaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Query endpoints
    @GetMapping("/giao-ca/{giaoCaId}")
    public ResponseEntity<List<ChiTietGiaoCaDTO>> getByGiaoCaId(@PathVariable Integer giaoCaId) {
        try {
            List<ChiTietGiaoCaDTO> chiTietList = chiTietGiaoCaService.getByGiaoCaId(giaoCaId);
            return ResponseEntity.ok(chiTietList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/loai-chi-tiet/{loaiChiTiet}")
    public ResponseEntity<List<ChiTietGiaoCaDTO>> getByLoaiChiTiet(@PathVariable String loaiChiTiet) {
        try {
            List<ChiTietGiaoCaDTO> chiTietList = chiTietGiaoCaService.getByLoaiChiTiet(loaiChiTiet);
            return ResponseEntity.ok(chiTietList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/giao-ca/{giaoCaId}/loai-chi-tiet/{loaiChiTiet}")
    public ResponseEntity<List<ChiTietGiaoCaDTO>> getByGiaoCaIdAndLoaiChiTiet(
            @PathVariable Integer giaoCaId,
            @PathVariable String loaiChiTiet) {
        try {
            List<ChiTietGiaoCaDTO> chiTietList = chiTietGiaoCaService.getByGiaoCaIdAndLoaiChiTiet(giaoCaId, loaiChiTiet);
            return ResponseEntity.ok(chiTietList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<ChiTietGiaoCaDTO>> getByTrangThai(@PathVariable Integer trangThai) {
        try {
            List<ChiTietGiaoCaDTO> chiTietList = chiTietGiaoCaService.getByTrangThai(trangThai);
            return ResponseEntity.ok(chiTietList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/giao-ca/{giaoCaId}/active")
    public ResponseEntity<List<ChiTietGiaoCaDTO>> getActiveByGiaoCaId(@PathVariable Integer giaoCaId) {
        try {
            List<ChiTietGiaoCaDTO> chiTietList = chiTietGiaoCaService.getActiveByGiaoCaId(giaoCaId);
            return ResponseEntity.ok(chiTietList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/giao-ca/{giaoCaId}/loai-chi-tiet/{loaiChiTiet}/active")
    public ResponseEntity<List<ChiTietGiaoCaDTO>> getActiveByGiaoCaIdAndLoaiChiTiet(
            @PathVariable Integer giaoCaId,
            @PathVariable String loaiChiTiet) {
        try {
            List<ChiTietGiaoCaDTO> chiTietList = chiTietGiaoCaService.getActiveByGiaoCaIdAndLoaiChiTiet(giaoCaId, loaiChiTiet);
            return ResponseEntity.ok(chiTietList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}/loai-chi-tiet/{loaiChiTiet}")
    public ResponseEntity<List<ChiTietGiaoCaDTO>> getByNhanVienIdAndLoaiChiTiet(
            @PathVariable Integer nhanVienId,
            @PathVariable String loaiChiTiet) {
        try {
            List<ChiTietGiaoCaDTO> chiTietList = chiTietGiaoCaService.getByNhanVienIdAndLoaiChiTiet(nhanVienId, loaiChiTiet);
            return ResponseEntity.ok(chiTietList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ca/{caId}/loai-chi-tiet/{loaiChiTiet}")
    public ResponseEntity<List<ChiTietGiaoCaDTO>> getByCaIdAndLoaiChiTiet(
            @PathVariable Integer caId,
            @PathVariable String loaiChiTiet) {
        try {
            List<ChiTietGiaoCaDTO> chiTietList = chiTietGiaoCaService.getByCaIdAndLoaiChiTiet(caId, loaiChiTiet);
            return ResponseEntity.ok(chiTietList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Statistics endpoints
    @GetMapping("/giao-ca/{giaoCaId}/loai-chi-tiet/{loaiChiTiet}/total-value")
    public ResponseEntity<BigDecimal> getTotalValueByGiaoCaIdAndLoaiChiTiet(
            @PathVariable Integer giaoCaId,
            @PathVariable String loaiChiTiet) {
        try {
            BigDecimal totalValue = chiTietGiaoCaService.getTotalValueByGiaoCaIdAndLoaiChiTiet(giaoCaId, loaiChiTiet);
            return ResponseEntity.ok(totalValue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/giao-ca/{giaoCaId}/loai-chi-tiet/{loaiChiTiet}/total-quantity")
    public ResponseEntity<Long> getTotalQuantityByGiaoCaIdAndLoaiChiTiet(
            @PathVariable Integer giaoCaId,
            @PathVariable String loaiChiTiet) {
        try {
            Long totalQuantity = chiTietGiaoCaService.getTotalQuantityByGiaoCaIdAndLoaiChiTiet(giaoCaId, loaiChiTiet);
            return ResponseEntity.ok(totalQuantity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Business endpoints
    @PostMapping("/add-doanh-thu")
    public ResponseEntity<ChiTietGiaoCaDTO> addDoanhThu(
            @RequestParam Integer giaoCaId,
            @RequestParam String tenChiTiet,
            @RequestParam BigDecimal giaTri,
            @RequestParam(required = false) String moTa) {
        try {
            ChiTietGiaoCaDTO chiTiet = chiTietGiaoCaService.addDoanhThu(giaoCaId, tenChiTiet, giaTri, moTa);
            return ResponseEntity.status(HttpStatus.CREATED).body(chiTiet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add-don-hang")
    public ResponseEntity<ChiTietGiaoCaDTO> addDonHang(
            @RequestParam Integer giaoCaId,
            @RequestParam String tenChiTiet,
            @RequestParam Integer soLuong,
            @RequestParam(required = false) String moTa) {
        try {
            ChiTietGiaoCaDTO chiTiet = chiTietGiaoCaService.addDonHang(giaoCaId, tenChiTiet, soLuong, moTa);
            return ResponseEntity.status(HttpStatus.CREATED).body(chiTiet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add-tien-mat")
    public ResponseEntity<ChiTietGiaoCaDTO> addTienMat(
            @RequestParam Integer giaoCaId,
            @RequestParam String tenChiTiet,
            @RequestParam BigDecimal giaTri,
            @RequestParam(required = false) String moTa) {
        try {
            ChiTietGiaoCaDTO chiTiet = chiTietGiaoCaService.addTienMat(giaoCaId, tenChiTiet, giaTri, moTa);
            return ResponseEntity.status(HttpStatus.CREATED).body(chiTiet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add-su-co")
    public ResponseEntity<ChiTietGiaoCaDTO> addSuCo(
            @RequestParam Integer giaoCaId,
            @RequestParam String tenChiTiet,
            @RequestParam(required = false) String moTa) {
        try {
            ChiTietGiaoCaDTO chiTiet = chiTietGiaoCaService.addSuCo(giaoCaId, tenChiTiet, moTa);
            return ResponseEntity.status(HttpStatus.CREATED).body(chiTiet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/add-cong-viec")
    public ResponseEntity<ChiTietGiaoCaDTO> addCongViec(
            @RequestParam Integer giaoCaId,
            @RequestParam String tenChiTiet,
            @RequestParam(required = false) String moTa) {
        try {
            ChiTietGiaoCaDTO chiTiet = chiTietGiaoCaService.addCongViec(giaoCaId, tenChiTiet, moTa);
            return ResponseEntity.status(HttpStatus.CREATED).body(chiTiet);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}








































