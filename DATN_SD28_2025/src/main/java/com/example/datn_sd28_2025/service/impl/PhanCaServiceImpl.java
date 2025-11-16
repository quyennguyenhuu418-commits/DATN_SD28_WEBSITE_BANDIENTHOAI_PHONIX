package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.PhanCaDTO;
import com.example.datn_sd28_2025.entity.PhanCa;
import com.example.datn_sd28_2025.repository.PhanCaRepository;
import com.example.datn_sd28_2025.service.PhanCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PhanCaServiceImpl implements PhanCaService {

    @Autowired
    private PhanCaRepository phanCaRepository;

    @Override
    public List<PhanCaDTO> getAll() {
        return phanCaRepository.findAllWithRelations().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PhanCaDTO> getById(Integer id) {
        return phanCaRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public PhanCaDTO save(PhanCaDTO phanCaDTO) {
        // Check if phan ca already exists for same nhan vien, ca, and date
        if (phanCaRepository.countByNhanVienIdAndNgayLamViecAndCaId(
                phanCaDTO.getNhanVienId(), phanCaDTO.getNgayLamViec(), phanCaDTO.getCaId()) > 0) {
            throw new RuntimeException("Nhân viên đã được phân ca này trong ngày này");
        }

        // Check if ca is already assigned to another employee on the same date
        // Chỉ kiểm tra nếu ca đã được phân cho nhân viên KHÁC
        long existingAssignments = phanCaRepository.countByCaIdAndNgayLamViec(phanCaDTO.getCaId(), phanCaDTO.getNgayLamViec());
        if (existingAssignments > 0) {
            // Kiểm tra xem ca đã được phân cho nhân viên khác chưa
            List<PhanCa> existingPhanCa = phanCaRepository.findByCaIdAndNgayLamViec(phanCaDTO.getCaId(), phanCaDTO.getNgayLamViec());
            boolean assignedToDifferentEmployee = existingPhanCa.stream()
                    .anyMatch(pc -> !pc.getNhanVienId().equals(phanCaDTO.getNhanVienId()));
            
            if (assignedToDifferentEmployee) {
                throw new RuntimeException("Ca này đã được phân cho nhân viên khác trong ngày này");
            }
        }

        PhanCa phanCa = convertToEntity(phanCaDTO);
        
        // Đảm bảo trangThai mặc định là 0 (Chưa bắt đầu) nếu null
        if (phanCa.getTrangThai() == null) {
            phanCa.setTrangThai(0);
        }
        
        // Đảm bảo không tự động set gioBatDauThucTe nếu chưa bắt đầu
        if (phanCa.getTrangThai() == 0 && phanCa.getGioBatDauThucTe() != null) {
            phanCa.setGioBatDauThucTe(null);
        }
        
        // Tự động set nguoiTao từ SecurityContext (nhân viên đang đăng nhập)
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            phanCa.setNguoiTao(currentUsername);
            phanCa.setNguoiCapNhat(currentUsername);
        }
        
        PhanCa savedPhanCa = phanCaRepository.save(phanCa);
        return convertToDto(savedPhanCa);
    }

    @Override
    @Transactional
    public PhanCaDTO update(Integer id, PhanCaDTO phanCaDTO) {
        PhanCa existingPhanCa = phanCaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phân ca không tồn tại"));

        // Check if phan ca already exists for another record
        if (phanCaRepository.countByNhanVienIdAndNgayLamViecAndCaId(
                phanCaDTO.getNhanVienId(), phanCaDTO.getNgayLamViec(), phanCaDTO.getCaId()) > 0 &&
                !existingPhanCa.getNhanVienId().equals(phanCaDTO.getNhanVienId()) &&
                !existingPhanCa.getCaId().equals(phanCaDTO.getCaId()) &&
                !existingPhanCa.getNgayLamViec().equals(phanCaDTO.getNgayLamViec())) {
            throw new RuntimeException("Nhân viên đã được phân ca này trong ngày này");
        }

        // Check if ca is already assigned to another employee on the same date (excluding current record)
        if (!existingPhanCa.getCaId().equals(phanCaDTO.getCaId()) || 
            !existingPhanCa.getNgayLamViec().equals(phanCaDTO.getNgayLamViec())) {
            
            long existingAssignments = phanCaRepository.countByCaIdAndNgayLamViec(phanCaDTO.getCaId(), phanCaDTO.getNgayLamViec());
            if (existingAssignments > 0) {
                // Kiểm tra xem ca đã được phân cho nhân viên khác chưa
                List<PhanCa> existingPhanCaList = phanCaRepository.findByCaIdAndNgayLamViec(phanCaDTO.getCaId(), phanCaDTO.getNgayLamViec());
                boolean assignedToDifferentEmployee = existingPhanCaList.stream()
                        .anyMatch(pc -> !pc.getNhanVienId().equals(phanCaDTO.getNhanVienId()) && 
                                       !pc.getId().equals(existingPhanCa.getId()));
                
                if (assignedToDifferentEmployee) {
                    throw new RuntimeException("Ca này đã được phân cho nhân viên khác trong ngày này");
                }
            }
        }

        // Update fields
        existingPhanCa.setNhanVienId(phanCaDTO.getNhanVienId());
        existingPhanCa.setCaId(phanCaDTO.getCaId());
        existingPhanCa.setNgayLamViec(phanCaDTO.getNgayLamViec());
        
        // Chỉ cho phép update trangThai thông qua API startShift/endShift
        // Nếu DTO có trangThai khác với hiện tại, chỉ cho phép nếu hợp lý
        Integer newTrangThai = phanCaDTO.getTrangThai();
        if (newTrangThai == null) {
            newTrangThai = existingPhanCa.getTrangThai(); // Giữ nguyên
        }
        
        // Validation: Đảm bảo trangThai và gioBatDauThucTe/gioKetThucThucTe hợp lý
        if (newTrangThai == 0) {
            // Chưa bắt đầu: phải không có gioBatDauThucTe và gioKetThucThucTe
            existingPhanCa.setTrangThai(0);
            existingPhanCa.setGioBatDauThucTe(null);
            existingPhanCa.setGioKetThucThucTe(null);
        } else if (newTrangThai == 1) {
            // Đang làm: phải có gioBatDauThucTe
            existingPhanCa.setTrangThai(1);
            if (existingPhanCa.getGioBatDauThucTe() == null) {
                // Nếu chưa có thì set là bây giờ (nhưng chỉ khi update qua DTO, không nên dùng API startShift)
                existingPhanCa.setGioBatDauThucTe(phanCaDTO.getGioBatDauThucTe() != null 
                    ? phanCaDTO.getGioBatDauThucTe() 
                    : java.time.LocalDateTime.now());
            }
            existingPhanCa.setGioKetThucThucTe(null);
        } else if (newTrangThai == 2) {
            // Đã kết thúc: phải có cả gioBatDauThucTe và gioKetThucThucTe
            existingPhanCa.setTrangThai(2);
            if (existingPhanCa.getGioKetThucThucTe() == null) {
                existingPhanCa.setGioKetThucThucTe(phanCaDTO.getGioKetThucThucTe() != null 
                    ? phanCaDTO.getGioKetThucThucTe() 
                    : java.time.LocalDateTime.now());
            }
        } else {
            existingPhanCa.setTrangThai(newTrangThai);
        }
        
        // Chỉ update nếu DTO có giá trị
        if (phanCaDTO.getGioBatDauThucTe() != null) {
        existingPhanCa.setGioBatDauThucTe(phanCaDTO.getGioBatDauThucTe());
        }
        if (phanCaDTO.getGioKetThucThucTe() != null) {
        existingPhanCa.setGioKetThucThucTe(phanCaDTO.getGioKetThucThucTe());
        }
        
        existingPhanCa.setGhiChu(phanCaDTO.getGhiChu());
        
        // Tự động set nguoiCapNhat từ SecurityContext (nhân viên đang đăng nhập)
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            existingPhanCa.setNguoiCapNhat(currentUsername);
        }

        PhanCa updatedPhanCa = phanCaRepository.save(existingPhanCa);
        return convertToDto(updatedPhanCa);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!phanCaRepository.existsById(id)) {
            throw new RuntimeException("Phân ca không tồn tại");
        }
        phanCaRepository.deleteById(id);
    }

    @Override
    public List<PhanCaDTO> getByNhanVienId(Integer nhanVienId) {
        return phanCaRepository.findByNhanVienId(nhanVienId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhanCaDTO> getByCaId(Integer caId) {
        return phanCaRepository.findByCaId(caId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhanCaDTO> getByNgayLamViec(LocalDate ngayLamViec) {
        return phanCaRepository.findByNgayLamViec(ngayLamViec).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhanCaDTO> getByNhanVienIdAndNgayLamViec(Integer nhanVienId, LocalDate ngayLamViec) {
        return phanCaRepository.findByNhanVienIdAndNgayLamViec(nhanVienId, ngayLamViec).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhanCaDTO> getByCaIdAndNgayLamViec(Integer caId, LocalDate ngayLamViec) {
        return phanCaRepository.findByCaIdAndNgayLamViec(caId, ngayLamViec).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhanCaDTO> getByTrangThai(Integer trangThai) {
        return phanCaRepository.findByTrangThai(trangThai).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhanCaDTO> getByNhanVienIdAndDateRange(Integer nhanVienId, LocalDate startDate, LocalDate endDate) {
        return phanCaRepository.findByNhanVienIdAndDateRange(nhanVienId, startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhanCaDTO> getByCaIdAndDateRange(Integer caId, LocalDate startDate, LocalDate endDate) {
        return phanCaRepository.findByCaIdAndDateRange(caId, startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhanCaDTO> getUpcomingShiftsByNhanVienId(Integer nhanVienId) {
        LocalDate today = LocalDate.now();
        return phanCaRepository.findUpcomingShiftsByNhanVienId(nhanVienId, today).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhanCaDTO> getCurrentShiftsByNhanVienId(Integer nhanVienId) {
        LocalDate today = LocalDate.now();
        return phanCaRepository.findCurrentShiftsByNhanVienId(nhanVienId, today).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PhanCaDTO startShift(Integer phanCaId) {
        PhanCa phanCa = phanCaRepository.findById(phanCaId)
                .orElseThrow(() -> new RuntimeException("Phân ca không tồn tại"));

        if (phanCa.getTrangThai() != 0) {
            throw new RuntimeException("Phân ca không ở trạng thái chưa bắt đầu");
        }

        // VALIDATION MỚI: Kiểm tra ca trước của cùng nhân viên trong cùng ngày
        // Chỉ cho phép bắt đầu ca tiếp theo khi:
        // 1. Không có ca trước đang làm (status = 1) của cùng nhân viên
        // 2. Nếu có ca trước đã kết thúc (status = 2), có thể cần kiểm tra giao ca (tuỳ nghiệp vụ)
        
        if (phanCa.getCa() == null) {
            throw new RuntimeException("Phân ca không có thông tin ca làm việc");
        }
        
        Integer nhanVienId = phanCa.getNhanVienId();
        LocalDate ngayLamViec = phanCa.getNgayLamViec();
        // Convert LocalTime to LocalDateTime by combining with ngayLamViec
        LocalDateTime gioBatDauCa = LocalDateTime.of(ngayLamViec, phanCa.getCa().getGioBatDau());
        
        // Lấy tất cả phân ca của cùng nhân viên trong cùng ngày
        List<PhanCa> sameDayShifts = phanCaRepository.findByNhanVienIdAndNgayLamViec(nhanVienId, ngayLamViec);
        
        // Tìm ca trước (có giờ kết thúc <= giờ bắt đầu ca hiện tại)
        Optional<PhanCa> previousShiftOpt = sameDayShifts.stream()
                .filter(pc -> {
                    // Bỏ qua ca hiện tại
                    if (pc.getId().equals(phanCaId)) {
                        return false;
                    }
                    // Chỉ lấy ca có giờ kết thúc <= giờ bắt đầu ca hiện tại
                    if (pc.getCa() != null && pc.getCa().getGioKetThuc() != null) {
                        // Convert LocalTime to LocalDateTime by combining with ngayLamViec
                        LocalDateTime gioKetThucCa = LocalDateTime.of(ngayLamViec, pc.getCa().getGioKetThuc());
                        return !gioKetThucCa.isAfter(gioBatDauCa);
                    }
                    return false;
                })
                .max((pc1, pc2) -> {
                    // Lấy ca trước gần nhất (giờ kết thúc lớn nhất)
                    if (pc1.getCa() != null && pc2.getCa() != null &&
                        pc1.getCa().getGioKetThuc() != null && pc2.getCa().getGioKetThuc() != null) {
                        return pc1.getCa().getGioKetThuc().compareTo(pc2.getCa().getGioKetThuc());
                    }
                    return 0;
                });
        
        if (previousShiftOpt.isPresent()) {
            PhanCa previousShift = previousShiftOpt.get();
            
            // Nếu ca trước đang làm (status = 1), KHÔNG cho phép bắt đầu ca tiếp theo
            if (previousShift.getTrangThai() != null && previousShift.getTrangThai() == 1) {
                throw new RuntimeException("Không thể bắt đầu ca tiếp theo vì ca trước ("
                        + (previousShift.getCa() != null ? previousShift.getCa().getTenCa() : "N/A")
                        + ") vẫn đang làm. Vui lòng kết thúc ca trước hoặc đợi ca trước kết thúc.");
            }
            
            // Nếu ca trước đã kết thúc (status = 2), cho phép bắt đầu ca tiếp theo
            // (không cần kiểm tra giao ca vì có thể nhân viên tự làm tiếp)
            if (previousShift.getTrangThai() != null && previousShift.getTrangThai() == 2) {
                System.out.println("[START_SHIFT] ✅ Ca trước (ID: " + previousShift.getId() + 
                        ", Ca: " + (previousShift.getCa() != null ? previousShift.getCa().getTenCa() : "N/A") + 
                        ") đã kết thúc, cho phép bắt đầu ca tiếp theo");
            }
        } else {
            System.out.println("[START_SHIFT] ✅ Không có ca trước, cho phép bắt đầu ca này");
        }

        phanCa.setTrangThai(1); // Đang làm
        phanCa.setGioBatDauThucTe(LocalDateTime.now());
        phanCa.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());

        PhanCa updatedPhanCa = phanCaRepository.save(phanCa);
        return convertToDto(updatedPhanCa);
    }

    @Override
    @Transactional
    public PhanCaDTO endShift(Integer phanCaId) {
        PhanCa phanCa = phanCaRepository.findById(phanCaId)
                .orElseThrow(() -> new RuntimeException("Phân ca không tồn tại"));

        if (phanCa.getTrangThai() != 1) {
            throw new RuntimeException("Phân ca không ở trạng thái đang làm");
        }

        phanCa.setTrangThai(2); // Đã kết thúc
        phanCa.setGioKetThucThucTe(LocalDateTime.now());
        phanCa.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());

        PhanCa updatedPhanCa = phanCaRepository.save(phanCa);
        return convertToDto(updatedPhanCa);
    }

    @Override
    @Transactional
    public PhanCaDTO markAbsent(Integer phanCaId, String ghiChu) {
        PhanCa phanCa = phanCaRepository.findById(phanCaId)
                .orElseThrow(() -> new RuntimeException("Phân ca không tồn tại"));

        phanCa.setTrangThai(3); // Vắng mặt
        phanCa.setGhiChu(ghiChu);
        phanCa.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());

        PhanCa updatedPhanCa = phanCaRepository.save(phanCa);
        return convertToDto(updatedPhanCa);
    }

    @Override
    public boolean existsByNhanVienIdAndNgayLamViecAndCaId(Integer nhanVienId, LocalDate ngayLamViec, Integer caId) {
        return phanCaRepository.countByNhanVienIdAndNgayLamViecAndCaId(nhanVienId, ngayLamViec, caId) > 0;
    }

    private PhanCaDTO convertToDto(PhanCa phanCa) {
        PhanCaDTO.PhanCaDTOBuilder builder = PhanCaDTO.builder()
                .id(phanCa.getId())
                .nhanVienId(phanCa.getNhanVienId())
                .caId(phanCa.getCaId())
                .ngayLamViec(phanCa.getNgayLamViec())
                .trangThai(phanCa.getTrangThai())
                .trangThaiText(getTrangThaiText(phanCa.getTrangThai()))
                .gioBatDauThucTe(phanCa.getGioBatDauThucTe())
                .gioKetThucThucTe(phanCa.getGioKetThucThucTe())
                .ghiChu(phanCa.getGhiChu())
                .ngayTao(phanCa.getNgayTao())
                .ngayCapNhat(phanCa.getNgayCapNhat())
                .nguoiTao(phanCa.getNguoiTao())
                .nguoiCapNhat(phanCa.getNguoiCapNhat());

        // Load nhanVien data if available
        if (phanCa.getNhanVien() != null) {
            builder.nhanVien(PhanCaDTO.NhanVienInfo.builder()
                    .id(phanCa.getNhanVien().getId())
                    .hoTen(phanCa.getNhanVien().getHoTen())
                    .build());
        }

        // Load ca data if available
        if (phanCa.getCa() != null) {
            builder.ca(PhanCaDTO.CaInfo.builder()
                    .id(phanCa.getCa().getId())
                    .tenCa(phanCa.getCa().getTenCa())
                    .gioBatDau  (phanCa.getCa().getGioBatDau())
                    .gioKetThuc(phanCa.getCa().getGioKetThuc())
                    .moTa(phanCa.getCa().getMoTa())
                    .build());
        }

        return builder.build();
    }

    private PhanCa convertToEntity(PhanCaDTO phanCaDTO) {
        return PhanCa.builder()
                .id(phanCaDTO.getId())
                .nhanVienId(phanCaDTO.getNhanVienId())
                .caId(phanCaDTO.getCaId())
                .ngayLamViec(phanCaDTO.getNgayLamViec())
                .trangThai(phanCaDTO.getTrangThai())
                .gioBatDauThucTe(phanCaDTO.getGioBatDauThucTe())
                .gioKetThucThucTe(phanCaDTO.getGioKetThucThucTe())
                .ghiChu(phanCaDTO.getGhiChu())
                .nguoiTao(phanCaDTO.getNguoiTao())
                .nguoiCapNhat(phanCaDTO.getNguoiCapNhat())
                .build();
    }

    private String getTrangThaiText(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        return switch (trangThai) {
            case 0 -> "Chưa bắt đầu";
            case 1 -> "Đang làm";
            case 2 -> "Đã kết thúc";
            case 3 -> "Vắng mặt";
            default -> "Không xác định";
        };
    }

    @Override
    public Map<String, Object> getShiftSummary(Integer shiftId, LocalDate ngayLamViec) {
        // Mock data for testing - replace with real calculation
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRevenue", 1500000); // Mock: 1.5M VND
        summary.put("totalOrders", 25); // Mock: 25 orders
        summary.put("shiftId", shiftId);
        summary.put("date", ngayLamViec.toString());
        return summary;
    }
}
