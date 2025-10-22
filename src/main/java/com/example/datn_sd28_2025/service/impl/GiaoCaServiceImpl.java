package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.GiaoCaDTO;
import com.example.datn_sd28_2025.entity.GiaoCa;
import com.example.datn_sd28_2025.entity.NhanVien;
import com.example.datn_sd28_2025.entity.PhanCa;
import com.example.datn_sd28_2025.repository.GiaoCaRepository;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import com.example.datn_sd28_2025.repository.PhanCaRepository;
import com.example.datn_sd28_2025.service.GiaoCaService;
import com.example.datn_sd28_2025.service.LichSuGiaoCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GiaoCaServiceImpl implements GiaoCaService {

    @Autowired
    private GiaoCaRepository giaoCaRepository;
    
    @Autowired
    private PhanCaRepository phanCaRepository;
    
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    @Autowired
    private LichSuGiaoCaService lichSuGiaoCaService;

    @Override
    public List<GiaoCaDTO> getAll() {
        return giaoCaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<GiaoCaDTO> getById(Integer id) {
        return giaoCaRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public GiaoCaDTO save(GiaoCaDTO giaoCaDTO) {
        // Check if maGiaoCa already exists
        if (giaoCaRepository.existsByMaGiaoCa(giaoCaDTO.getMaGiaoCa())) {
            throw new RuntimeException("Mã giao ca đã tồn tại trong hệ thống");
        }

        GiaoCa giaoCa = convertToEntity(giaoCaDTO);
        GiaoCa savedGiaoCa = giaoCaRepository.save(giaoCa);
        
        // Log action
        lichSuGiaoCaService.logAction(savedGiaoCa.getId(), "TAO_MOI", 
                savedGiaoCa.getNhanVienGiaoId(), "Tạo giao ca mới");
        
        return convertToDto(savedGiaoCa);
    }

    @Override
    @Transactional
    public GiaoCaDTO update(Integer id, GiaoCaDTO giaoCaDTO) {
        GiaoCa existingGiaoCa = giaoCaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Giao ca không tồn tại"));

        // Update fields
        existingGiaoCa.setNhanVienNhanId(giaoCaDTO.getNhanVienNhanId());
        existingGiaoCa.setSoTienCuoiCa(giaoCaDTO.getSoTienCuoiCa());
        existingGiaoCa.setSoTienThuThem(giaoCaDTO.getSoTienThuThem());
        existingGiaoCa.setSoTienChiRa(giaoCaDTO.getSoTienChiRa());
        existingGiaoCa.setTongDoanhThu(giaoCaDTO.getTongDoanhThu());
        existingGiaoCa.setSoDonHang(giaoCaDTO.getSoDonHang());
        existingGiaoCa.setSoDonHangThanhToanTienMat(giaoCaDTO.getSoDonHangThanhToanTienMat());
        existingGiaoCa.setSoDonHangThanhToanChuyenKhoan(giaoCaDTO.getSoDonHangThanhToanChuyenKhoan());
        existingGiaoCa.setBaoCaoCongViec(giaoCaDTO.getBaoCaoCongViec());
        existingGiaoCa.setSuCoBatThuong(giaoCaDTO.getSuCoBatThuong());
        existingGiaoCa.setCongViecTonDong(giaoCaDTO.getCongViecTonDong());
        existingGiaoCa.setGhiChu(giaoCaDTO.getGhiChu());
        existingGiaoCa.setNguoiCapNhat(giaoCaDTO.getNguoiCapNhat());

        GiaoCa updatedGiaoCa = giaoCaRepository.save(existingGiaoCa);
        
        // Log action
        lichSuGiaoCaService.logAction(updatedGiaoCa.getId(), "CAP_NHAT", 
                updatedGiaoCa.getNhanVienGiaoId(), "Cập nhật giao ca");
        
        return convertToDto(updatedGiaoCa);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!giaoCaRepository.existsById(id)) {
            throw new RuntimeException("Giao ca không tồn tại");
        }
        giaoCaRepository.deleteById(id);
    }

    @Override
    public List<GiaoCaDTO> getByPhanCaId(Integer phanCaId) {
        return giaoCaRepository.findByPhanCaId(phanCaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GiaoCaDTO> getByNhanVienGiaoId(Integer nhanVienGiaoId) {
        return giaoCaRepository.findByNhanVienGiaoId(nhanVienGiaoId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GiaoCaDTO> getByNhanVienNhanId(Integer nhanVienNhanId) {
        return giaoCaRepository.findByNhanVienNhanId(nhanVienNhanId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GiaoCaDTO> getByTrangThai(Integer trangThai) {
        return giaoCaRepository.findByTrangThai(trangThai).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GiaoCaDTO> getByNhanVienId(Integer nhanVienId) {
        return giaoCaRepository.findByNhanVienId(nhanVienId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GiaoCaDTO> getByCaId(Integer caId) {
        return giaoCaRepository.findByCaId(caId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GiaoCaDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return giaoCaRepository.findByDateRange(startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GiaoCaDTO> getByNhanVienIdAndDateRange(Integer nhanVienId, LocalDateTime startDate, LocalDateTime endDate) {
        return giaoCaRepository.findByNhanVienIdAndDateRange(nhanVienId, startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GiaoCaDTO> getByCaIdAndDateRange(Integer caId, LocalDateTime startDate, LocalDateTime endDate) {
        return giaoCaRepository.findByCaIdAndDateRange(caId, startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GiaoCaDTO> getPendingGiaoCaByNhanVienId(Integer nhanVienId) {
        return giaoCaRepository.findPendingGiaoCaByNhanVienId(nhanVienId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GiaoCaDTO> getPendingGiaoCaForNhanVienId(Integer nhanVienId) {
        // Kiểm tra xem nhân viên có đang trong ca không
        // Sử dụng múi giờ Việt Nam
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        System.out.println("=== DEBUG GIAO CA ===");
        System.out.println("NhanVienId: " + nhanVienId);
        System.out.println("Today (Java Vietnam): " + today);
        
        // Tìm ca làm việc hôm nay - sử dụng cả ID và tìm kiếm rộng hơn
        List<PhanCa> todayShifts = phanCaRepository.findByNhanVienIdAndNgayLamViec(nhanVienId, today);
        System.out.println("Found shifts for today by ID: " + todayShifts.size());
        
        // Nếu không tìm thấy ca theo ID, thử tìm theo tên nhân viên
        if (todayShifts.isEmpty()) {
            System.out.println("No shifts found by ID, trying to find by employee name...");
            // Lấy thông tin nhân viên để tìm theo tên
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(nhanVienId);
            if (nhanVienOpt.isPresent()) {
                String hoTen = nhanVienOpt.get().getHoTen();
                System.out.println("Employee name: " + hoTen);
                
                // Tìm tất cả ca hôm nay và lọc theo tên
                List<PhanCa> allTodayShifts = phanCaRepository.findByNgayLamViec(today);
                todayShifts = allTodayShifts.stream()
                    .filter(shift -> shift.getNhanVien() != null && 
                           shift.getNhanVien().getHoTen().equals(hoTen))
                    .collect(Collectors.toList());
                System.out.println("Found shifts for today by name: " + todayShifts.size());
            }
        }
        
        // Debug: Kiểm tra tất cả ca của nhân viên này
        List<PhanCa> allShifts = phanCaRepository.findByNhanVienId(nhanVienId);
        System.out.println("All shifts for this employee by ID: " + allShifts.size());
        for (PhanCa shift : allShifts) {
            System.out.println("  - Shift ID: " + shift.getId() + 
                             ", Date: " + shift.getNgayLamViec() + 
                             ", Status: " + shift.getTrangThai() +
                             ", Employee: " + (shift.getNhanVien() != null ? shift.getNhanVien().getHoTen() : "NULL"));
        }
        
        // Chỉ trả về giao ca nếu nhân viên có ca hôm nay
        if (todayShifts.isEmpty()) {
            System.out.println("No shifts found for today, returning empty list");
            return Collections.emptyList();
        }
        
        // Lấy danh sách giao ca chờ xác nhận cho nhân viên này
        List<GiaoCa> pendingGiaoCa = giaoCaRepository.findPendingGiaoCaForNhanVienId(nhanVienId);
        System.out.println("Found pending giao ca: " + pendingGiaoCa.size());
        
        // Debug chi tiết giao ca
        for (GiaoCa giaoCa : pendingGiaoCa) {
            System.out.println("  - GiaoCa ID: " + giaoCa.getId() + 
                             ", Ma: " + giaoCa.getMaGiaoCa() + 
                             ", From: " + giaoCa.getNhanVienGiaoId() + 
                             ", To: " + giaoCa.getNhanVienNhanId() + 
                             ", Status: " + giaoCa.getTrangThai());
        }
        
        List<GiaoCaDTO> result = pendingGiaoCa.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        
        System.out.println("Returning " + result.size() + " giao ca DTOs");
        return result;
    }

    @Override
    public List<GiaoCaDTO> getByNhanVienIdAndNgayLamViec(Integer nhanVienId, java.time.LocalDate ngayLamViec) {
        return giaoCaRepository.findByNhanVienIdAndNgayLamViec(nhanVienId, ngayLamViec).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GiaoCaDTO createGiaoCa(Integer phanCaId, Integer nhanVienGiaoId, BigDecimal soTienDauCa, String ghiChu) {
        // Check if phanCa exists
        PhanCa phanCa = phanCaRepository.findById(phanCaId)
                .orElseThrow(() -> new RuntimeException("Phân ca không tồn tại"));

        // Kiểm tra quyền hạn: chỉ nhân viên trong ca mới được tạo giao ca
        if (!phanCa.getNhanVienId().equals(nhanVienGiaoId)) {
            throw new RuntimeException("Bạn không có quyền tạo giao ca cho phân ca này");
        }

        if (phanCa.getTrangThai() != 1) {
            throw new RuntimeException("Phân ca không ở trạng thái đang làm");
        }
        
        // Kiểm tra xem nhân viên có đang trong ca không (trạng thái = 1)
        if (phanCa.getTrangThai() != 1) {
            throw new RuntimeException("Bạn không đang trong ca làm việc, không thể tạo giao ca");
        }

        // Check if there's already a giao ca for this phanCa
        Optional<GiaoCa> existingGiaoCa = giaoCaRepository.findByPhanCaIdAndTrangThai(phanCaId, 0);
        if (existingGiaoCa.isPresent()) {
            throw new RuntimeException("Đã tồn tại giao ca chưa xác nhận cho phân ca này");
        }

        GiaoCaDTO giaoCaDTO = GiaoCaDTO.builder()
                .phanCaId(phanCaId)
                .nhanVienGiaoId(nhanVienGiaoId)
                .ngayGiaoCa(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .soTienDauCa(soTienDauCa != null ? soTienDauCa : BigDecimal.ZERO)
                .ghiChu(ghiChu)
                .trangThai(0) // Chờ xác nhận
                .build();

        return save(giaoCaDTO);
    }

    @Override
    @Transactional
    public GiaoCaDTO confirmGiaoCa(Integer giaoCaId, Integer nhanVienNhanId, GiaoCaDTO giaoCaDTO) {
        GiaoCa existingGiaoCa = giaoCaRepository.findById(giaoCaId)
                .orElseThrow(() -> new RuntimeException("Giao ca không tồn tại"));

        if (existingGiaoCa.getTrangThai() != 0) {
            throw new RuntimeException("Giao ca không ở trạng thái chờ xác nhận");
        }

        // Kiểm tra quyền hạn: chỉ nhân viên trong ca mới được xác nhận giao ca
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<PhanCa> todayShifts = phanCaRepository.findByNhanVienIdAndNgayLamViec(nhanVienNhanId, today);
        if (todayShifts.isEmpty()) {
            throw new RuntimeException("Bạn không có ca làm việc hôm nay, không thể xác nhận giao ca");
        }
        
        // Kiểm tra xem nhân viên có đang trong ca không (trạng thái = 1)
        boolean isCurrentlyWorking = todayShifts.stream()
                .anyMatch(shift -> shift.getTrangThai() == 1);
        if (!isCurrentlyWorking) {
            throw new RuntimeException("Bạn không đang trong ca làm việc, không thể xác nhận giao ca");
        }

        // Update fields
        existingGiaoCa.setNhanVienNhanId(nhanVienNhanId);
        existingGiaoCa.setSoTienCuoiCa(giaoCaDTO.getSoTienCuoiCa());
        existingGiaoCa.setSoTienThuThem(giaoCaDTO.getSoTienThuThem());
        existingGiaoCa.setSoTienChiRa(giaoCaDTO.getSoTienChiRa());
        existingGiaoCa.setTongDoanhThu(giaoCaDTO.getTongDoanhThu());
        existingGiaoCa.setSoDonHang(giaoCaDTO.getSoDonHang());
        existingGiaoCa.setSoDonHangThanhToanTienMat(giaoCaDTO.getSoDonHangThanhToanTienMat());
        existingGiaoCa.setSoDonHangThanhToanChuyenKhoan(giaoCaDTO.getSoDonHangThanhToanChuyenKhoan());
        existingGiaoCa.setBaoCaoCongViec(giaoCaDTO.getBaoCaoCongViec());
        existingGiaoCa.setSuCoBatThuong(giaoCaDTO.getSuCoBatThuong());
        existingGiaoCa.setCongViecTonDong(giaoCaDTO.getCongViecTonDong());
        existingGiaoCa.setGhiChu(giaoCaDTO.getGhiChu());
        existingGiaoCa.setTrangThai(1); // Đã xác nhận
        existingGiaoCa.setThoiGianXacNhan(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        
        // Lấy tên nhân viên xác nhận
        String nguoiXacNhan = "NHAN_VIEN_" + nhanVienNhanId;
        try {
            Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(nhanVienNhanId);
            if (nhanVienOpt.isPresent()) {
                nguoiXacNhan = nhanVienOpt.get().getHoTen();
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thông tin nhân viên xác nhận: " + e.getMessage());
        }
        existingGiaoCa.setNguoiCapNhat(nguoiXacNhan);

        GiaoCa updatedGiaoCa = giaoCaRepository.save(existingGiaoCa);
        
        // Cập nhật trạng thái phân ca cho nhân viên nhận ca
        for (PhanCa phanCa : todayShifts) {
            phanCa.setTrangThai(1); // Đang làm
            phanCa.setNguoiCapNhat("SYSTEM");
            phanCa.setNgayCapNhat(LocalDateTime.now());
            phanCaRepository.save(phanCa);
        }
        
        // Cập nhật trạng thái phân ca cho nhân viên giao ca (kết thúc ca)
        Integer nhanVienGiaoId = existingGiaoCa.getNhanVienGiaoId();
        if (nhanVienGiaoId != null) {
            List<PhanCa> giaoCaShifts = phanCaRepository.findByNhanVienIdAndNgayLamViec(nhanVienGiaoId, today);
            for (PhanCa phanCa : giaoCaShifts) {
                phanCa.setTrangThai(2); // Đã kết thúc
                phanCa.setNguoiCapNhat("SYSTEM");
                phanCa.setNgayCapNhat(LocalDateTime.now());
                phanCaRepository.save(phanCa);
            }
        }
        
        // Log action
        lichSuGiaoCaService.logAction(updatedGiaoCa.getId(), "XAC_NHAN", 
                nhanVienNhanId, "Xác nhận giao ca thành công");

        return convertToDto(updatedGiaoCa);
    }

    @Override
    @Transactional
    public GiaoCaDTO cancelGiaoCa(Integer giaoCaId, String lyDo) {
        GiaoCa existingGiaoCa = giaoCaRepository.findById(giaoCaId)
                .orElseThrow(() -> new RuntimeException("Giao ca không tồn tại"));

        existingGiaoCa.setTrangThai(2); // Đã hủy
        existingGiaoCa.setGhiChu(lyDo);
        existingGiaoCa.setNguoiCapNhat("SYSTEM");

        GiaoCa updatedGiaoCa = giaoCaRepository.save(existingGiaoCa);
        
        // Log action
        lichSuGiaoCaService.logAction(updatedGiaoCa.getId(), "HUY", 
                updatedGiaoCa.getNhanVienGiaoId(), "Hủy giao ca: " + lyDo);

        return convertToDto(updatedGiaoCa);
    }

    @Override
    public BigDecimal getTotalRevenueByNhanVienIdAndDateRange(Integer nhanVienId, LocalDateTime startDate, LocalDateTime endDate) {
        return giaoCaRepository.getTotalRevenueByNhanVienIdAndDateRange(nhanVienId, startDate, endDate);
    }

    @Override
    public BigDecimal getTotalRevenueByCaIdAndDateRange(Integer caId, LocalDateTime startDate, LocalDateTime endDate) {
        return giaoCaRepository.getTotalRevenueByCaIdAndDateRange(caId, startDate, endDate);
    }

    @Override
    public Long countGiaoCaByNhanVienIdAndDateRange(Integer nhanVienId, LocalDateTime startDate, LocalDateTime endDate) {
        return giaoCaRepository.countGiaoCaByNhanVienIdAndDateRange(nhanVienId, startDate, endDate);
    }

    @Override
    public Long countGiaoCaByCaIdAndDateRange(Integer caId, LocalDateTime startDate, LocalDateTime endDate) {
        return giaoCaRepository.countGiaoCaByCaIdAndDateRange(caId, startDate, endDate);
    }

    @Override
    public boolean existsByMaGiaoCa(String maGiaoCa) {
        return giaoCaRepository.existsByMaGiaoCa(maGiaoCa);
    }

    private GiaoCaDTO convertToDto(GiaoCa giaoCa) {
        GiaoCaDTO.GiaoCaDTOBuilder builder = GiaoCaDTO.builder()
                .id(giaoCa.getId())
                .maGiaoCa(giaoCa.getMaGiaoCa())
                .phanCaId(giaoCa.getPhanCaId())
                .nhanVienGiaoId(giaoCa.getNhanVienGiaoId())
                .nhanVienNhanId(giaoCa.getNhanVienNhanId())
                .ngayGiaoCa(giaoCa.getNgayGiaoCa())
                .soTienDauCa(giaoCa.getSoTienDauCa())
                .soTienCuoiCa(giaoCa.getSoTienCuoiCa())
                .soTienThuThem(giaoCa.getSoTienThuThem())
                .soTienChiRa(giaoCa.getSoTienChiRa())
                .chenhLech(giaoCa.getChenhLech())
                .tongDoanhThu(giaoCa.getTongDoanhThu())
                .soDonHang(giaoCa.getSoDonHang())
                .soDonHangThanhToanTienMat(giaoCa.getSoDonHangThanhToanTienMat())
                .soDonHangThanhToanChuyenKhoan(giaoCa.getSoDonHangThanhToanChuyenKhoan())
                .trangThai(giaoCa.getTrangThai())
                .trangThaiText(getTrangThaiText(giaoCa.getTrangThai()))
                .thoiGianXacNhan(giaoCa.getThoiGianXacNhan())
                .ghiChu(giaoCa.getGhiChu())
                .baoCaoCongViec(giaoCa.getBaoCaoCongViec())
                .suCoBatThuong(giaoCa.getSuCoBatThuong())
                .congViecTonDong(giaoCa.getCongViecTonDong())
                .ngayTao(giaoCa.getNgayTao())
                .ngayCapNhat(giaoCa.getNgayCapNhat())
                .nguoiTao(giaoCa.getNguoiTao())
                .nguoiCapNhat(giaoCa.getNguoiCapNhat());

        // Load thông tin nhân viên giao ca từ nhanVienGiaoId
        System.out.println("=== CONVERT TO DTO DEBUG ===");
        System.out.println("GiaoCa ID: " + giaoCa.getId());
        System.out.println("nhanVienGiaoId: " + giaoCa.getNhanVienGiaoId());
        System.out.println("nhanVienGiao: " + (giaoCa.getNhanVienGiao() != null ? giaoCa.getNhanVienGiao().getHoTen() : "NULL"));
        
        if (giaoCa.getNhanVienGiao() != null) {
            System.out.println("Using nhanVienGiao: " + giaoCa.getNhanVienGiao().getHoTen());
            builder.nhanVienGiao(GiaoCaDTO.NhanVienInfo.builder()
                    .id(giaoCa.getNhanVienGiao().getId())
                    .hoTen(giaoCa.getNhanVienGiao().getHoTen())
                    .maNhanVien(giaoCa.getNhanVienGiao().getMaNhanVien())
                    .email(giaoCa.getNhanVienGiao().getEmail())
                    .soDienThoai(giaoCa.getNhanVienGiao().getSoDienThoai())
                    .build());
        } else if (giaoCa.getNhanVienGiaoId() != null) {
            // Fallback: load from nhanVienGiaoId if nhanVienGiao is null
            try {
                Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(giaoCa.getNhanVienGiaoId());
                if (nhanVienOpt.isPresent()) {
                    NhanVien nhanVien = nhanVienOpt.get();
                    builder.nhanVienGiao(GiaoCaDTO.NhanVienInfo.builder()
                            .id(nhanVien.getId())
                            .hoTen(nhanVien.getHoTen())
                            .maNhanVien(nhanVien.getMaNhanVien())
                            .email(nhanVien.getEmail())
                            .soDienThoai(nhanVien.getSoDienThoai())
                            .build());
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi load thông tin nhân viên giao ca: " + e.getMessage());
            }
        }

        // Load thông tin nhân viên nhận ca
        if (giaoCa.getNhanVienNhan() != null) {
            builder.nhanVienNhan(GiaoCaDTO.NhanVienInfo.builder()
                    .id(giaoCa.getNhanVienNhan().getId())
                    .hoTen(giaoCa.getNhanVienNhan().getHoTen())
                    .maNhanVien(giaoCa.getNhanVienNhan().getMaNhanVien())
                    .email(giaoCa.getNhanVienNhan().getEmail())
                    .soDienThoai(giaoCa.getNhanVienNhan().getSoDienThoai())
                    .build());
        }

        return builder.build();
    }

    private GiaoCa convertToEntity(GiaoCaDTO giaoCaDTO) {
        return GiaoCa.builder()
                .id(giaoCaDTO.getId())
                .maGiaoCa(giaoCaDTO.getMaGiaoCa())
                .phanCaId(giaoCaDTO.getPhanCaId())
                .nhanVienGiaoId(giaoCaDTO.getNhanVienGiaoId())
                .nhanVienNhanId(giaoCaDTO.getNhanVienNhanId())
                .ngayGiaoCa(giaoCaDTO.getNgayGiaoCa())
                .soTienDauCa(giaoCaDTO.getSoTienDauCa())
                .soTienCuoiCa(giaoCaDTO.getSoTienCuoiCa())
                .soTienThuThem(giaoCaDTO.getSoTienThuThem())
                .soTienChiRa(giaoCaDTO.getSoTienChiRa())
                .tongDoanhThu(giaoCaDTO.getTongDoanhThu())
                .soDonHang(giaoCaDTO.getSoDonHang())
                .soDonHangThanhToanTienMat(giaoCaDTO.getSoDonHangThanhToanTienMat())
                .soDonHangThanhToanChuyenKhoan(giaoCaDTO.getSoDonHangThanhToanChuyenKhoan())
                .trangThai(giaoCaDTO.getTrangThai())
                .ghiChu(giaoCaDTO.getGhiChu())
                .baoCaoCongViec(giaoCaDTO.getBaoCaoCongViec())
                .suCoBatThuong(giaoCaDTO.getSuCoBatThuong())
                .congViecTonDong(giaoCaDTO.getCongViecTonDong())
                .nguoiTao(giaoCaDTO.getNguoiTao())
                .nguoiCapNhat(giaoCaDTO.getNguoiCapNhat())
                .build();
    }

    private String getTrangThaiText(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        return switch (trangThai) {
            case 0 -> "Chờ xác nhận";
            case 1 -> "Đã xác nhận";
            case 2 -> "Đã hủy";
            default -> "Không xác định";
        };
    }

    @Override
    public List<GiaoCaDTO> getRecentGiaoCa(int limit) {
        System.out.println("=== GET RECENT GIAO CA DEBUG ===");
        System.out.println("Requested limit: " + limit);
        
        List<GiaoCa> giaoCaList = giaoCaRepository.findTop10ByOrderByNgayGiaoCaDesc();
        System.out.println("Found " + giaoCaList.size() + " giao ca records from database");
        
        for (GiaoCa gc : giaoCaList) {
            System.out.println("  - GiaoCa ID: " + gc.getId() + 
                             ", Ma: " + gc.getMaGiaoCa() + 
                             ", Status: " + gc.getTrangThai() + 
                             ", Date: " + gc.getNgayGiaoCa());
        }
        
        List<GiaoCaDTO> result = giaoCaList.stream()
                .map(this::convertToDto)
                .limit(limit)
                .collect(Collectors.toList());
        
        System.out.println("Returning " + result.size() + " giao ca DTOs");
        return result;
    }
}
