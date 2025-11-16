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
import java.time.ZoneId;
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

    @GetMapping("/today-shift")
    public ResponseEntity<PhanCaDTO> getTodayShift(@RequestParam(required = false) Integer nhanVienId) {
        try {
            LocalDate today = LocalDate.now();
            
            List<PhanCaDTO> phanCaList;
            
            if (nhanVienId != null) {
                phanCaList = phanCaService.getByNhanVienIdAndDateRange(nhanVienId, today, today);
            } else {
                phanCaList = phanCaService.getByNgayLamViec(today);
            }
            
            // Ưu tiên: Tìm phân ca chưa bắt đầu (status = 0 hoặc null)
            Optional<PhanCaDTO> notStartedShift = phanCaList.stream()
                    .filter(pc -> (pc.getTrangThai() == null || pc.getTrangThai() == 0))
                    .min((pc1, pc2) -> {
                        if (pc1.getCa() != null && pc2.getCa() != null) {
                            return pc1.getCa().getGioBatDau().compareTo(pc2.getCa().getGioBatDau());
                        }
                        return 0;
                    });
            
            if (notStartedShift.isPresent()) {
                return ResponseEntity.ok(notStartedShift.get());
            }
            
            // Fallback: Tìm phân ca đang làm (status = 1)
            Optional<PhanCaDTO> activeShift = phanCaList.stream()
                    .filter(pc -> pc.getTrangThai() == 1)
                    .min((pc1, pc2) -> {
                        if (pc1.getCa() != null && pc2.getCa() != null) {
                            return pc1.getCa().getGioBatDau().compareTo(pc2.getCa().getGioBatDau());
                        }
                        return 0;
                    });
            
            if (activeShift.isPresent()) {
                return ResponseEntity.ok(activeShift.get());
            }
            
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/next-shift")
    public ResponseEntity<PhanCaDTO> getNextShift(@RequestParam(required = false) String date, 
                                                    @RequestParam(required = false) Integer phanCaId) {
        try {
            // Nếu có phanCaId, tìm phân ca tiếp theo sau phân ca đó
            if (phanCaId != null) {
                Optional<PhanCaDTO> currentPhanCaOpt = phanCaService.getById(phanCaId);
                if (currentPhanCaOpt.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                PhanCaDTO currentPhanCa = currentPhanCaOpt.get();
                LocalDate currentNgayLamViec = currentPhanCa.getNgayLamViec();
                LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                
                if (currentPhanCa.getCa() == null) {
                    return ResponseEntity.notFound().build();
                }
                
                LocalTime gioKetThucCurrentCa = currentPhanCa.getCa().getGioKetThuc();
                
                // Tìm ca chưa bắt đầu tiếp theo
                // 1. Đầu tiên tìm trong cùng ngày (nếu giờ bắt đầu >= giờ kết thúc ca hiện tại)
                // 2. Nếu không có trong cùng ngày, mới tìm từ ngày tiếp theo
                
                // Bước 1: Tìm trong cùng ngày (chỉ nếu ngày hiện tại >= hôm nay)
                if (!currentNgayLamViec.isBefore(today)) {
                    List<PhanCaDTO> sameDayPhanCaList = phanCaService.getByNgayLamViec(currentNgayLamViec);
                    
                    Optional<PhanCaDTO> sameDayNextShift = sameDayPhanCaList.stream()
                            .filter(pc -> {
                                // Bỏ qua ca hiện tại
                                if (pc.getId().equals(phanCaId)) {
                                    return false;
                                }
                                // Chỉ lấy ca chưa bắt đầu (trangThai = 0 hoặc null)
                                if (pc.getTrangThai() != null && pc.getTrangThai() != 0) {
                                    return false;
                                }
                                // Đảm bảo ca có thông tin ca
                                if (pc.getCa() == null) {
                                    return false;
                                }
                                // Đảm bảo giờ bắt đầu >= giờ kết thúc ca hiện tại
                                LocalTime gioBatDauNextCa = pc.getCa().getGioBatDau();
                                if (gioBatDauNextCa.isBefore(gioKetThucCurrentCa)) {
                                    return false;
                                }
                                return true;
                            })
                            .sorted((pc1, pc2) -> {
                                // Sắp xếp theo giờ bắt đầu (ca sớm nhất)
                                if (pc1.getCa() != null && pc2.getCa() != null) {
                                    return pc1.getCa().getGioBatDau().compareTo(pc2.getCa().getGioBatDau());
                                }
                                return 0;
                            })
                            .findFirst();
                    
                    if (sameDayNextShift.isPresent()) {
                        return ResponseEntity.ok(sameDayNextShift.get());
                    }
                }
                
                // Bước 2: Nếu không tìm thấy trong cùng ngày, tìm từ ngày tiếp theo
                // Bắt đầu tìm từ ngày tiếp theo của ngày làm việc hiện tại
                // Nếu ngày làm việc hiện tại < hôm nay, thì bắt đầu từ hôm nay
                LocalDate startSearchDate;
                if (currentNgayLamViec.isBefore(today)) {
                    startSearchDate = today;
                } else {
                    startSearchDate = currentNgayLamViec.plusDays(1);
                }
                
                LocalDate endDate = startSearchDate.plusDays(30); // Tìm trong 30 ngày tới
                
                // Tìm ca sớm nhất chưa bắt đầu từ ngày tiếp theo trở đi
                Optional<PhanCaDTO> nextDayShift = Optional.empty();
                for (LocalDate searchDate = startSearchDate; !searchDate.isAfter(endDate) && !nextDayShift.isPresent(); searchDate = searchDate.plusDays(1)) {
                    // Đảm bảo chỉ tìm ca từ ngày hôm nay trở đi
                    if (searchDate.isBefore(today)) {
                        continue;
                    }
                    
                    List<PhanCaDTO> dayPhanCaList = phanCaService.getByNgayLamViec(searchDate);
                    
                    // Chấp nhận tất cả ca chưa bắt đầu từ ngày tiếp theo
                    nextDayShift = dayPhanCaList.stream()
                            .filter(pc -> {
                                if (pc.getCa() == null) {
                                    return false;
                                }
                                // Chỉ lấy ca chưa bắt đầu (trangThai = 0 hoặc null)
                                if (pc.getTrangThai() != null && pc.getTrangThai() != 0) {
                                    return false;
                                }
                                // Đảm bảo ngày >= hôm nay
                                if (pc.getNgayLamViec().isBefore(today)) {
                                    return false;
                                }
                                // Đảm bảo là ngày tiếp theo (không phải cùng ngày)
                                if (pc.getNgayLamViec().equals(currentNgayLamViec)) {
                                    return false;
                                }
                                return true;
                            })
                            .sorted((pc1, pc2) -> {
                                // Sắp xếp theo ngày, sau đó theo giờ bắt đầu
                                int dateCompare = pc1.getNgayLamViec().compareTo(pc2.getNgayLamViec());
                                if (dateCompare != 0) return dateCompare;
                                if (pc1.getCa() != null && pc2.getCa() != null) {
                                    return pc1.getCa().getGioBatDau().compareTo(pc2.getCa().getGioBatDau());
                                }
                                return 0;
                            })
                            .findFirst();
                    
                    if (nextDayShift.isPresent()) {
                        break;
                    }
                }
                
                if (nextDayShift.isPresent()) {
                    return ResponseEntity.ok(nextDayShift.get());
                }
                
                return ResponseEntity.notFound().build();
            }
            
            // Logic cũ: Tìm ca tiếp theo theo date
            if (date == null || date.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            try {
                LocalDate ngayLamViec = LocalDate.parse(date);
                List<PhanCaDTO> phanCaList = phanCaService.getByNgayLamViec(ngayLamViec);
                
                if (phanCaList.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                
                // Tìm ca chưa bắt đầu (status = 0) sớm nhất trong ngày
                Optional<PhanCaDTO> nextShift = phanCaList.stream()
                        .filter(pc -> {
                            if (pc.getCa() == null) return false;
                            // Chỉ lấy ca chưa bắt đầu (trangThai = 0 hoặc null)
                            if (pc.getTrangThai() != null && pc.getTrangThai() != 0) {
                                return false;
                            }
                            return true;
                        })
                        .sorted((pc1, pc2) -> {
                            // Sắp xếp theo giờ bắt đầu
                            if (pc1.getCa() != null && pc2.getCa() != null) {
                                return pc1.getCa().getGioBatDau().compareTo(pc2.getCa().getGioBatDau());
                            }
                            return 0;
                        })
                        .findFirst();
                
                if (nextShift.isPresent()) {
                    return ResponseEntity.ok(nextShift.get());
                } else {
                    return ResponseEntity.notFound().build();
                }
            } catch (java.time.format.DateTimeParseException e) {
                return ResponseEntity.badRequest().build();
            }
        } catch (Exception e) {
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
