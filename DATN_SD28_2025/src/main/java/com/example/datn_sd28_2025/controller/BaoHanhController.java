package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.LichSuXuLyBaoHanhDTO;
import com.example.datn_sd28_2025.dto.PhieuBaoHanhDTO;
import com.example.datn_sd28_2025.service.BaoHanhService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bao-hanh")
@CrossOrigin(origins = "*")
public class BaoHanhController {

    @Autowired
    private BaoHanhService baoHanhService;

    @GetMapping
    public ResponseEntity<List<PhieuBaoHanhDTO>> getAll() {
        List<PhieuBaoHanhDTO> phieuBaoHanhList = baoHanhService.getAll();
        return ResponseEntity.ok(phieuBaoHanhList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhieuBaoHanhDTO> getById(@PathVariable Integer id) {
        Optional<PhieuBaoHanhDTO> phieuBaoHanh = baoHanhService.getById(id);
        return phieuBaoHanh.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PhieuBaoHanhDTO phieuBaoHanhDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            PhieuBaoHanhDTO saved = baoHanhService.create(phieuBaoHanhDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo phiếu bảo hành: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody PhieuBaoHanhDTO phieuBaoHanhDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            PhieuBaoHanhDTO updated = baoHanhService.update(id, phieuBaoHanhDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi khi cập nhật phiếu bảo hành: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật phiếu bảo hành: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<PhieuBaoHanhDTO>> search(
            @RequestParam(required = false) String maPhieu,
            @RequestParam(required = false) String tenKhachHang,
            @RequestParam(required = false) String soDienThoai,
            @RequestParam(required = false) String imeiSerial,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) LocalDate ngayNhanTu,
            @RequestParam(required = false) LocalDate ngayNhanDen
    ) {
        List<PhieuBaoHanhDTO> results = baoHanhService.search(
                maPhieu, tenKhachHang, soDienThoai, imeiSerial, trangThai, ngayNhanTu, ngayNhanDen
        );
        return ResponseEntity.ok(results);
    }

    // Quy trình bảo hành
    @PostMapping("/tiep-nhan")
    public ResponseEntity<?> tiepNhanYeuCau(@Valid @RequestBody PhieuBaoHanhDTO phieuBaoHanhDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            PhieuBaoHanhDTO saved = baoHanhService.tiepNhanYeuCau(phieuBaoHanhDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tiếp nhận yêu cầu: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/kiem-tra-dieu-kien")
    public ResponseEntity<?> kiemTraDieuKienBaoHanh(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            Boolean duDieuKien = (Boolean) request.get("duDieuKien");
            String lyDo = (String) request.get("lyDo");
            PhieuBaoHanhDTO updated = baoHanhService.kiemTraDieuKienBaoHanh(id, duDieuKien, lyDo);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/sua-noi-bo")
    public ResponseEntity<?> suaNoiBo(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            String noiDungSuaChua = (String) request.get("noiDungSuaChua");
            String ghiChu = (String) request.get("ghiChu");
            Integer nhanVienKyThuatId = request.get("nhanVienKyThuatId") != null ? 
                    ((Number) request.get("nhanVienKyThuatId")).intValue() : null;
            PhieuBaoHanhDTO updated = baoHanhService.suaNoiBo(id, noiDungSuaChua, ghiChu, nhanVienKyThuatId);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/gui-ttbh")
    public ResponseEntity<?> guiTTBH(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            String ttbhHang = (String) request.get("ttbhHang");
            String maBaoHanhHang = (String) request.get("maBaoHanhHang");
            PhieuBaoHanhDTO updated = baoHanhService.guiTTBH(id, ttbhHang, maBaoHanhHang);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/nhan-tu-ttbh")
    public ResponseEntity<?> nhanTuTTBH(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            String noiDungSuaChua = (String) request.get("noiDungSuaChua");
            String ghiChu = (String) request.get("ghiChu");
            PhieuBaoHanhDTO updated = baoHanhService.nhanTuTTBH(id, noiDungSuaChua, ghiChu);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/kiem-tra-qc")
    public ResponseEntity<?> kiemTraQC(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            Boolean qcPass = (Boolean) request.get("qcPass");
            String ghiChu = (String) request.get("ghiChu");
            PhieuBaoHanhDTO updated = baoHanhService.kiemTraQC(id, qcPass, ghiChu);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/tra-may")
    public ResponseEntity<?> traMay(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            String ghiChu = (String) request.get("ghiChu");
            PhieuBaoHanhDTO updated = baoHanhService.traMay(id, ghiChu);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/update-status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> request) {
        try {
            Integer trangThai = request.get("trangThai") != null ? 
                    ((Number) request.get("trangThai")).intValue() : null;
            String hanhDong = (String) request.get("hanhDong");
            String noiDungXuLy = (String) request.get("noiDungXuLy");
            PhieuBaoHanhDTO updated = baoHanhService.updateStatus(id, trangThai, hanhDong, noiDungXuLy);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    // Lịch sử xử lý
    @GetMapping("/{id}/lich-su")
    public ResponseEntity<List<LichSuXuLyBaoHanhDTO>> getLichSuXuLy(@PathVariable Integer id) {
        List<LichSuXuLyBaoHanhDTO> lichSu = baoHanhService.getLichSuXuLy(id);
        return ResponseEntity.ok(lichSu);
    }

    @PostMapping("/{id}/lich-su")
    public ResponseEntity<?> addLichSuXuLy(
            @PathVariable Integer id,
            @Valid @RequestBody LichSuXuLyBaoHanhDTO lichSuDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            LichSuXuLyBaoHanhDTO saved = baoHanhService.addLichSuXuLy(id, lichSuDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi: " + e.getMessage());
        }
    }

    // Statistics
    @GetMapping("/statistics/by-trang-thai")
    public ResponseEntity<Map<String, Long>> getStatisticsByTrangThai(
            @RequestParam(required = false) Integer trangThai) {
        Long count = baoHanhService.countByTrangThai(trangThai);
        Map<String, Long> stats = new HashMap<>();
        stats.put("count", count);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/by-trang-thai/{trangThai}")
    public ResponseEntity<List<PhieuBaoHanhDTO>> getByTrangThai(@PathVariable Integer trangThai) {
        List<PhieuBaoHanhDTO> results = baoHanhService.findByTrangThai(trangThai);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/by-khach-hang/{khachHangId}")
    public ResponseEntity<List<PhieuBaoHanhDTO>> getByKhachHangId(@PathVariable Integer khachHangId) {
        List<PhieuBaoHanhDTO> results = baoHanhService.findByKhachHangId(khachHangId);
        return ResponseEntity.ok(results);
    }
}

