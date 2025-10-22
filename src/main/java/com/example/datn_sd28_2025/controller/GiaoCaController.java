package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.GiaoCaDTO;
import com.example.datn_sd28_2025.service.GiaoCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/giao-ca")
@CrossOrigin(origins = "*")
public class GiaoCaController {

    @Autowired
    private GiaoCaService giaoCaService;

    @GetMapping
    public ResponseEntity<List<GiaoCaDTO>> getAll() {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getAll();
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<GiaoCaDTO> getById(@PathVariable Integer id) {
        try {
            Optional<GiaoCaDTO> giaoCa = giaoCaService.getById(id);
            return giaoCa.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<GiaoCaDTO> create(@Valid @RequestBody GiaoCaDTO giaoCaDTO) {
        try {
            System.out.println("Creating giao ca with data: " + giaoCaDTO);
            GiaoCaDTO createdGiaoCa = giaoCaService.save(giaoCaDTO);
            System.out.println("Giao ca created successfully: " + createdGiaoCa.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGiaoCa);
        } catch (RuntimeException e) {
            System.out.println("Runtime error creating giao ca: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.out.println("Unexpected error creating giao ca: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GiaoCaDTO> update(@PathVariable Integer id, @Valid @RequestBody GiaoCaDTO giaoCaDTO) {
        try {
            GiaoCaDTO updatedGiaoCa = giaoCaService.update(id, giaoCaDTO);
            return ResponseEntity.ok(updatedGiaoCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            giaoCaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Business endpoints
    @PostMapping("/create")
    public ResponseEntity<GiaoCaDTO> createGiaoCa(
            @RequestParam Integer phanCaId,
            @RequestParam Integer nhanVienGiaoId,
            @RequestParam(required = false) BigDecimal soTienDauCa,
            @RequestParam(required = false) String ghiChu) {
        try {
            GiaoCaDTO giaoCa = giaoCaService.createGiaoCa(phanCaId, nhanVienGiaoId, soTienDauCa, ghiChu);
            return ResponseEntity.status(HttpStatus.CREATED).body(giaoCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<GiaoCaDTO> confirmGiaoCa(
            @PathVariable Integer id,
            @RequestParam Integer nhanVienNhanId,
            @Valid @RequestBody GiaoCaDTO giaoCaDTO) {
        try {
            GiaoCaDTO confirmedGiaoCa = giaoCaService.confirmGiaoCa(id, nhanVienNhanId, giaoCaDTO);
            return ResponseEntity.ok(confirmedGiaoCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<GiaoCaDTO> cancelGiaoCa(
            @PathVariable Integer id,
            @RequestParam String lyDo) {
        try {
            GiaoCaDTO cancelledGiaoCa = giaoCaService.cancelGiaoCa(id, lyDo);
            return ResponseEntity.ok(cancelledGiaoCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Query endpoints
    @GetMapping("/phan-ca/{phanCaId}")
    public ResponseEntity<List<GiaoCaDTO>> getByPhanCaId(@PathVariable Integer phanCaId) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getByPhanCaId(phanCaId);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien-giao/{nhanVienId}")
    public ResponseEntity<List<GiaoCaDTO>> getByNhanVienGiaoId(@PathVariable Integer nhanVienId) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getByNhanVienGiaoId(nhanVienId);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien-nhan/{nhanVienId}")
    public ResponseEntity<List<GiaoCaDTO>> getByNhanVienNhanId(@PathVariable Integer nhanVienId) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getByNhanVienNhanId(nhanVienId);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<GiaoCaDTO>> getByTrangThai(@PathVariable Integer trangThai) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getByTrangThai(trangThai);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}")
    public ResponseEntity<List<GiaoCaDTO>> getByNhanVienId(@PathVariable Integer nhanVienId) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getByNhanVienId(nhanVienId);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ca/{caId}")
    public ResponseEntity<List<GiaoCaDTO>> getByCaId(@PathVariable Integer caId) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getByCaId(caId);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<List<GiaoCaDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getByDateRange(startDate, endDate);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}/date-range")
    public ResponseEntity<List<GiaoCaDTO>> getByNhanVienIdAndDateRange(
            @PathVariable Integer nhanVienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getByNhanVienIdAndDateRange(nhanVienId, startDate, endDate);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ca/{caId}/date-range")
    public ResponseEntity<List<GiaoCaDTO>> getByCaIdAndDateRange(
            @PathVariable Integer caId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getByCaIdAndDateRange(caId, startDate, endDate);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}/pending")
    public ResponseEntity<List<GiaoCaDTO>> getPendingGiaoCaByNhanVienId(@PathVariable Integer nhanVienId) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getPendingGiaoCaByNhanVienId(nhanVienId);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}/pending-for")
    public ResponseEntity<List<GiaoCaDTO>> getPendingGiaoCaForNhanVienId(@PathVariable Integer nhanVienId) {
        try {
            List<GiaoCaDTO> giaoCaList = giaoCaService.getPendingGiaoCaForNhanVienId(nhanVienId);
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Statistics endpoints
    @GetMapping("/nhan-vien/{nhanVienId}/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueByNhanVienIdAndDateRange(
            @PathVariable Integer nhanVienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            BigDecimal revenue = giaoCaService.getTotalRevenueByNhanVienIdAndDateRange(nhanVienId, startDate, endDate);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ca/{caId}/revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueByCaIdAndDateRange(
            @PathVariable Integer caId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            BigDecimal revenue = giaoCaService.getTotalRevenueByCaIdAndDateRange(caId, startDate, endDate);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}/count")
    public ResponseEntity<Long> countGiaoCaByNhanVienIdAndDateRange(
            @PathVariable Integer nhanVienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Long count = giaoCaService.countGiaoCaByNhanVienIdAndDateRange(nhanVienId, startDate, endDate);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ca/{caId}/count")
    public ResponseEntity<Long> countGiaoCaByCaIdAndDateRange(
            @PathVariable Integer caId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Long count = giaoCaService.countGiaoCaByCaIdAndDateRange(caId, startDate, endDate);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<GiaoCaDTO>> getRecentGiaoCa(@RequestParam(defaultValue = "10") int limit) {
        try {
            System.out.println("=== GIAO CA CONTROLLER DEBUG ===");
            System.out.println("Received request for recent giao ca with limit: " + limit);
            
            List<GiaoCaDTO> giaoCaList = giaoCaService.getRecentGiaoCa(limit);
            
            System.out.println("Controller returning " + giaoCaList.size() + " giao ca records");
            return ResponseEntity.ok(giaoCaList);
        } catch (Exception e) {
            System.err.println("Error in getRecentGiaoCa controller: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
