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
        existingPhanCa.setTrangThai(phanCaDTO.getTrangThai());
        existingPhanCa.setGioBatDauThucTe(phanCaDTO.getGioBatDauThucTe());
        existingPhanCa.setGioKetThucThucTe(phanCaDTO.getGioKetThucThucTe());
        existingPhanCa.setGhiChu(phanCaDTO.getGhiChu());
        existingPhanCa.setNguoiCapNhat(phanCaDTO.getNguoiCapNhat());

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

        phanCa.setTrangThai(1); // Đang làm
        phanCa.setGioBatDauThucTe(LocalDateTime.now());
        phanCa.setNguoiCapNhat("SYSTEM");

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
        phanCa.setNguoiCapNhat("SYSTEM");

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
        phanCa.setNguoiCapNhat("SYSTEM");

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
