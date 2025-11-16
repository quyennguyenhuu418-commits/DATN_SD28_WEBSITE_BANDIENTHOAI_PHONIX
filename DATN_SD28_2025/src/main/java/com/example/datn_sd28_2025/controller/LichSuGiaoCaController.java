package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.LichSuGiaoCaDTO;
import com.example.datn_sd28_2025.service.LichSuGiaoCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/lich-su-giao-ca")
@CrossOrigin(origins = "*")
public class LichSuGiaoCaController {

    @Autowired
    private LichSuGiaoCaService lichSuGiaoCaService;

    @GetMapping
    public ResponseEntity<List<LichSuGiaoCaDTO>> getAll() {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getAll();
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<LichSuGiaoCaDTO> getById(@PathVariable Integer id) {
        try {
            LichSuGiaoCaDTO lichSu = lichSuGiaoCaService.getById(id);
            return ResponseEntity.ok(lichSu);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<LichSuGiaoCaDTO> create(@Valid @RequestBody LichSuGiaoCaDTO lichSuGiaoCaDTO) {
        try {
            LichSuGiaoCaDTO createdLichSu = lichSuGiaoCaService.save(lichSuGiaoCaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLichSu);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<LichSuGiaoCaDTO> update(@PathVariable Integer id, @Valid @RequestBody LichSuGiaoCaDTO lichSuGiaoCaDTO) {
        try {
            LichSuGiaoCaDTO updatedLichSu = lichSuGiaoCaService.update(id, lichSuGiaoCaDTO);
            return ResponseEntity.ok(updatedLichSu);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            lichSuGiaoCaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Query endpoints
    @GetMapping("/giao-ca/{giaoCaId}")
    public ResponseEntity<List<LichSuGiaoCaDTO>> getByGiaoCaId(@PathVariable Integer giaoCaId) {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getByGiaoCaId(giaoCaId);
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nguoi-thuc-hien/{nguoiThucHienId}")
    public ResponseEntity<List<LichSuGiaoCaDTO>> getByNguoiThucHienId(@PathVariable Integer nguoiThucHienId) {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getByNguoiThucHienId(nguoiThucHienId);
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/hanh-dong/{hanhDong}")
    public ResponseEntity<List<LichSuGiaoCaDTO>> getByHanhDong(@PathVariable String hanhDong) {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getByHanhDong(hanhDong);
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/giao-ca/{giaoCaId}/ordered")
    public ResponseEntity<List<LichSuGiaoCaDTO>> getByGiaoCaIdOrderByThoiGianDesc(@PathVariable Integer giaoCaId) {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getByGiaoCaIdOrderByThoiGianDesc(giaoCaId);
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nguoi-thuc-hien/{nguoiThucHienId}/ordered")
    public ResponseEntity<List<LichSuGiaoCaDTO>> getByNguoiThucHienIdOrderByThoiGianDesc(@PathVariable Integer nguoiThucHienId) {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getByNguoiThucHienIdOrderByThoiGianDesc(nguoiThucHienId);
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<LichSuGiaoCaDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getByDateRange(startDate, endDate);
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/giao-ca/{giaoCaId}/hanh-dong/{hanhDong}")
    public ResponseEntity<List<LichSuGiaoCaDTO>> getByGiaoCaIdAndHanhDong(
            @PathVariable Integer giaoCaId,
            @PathVariable String hanhDong) {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getByGiaoCaIdAndHanhDong(giaoCaId, hanhDong);
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nguoi-thuc-hien/{nguoiThucHienId}/date-range")
    public ResponseEntity<List<LichSuGiaoCaDTO>> getByNguoiThucHienIdAndDateRange(
            @PathVariable Integer nguoiThucHienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getByNguoiThucHienIdAndDateRange(nguoiThucHienId, startDate, endDate);
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}")
    public ResponseEntity<List<LichSuGiaoCaDTO>> getByNhanVienId(@PathVariable Integer nhanVienId) {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getByNhanVienId(nhanVienId);
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ca/{caId}")
    public ResponseEntity<List<LichSuGiaoCaDTO>> getByCaId(@PathVariable Integer caId) {
        try {
            List<LichSuGiaoCaDTO> lichSuList = lichSuGiaoCaService.getByCaId(caId);
            return ResponseEntity.ok(lichSuList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Business endpoint
    @PostMapping("/log-action")
    public ResponseEntity<LichSuGiaoCaDTO> logAction(
            @RequestParam Integer giaoCaId,
            @RequestParam String hanhDong,
            @RequestParam Integer nguoiThucHienId,
            @RequestParam(required = false) String noiDungThayDoi) {
        try {
            LichSuGiaoCaDTO lichSu = lichSuGiaoCaService.logAction(giaoCaId, hanhDong, nguoiThucHienId, noiDungThayDoi);
            return ResponseEntity.status(HttpStatus.CREATED).body(lichSu);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}








































