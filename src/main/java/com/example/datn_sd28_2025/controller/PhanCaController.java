package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.PhanCaDTO;
import com.example.datn_sd28_2025.service.PhanCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/phan-ca")
@CrossOrigin(origins = "*")
public class PhanCaController {

    @Autowired
    private PhanCaService phanCaService;

    @GetMapping
    public ResponseEntity<List<PhanCaDTO>> getAll() {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getAll();
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhanCaDTO> getById(@PathVariable Integer id) {
        try {
            Optional<PhanCaDTO> phanCa = phanCaService.getById(id);
            return phanCa.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<PhanCaDTO> create(@Valid @RequestBody PhanCaDTO phanCaDTO) {
        try {
            PhanCaDTO createdPhanCa = phanCaService.save(phanCaDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPhanCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhanCaDTO> update(@PathVariable Integer id, @Valid @RequestBody PhanCaDTO phanCaDTO) {
        try {
            PhanCaDTO updatedPhanCa = phanCaService.update(id, phanCaDTO);
            return ResponseEntity.ok(updatedPhanCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            phanCaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Query endpoints
    @GetMapping("/nhan-vien/{nhanVienId}")
    public ResponseEntity<List<PhanCaDTO>> getByNhanVienId(@PathVariable Integer nhanVienId) {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getByNhanVienId(nhanVienId);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ca/{caId}")
    public ResponseEntity<List<PhanCaDTO>> getByCaId(@PathVariable Integer caId) {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getByCaId(caId);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ngay-lam-viec")
    public ResponseEntity<List<PhanCaDTO>> getByNgayLamViec(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayLamViec) {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getByNgayLamViec(ngayLamViec);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}/ngay-lam-viec")
    public ResponseEntity<List<PhanCaDTO>> getByNhanVienIdAndNgayLamViec(
            @PathVariable Integer nhanVienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayLamViec) {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getByNhanVienIdAndNgayLamViec(nhanVienId, ngayLamViec);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ca/{caId}/ngay-lam-viec")
    public ResponseEntity<List<PhanCaDTO>> getByCaIdAndNgayLamViec(
            @PathVariable Integer caId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayLamViec) {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getByCaIdAndNgayLamViec(caId, ngayLamViec);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<PhanCaDTO>> getByTrangThai(@PathVariable Integer trangThai) {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getByTrangThai(trangThai);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}/date-range")
    public ResponseEntity<List<PhanCaDTO>> getByNhanVienIdAndDateRange(
            @PathVariable Integer nhanVienId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getByNhanVienIdAndDateRange(nhanVienId, startDate, endDate);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ca/{caId}/date-range")
    public ResponseEntity<List<PhanCaDTO>> getByCaIdAndDateRange(
            @PathVariable Integer caId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getByCaIdAndDateRange(caId, startDate, endDate);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}/upcoming")
    public ResponseEntity<List<PhanCaDTO>> getUpcomingShiftsByNhanVienId(@PathVariable Integer nhanVienId) {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getUpcomingShiftsByNhanVienId(nhanVienId);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nhan-vien/{nhanVienId}/current")
    public ResponseEntity<List<PhanCaDTO>> getCurrentShiftsByNhanVienId(@PathVariable Integer nhanVienId) {
        try {
            List<PhanCaDTO> phanCaList = phanCaService.getCurrentShiftsByNhanVienId(nhanVienId);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/current-shift")
    public ResponseEntity<PhanCaDTO> getCurrentShift(@RequestParam String date) {
        try {
            LocalDate ngayLamViec = LocalDate.parse(date);
            List<PhanCaDTO> phanCaList = phanCaService.getByNgayLamViec(ngayLamViec);
            
            // Find current shift (status = 1 - Đang làm)
            Optional<PhanCaDTO> currentShift = phanCaList.stream()
                    .filter(pc -> pc.getTrangThai() == 1)
                    .findFirst();
            
            if (currentShift.isPresent()) {
                return ResponseEntity.ok(currentShift.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/next-shift")
    public ResponseEntity<PhanCaDTO> getNextShift(@RequestParam String date) {
        try {
            LocalDate ngayLamViec = LocalDate.parse(date);
            List<PhanCaDTO> phanCaList = phanCaService.getByNgayLamViec(ngayLamViec);
            
            // Debug: Log all shifts for the date
            System.out.println("All shifts for date " + ngayLamViec + ":");
            phanCaList.forEach(pc -> System.out.println("Shift: " + 
                (pc.getCa() != null ? pc.getCa().getTenCa() : "null") + 
                ", Status: " + pc.getTrangThai() + 
                ", Time: " + (pc.getCa() != null ? pc.getCa().getGioBatDau() : "null")));
            
            // Find current time to determine which shift is currently active
            LocalTime currentTime = LocalTime.now();
            System.out.println("Current time: " + currentTime);
            
            // Find the currently active shift (status = 1)
            Optional<PhanCaDTO> currentShift = phanCaList.stream()
                    .filter(pc -> pc.getTrangThai() == 1)
                    .findFirst();
            
            if (currentShift.isPresent()) {
                System.out.println("Current active shift: " + 
                    (currentShift.get().getCa() != null ? currentShift.get().getCa().getTenCa() : "null"));
                
                // Find next shift: any shift with start time after current shift's start time
                LocalTime currentShiftStartTime = currentShift.get().getCa() != null ? 
                    currentShift.get().getCa().getGioBatDau() : LocalTime.MIN;
                
                Optional<PhanCaDTO> nextShift = phanCaList.stream()
                        .filter(pc -> {
                            if (pc.getCa() == null) return false;
                            // Find shift with start time after current shift's start time
                            return pc.getCa().getGioBatDau().isAfter(currentShiftStartTime);
                        })
                        .sorted((pc1, pc2) -> {
                            if (pc1.getCa() != null && pc2.getCa() != null) {
                                return pc1.getCa().getGioBatDau().compareTo(pc2.getCa().getGioBatDau());
                            }
                            return 0;
                        })
                        .findFirst();
                
                if (nextShift.isPresent()) {
                    System.out.println("Next shift found: " + 
                        (nextShift.get().getCa() != null ? nextShift.get().getCa().getTenCa() : "null"));
                    return ResponseEntity.ok(nextShift.get());
                } else {
                    System.out.println("No next shift found after current shift");
                    return ResponseEntity.notFound().build();
                }
            } else {
                System.out.println("No current active shift found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("Error getting next shift: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/debug-shifts")
    public ResponseEntity<List<PhanCaDTO>> getDebugShifts(@RequestParam String date) {
        try {
            LocalDate ngayLamViec = LocalDate.parse(date);
            List<PhanCaDTO> phanCaList = phanCaService.getByNgayLamViec(ngayLamViec);
            return ResponseEntity.ok(phanCaList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Business endpoints
    @PutMapping("/{id}/start")
    public ResponseEntity<PhanCaDTO> startShift(@PathVariable Integer id) {
        try {
            PhanCaDTO phanCa = phanCaService.startShift(id);
            return ResponseEntity.ok(phanCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/end")
    public ResponseEntity<PhanCaDTO> endShift(@PathVariable Integer id) {
        try {
            PhanCaDTO phanCa = phanCaService.endShift(id);
            return ResponseEntity.ok(phanCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/mark-absent")
    public ResponseEntity<PhanCaDTO> markAbsent(@PathVariable Integer id, @RequestParam String ghiChu) {
        try {
            PhanCaDTO phanCa = phanCaService.markAbsent(id, ghiChu);
            return ResponseEntity.ok(phanCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/shift-summary")
    public ResponseEntity<?> getShiftSummary(@RequestParam Integer shiftId, @RequestParam String date) {
        try {
            LocalDate ngayLamViec = LocalDate.parse(date);
            Map<String, Object> summary = phanCaService.getShiftSummary(shiftId, ngayLamViec);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
