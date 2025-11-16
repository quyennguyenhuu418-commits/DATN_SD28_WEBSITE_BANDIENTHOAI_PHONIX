package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.GiaoCaDTO;
import com.example.datn_sd28_2025.entity.Ca;
import com.example.datn_sd28_2025.entity.GiaoCa;
import com.example.datn_sd28_2025.entity.NhanVien;
import com.example.datn_sd28_2025.entity.PhanCa;
import com.example.datn_sd28_2025.repository.GiaoCaRepository;
import com.example.datn_sd28_2025.repository.HoaDonRepository;
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
    private HoaDonRepository hoaDonRepository;
    
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
        Optional<GiaoCa> giaoCaOpt = giaoCaRepository.findById(id);
        if (giaoCaOpt.isEmpty()) {
            return Optional.empty();
        }
        
        return Optional.of(convertToDto(giaoCaOpt.get()));
    }

    @Override
    @Transactional
    public GiaoCaDTO save(GiaoCaDTO giaoCaDTO) {
        // Check if maGiaoCa already exists
        if (giaoCaRepository.existsByMaGiaoCa(giaoCaDTO.getMaGiaoCa())) {
            throw new RuntimeException("Mã giao ca đã tồn tại trong hệ thống");
        }

        // Kiểm tra xem đã có giao ca cho phân ca này chưa (không cho tạo lại)
        if (giaoCaDTO.getPhanCaId() != null) {
            List<GiaoCa> existingGiaoCaList = giaoCaRepository.findByPhanCaId(giaoCaDTO.getPhanCaId());
            if (!existingGiaoCaList.isEmpty()) {
                throw new RuntimeException("Đã tồn tại giao ca cho phân ca này. Không thể tạo lại. Vui lòng sửa giao ca đã tạo.");
            }
        }

        // Tự động lấy số tiền đầu ca nếu chưa có (null hoặc 0)
        if (giaoCaDTO.getSoTienDauCa() == null || giaoCaDTO.getSoTienDauCa().compareTo(BigDecimal.ZERO) == 0) {
            if (giaoCaDTO.getPhanCaId() != null) {
                try {
                    PhanCa phanCa = phanCaRepository.findById(giaoCaDTO.getPhanCaId())
                            .orElse(null);
                    
                    if (phanCa != null) {
                        // Tìm giao ca trước đã xác nhận cho cùng ca này hoặc ca trước đó
                        List<GiaoCa> lastGiaoCaList = giaoCaRepository.findLastConfirmedGiaoCaByCaIdAndDate(
                                phanCa.getCaId(), phanCa.getNgayLamViec());
                        
                        if (!lastGiaoCaList.isEmpty()) {
                            GiaoCa lastGiaoCa = lastGiaoCaList.get(0);
                            if (lastGiaoCa.getSoTienCuoiCa() != null) {
                                giaoCaDTO.setSoTienDauCa(lastGiaoCa.getSoTienCuoiCa());
                            }
                        } else {
                            // Nếu không có giao ca trước, thử tìm theo phanCaId
                            Optional<GiaoCa> lastGiaoCaByPhanCa = giaoCaRepository.findLastConfirmedGiaoCaByPhanCaId(giaoCaDTO.getPhanCaId());
                            if (lastGiaoCaByPhanCa.isPresent() && lastGiaoCaByPhanCa.get().getSoTienCuoiCa() != null) {
                                giaoCaDTO.setSoTienDauCa(lastGiaoCaByPhanCa.get().getSoTienCuoiCa());
                            }
                        }
                    }
                } catch (Exception e) {
                    // Nếu lỗi thì để mặc định là 0
                    if (giaoCaDTO.getSoTienDauCa() == null) {
                        giaoCaDTO.setSoTienDauCa(BigDecimal.ZERO);
                    }
                }
            } else {
                // Nếu không có phanCaId thì mặc định là 0
                giaoCaDTO.setSoTienDauCa(BigDecimal.ZERO);
            }
        }

        GiaoCa giaoCa = convertToEntity(giaoCaDTO);
        
        // Tự động set nguoiTao từ SecurityContext (nhân viên đang đăng nhập) - luôn ghi đè
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            giaoCa.setNguoiTao(currentUsername);
            giaoCa.setNguoiCapNhat(currentUsername);
        }
        
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

        // Update fields (chỉ update các field được gửi từ frontend, không update các field readonly)
        if (giaoCaDTO.getNhanVienNhanId() != null) {
        existingGiaoCa.setNhanVienNhanId(giaoCaDTO.getNhanVienNhanId());
        }
        if (giaoCaDTO.getSoTienCuoiCa() != null) {
        existingGiaoCa.setSoTienCuoiCa(giaoCaDTO.getSoTienCuoiCa());
        }
        if (giaoCaDTO.getSoTienThuThem() != null) {
        existingGiaoCa.setSoTienThuThem(giaoCaDTO.getSoTienThuThem());
        }
        if (giaoCaDTO.getSoTienChiRa() != null) {
        existingGiaoCa.setSoTienChiRa(giaoCaDTO.getSoTienChiRa());
        }
        if (giaoCaDTO.getTongDoanhThu() != null) {
        existingGiaoCa.setTongDoanhThu(giaoCaDTO.getTongDoanhThu());
        }
        if (giaoCaDTO.getSoDonHang() != null) {
        existingGiaoCa.setSoDonHang(giaoCaDTO.getSoDonHang());
        }
        if (giaoCaDTO.getSoDonHangThanhToanTienMat() != null) {
        existingGiaoCa.setSoDonHangThanhToanTienMat(giaoCaDTO.getSoDonHangThanhToanTienMat());
        }
        if (giaoCaDTO.getSoDonHangThanhToanChuyenKhoan() != null) {
        existingGiaoCa.setSoDonHangThanhToanChuyenKhoan(giaoCaDTO.getSoDonHangThanhToanChuyenKhoan());
        }
        // Cập nhật chênh lệch nếu có thay đổi tiền
        if (giaoCaDTO.getSoTienCuoiCa() != null || giaoCaDTO.getSoTienThuThem() != null || giaoCaDTO.getSoTienChiRa() != null) {
            BigDecimal dauCa = existingGiaoCa.getSoTienDauCa() != null ? existingGiaoCa.getSoTienDauCa() : BigDecimal.ZERO;
            BigDecimal cuoiCa = existingGiaoCa.getSoTienCuoiCa() != null ? existingGiaoCa.getSoTienCuoiCa() : BigDecimal.ZERO;
            BigDecimal thuThem = existingGiaoCa.getSoTienThuThem() != null ? existingGiaoCa.getSoTienThuThem() : BigDecimal.ZERO;
            BigDecimal chiRa = existingGiaoCa.getSoTienChiRa() != null ? existingGiaoCa.getSoTienChiRa() : BigDecimal.ZERO;
            BigDecimal chenhLech = cuoiCa.subtract(dauCa).subtract(thuThem).add(chiRa);
            existingGiaoCa.setChenhLech(chenhLech);
        }
        if (giaoCaDTO.getBaoCaoCongViec() != null) {
        existingGiaoCa.setBaoCaoCongViec(giaoCaDTO.getBaoCaoCongViec());
        }
        if (giaoCaDTO.getSuCoBatThuong() != null) {
        existingGiaoCa.setSuCoBatThuong(giaoCaDTO.getSuCoBatThuong());
        }
        if (giaoCaDTO.getCongViecTonDong() != null) {
        existingGiaoCa.setCongViecTonDong(giaoCaDTO.getCongViecTonDong());
        }
        if (giaoCaDTO.getGhiChu() != null) {
        existingGiaoCa.setGhiChu(giaoCaDTO.getGhiChu());
        }
        
        // Tự động set nguoiCapNhat từ SecurityContext (nhân viên đang đăng nhập) - luôn ghi đè
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            existingGiaoCa.setNguoiCapNhat(currentUsername);
        }

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
        try {
            // Kiểm tra xem nhân viên có đang trong ca không
            // Sử dụng múi giờ Việt Nam
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            
            // Tìm ca làm việc hôm nay - sử dụng cả ID và tìm kiếm rộng hơn
            List<PhanCa> todayShifts = phanCaRepository.findByNhanVienIdAndNgayLamViec(nhanVienId, today);
            
            // Nếu không tìm thấy ca theo ID, thử tìm theo tên nhân viên
            if (todayShifts.isEmpty()) {
                // Lấy thông tin nhân viên để tìm theo tên
                Optional<NhanVien> nhanVienOpt = nhanVienRepository.findById(nhanVienId);
                if (nhanVienOpt.isPresent()) {
                    String hoTen = nhanVienOpt.get().getHoTen();
                    
                    // Tìm tất cả ca hôm nay và lọc theo tên
                    List<PhanCa> allTodayShifts = phanCaRepository.findByNgayLamViec(today);
                    todayShifts = allTodayShifts.stream()
                        .filter(shift -> shift.getNhanVien() != null && 
                               shift.getNhanVien().getHoTen().equals(hoTen))
                        .collect(Collectors.toList());
                }
            }
            
            // Chỉ trả về giao ca nếu nhân viên có ca hôm nay
            if (todayShifts.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Lấy danh sách giao ca chờ xác nhận cho nhân viên này
            // Tìm giao ca mà nhân viên này là người nhận (nhanVienNhanId)
            List<GiaoCa> pendingGiaoCa = giaoCaRepository.findPendingGiaoCaForNhanVienId(nhanVienId);
            
            // Nếu không tìm thấy, thử tìm giao ca mà nhân viên này là người giao (nhanVienGiaoId)
            if (pendingGiaoCa.isEmpty()) {
                pendingGiaoCa = giaoCaRepository.findPendingGiaoCaByNhanVienId(nhanVienId);
            }
            
            List<GiaoCaDTO> result = pendingGiaoCa.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            return result;
        } catch (Exception e) {
            System.err.println("Error in getPendingGiaoCaForNhanVienId: " + e.getMessage());
            e.printStackTrace();
            // Return empty list instead of throwing exception
            return Collections.emptyList();
        }
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

        // OPTION 1: Giao ca chỉ được tạo khi ca ĐÃ KẾT THÚC (status = 2)
        // Logic: Kết thúc ca trước → Giao ca sau
        if (phanCa.getTrangThai() != 2) {
            throw new RuntimeException("Phân ca phải ở trạng thái đã kết thúc mới được tạo giao ca. Vui lòng kết thúc ca trước.");
        }
        
        // Kiểm tra xem ca đã kết thúc chưa
        if (phanCa.getGioKetThucThucTe() == null) {
            throw new RuntimeException("Ca chưa có giờ kết thúc thực tế, không thể tạo giao ca");
        }

        // Chỉ cho phép tạo giao ca 1 lần cho mỗi phân ca
        // Nếu đã có giao ca (dù đã xác nhận hay chưa), chỉ cho phép edit, không cho tạo mới
        List<GiaoCa> existingGiaoCaList = giaoCaRepository.findByPhanCaId(phanCaId);
        if (!existingGiaoCaList.isEmpty()) {
            throw new RuntimeException("Đã tồn tại giao ca cho phân ca này. Chỉ cho phép sửa giao ca đã tạo, không thể tạo mới.");
        }

        // Tự động lấy tiền đầu ca từ giao ca trước (nếu có)
        BigDecimal tienDauCa = soTienDauCa != null ? soTienDauCa : BigDecimal.ZERO;
        
        // Tìm giao ca trước đã xác nhận cho cùng ca này hoặc ca trước đó
        List<GiaoCa> lastGiaoCaList = giaoCaRepository.findLastConfirmedGiaoCaByCaIdAndDate(
                phanCa.getCaId(), phanCa.getNgayLamViec());
        
        if (!lastGiaoCaList.isEmpty()) {
            GiaoCa lastGiaoCa = lastGiaoCaList.get(0);
            if (lastGiaoCa.getSoTienCuoiCa() != null) {
                tienDauCa = lastGiaoCa.getSoTienCuoiCa();
            }
        } else {
            // Nếu không có giao ca trước, thử tìm theo phanCaId
            Optional<GiaoCa> lastGiaoCaByPhanCa = giaoCaRepository.findLastConfirmedGiaoCaByPhanCaId(phanCaId);
            if (lastGiaoCaByPhanCa.isPresent() && lastGiaoCaByPhanCa.get().getSoTienCuoiCa() != null) {
                tienDauCa = lastGiaoCaByPhanCa.get().getSoTienCuoiCa();
            }
        }
        
        // Nếu người dùng nhập thủ công thì ưu tiên giá trị nhập vào
        if (soTienDauCa != null && soTienDauCa.compareTo(BigDecimal.ZERO) > 0) {
            tienDauCa = soTienDauCa;
        }

        GiaoCaDTO giaoCaDTO = GiaoCaDTO.builder()
                .phanCaId(phanCaId)
                .nhanVienGiaoId(nhanVienGiaoId)
                .ngayGiaoCa(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .soTienDauCa(tienDauCa)
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

        // LOGIC MỚI: Nhân viên nhận ca có thể xác nhận giao ca khi đăng nhập
        // Sau khi xác nhận, nhân viên cần tự bắt đầu ca thông qua chức năng "Bắt đầu ca"
        // Không còn tự động bắt đầu ca nữa
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<PhanCa> todayShifts = phanCaRepository.findByNhanVienIdAndNgayLamViec(nhanVienNhanId, today);
        
        // Chỉ kiểm tra nhân viên có ca hôm nay (không cần kiểm tra status vì nhân viên sẽ tự bắt đầu ca sau)
        if (todayShifts.isEmpty()) {
            // Cho phép xác nhận nếu nhân viên có ca trong tương lai (không nhất thiết hôm nay)
            LocalDate endDate = today.plusDays(30);
            boolean hasAnyShift = false;
            for (LocalDate searchDate = today; searchDate.isBefore(endDate); searchDate = searchDate.plusDays(1)) {
                List<PhanCa> dayShifts = phanCaRepository.findByNhanVienIdAndNgayLamViec(nhanVienNhanId, searchDate);
                if (!dayShifts.isEmpty()) {
                    hasAnyShift = true;
                    break;
                }
            }
            if (!hasAnyShift) {
                throw new RuntimeException("Bạn không có ca làm việc nào trong 30 ngày tới, không thể xác nhận giao ca");
            }
        }

        // Lấy thông tin phân ca của giao ca
        PhanCa phanCaGiao = existingGiaoCa.getPhanCa();
        if (phanCaGiao == null) {
            phanCaGiao = phanCaRepository.findById(existingGiaoCa.getPhanCaId())
                    .orElseThrow(() -> new RuntimeException("Phân ca không tồn tại"));
        }

        // Tự động tính doanh thu từ HoaDon trong khoảng thời gian ca (nếu chưa có)
        LocalDateTime batDauCa = phanCaGiao.getGioBatDauThucTe() != null 
                ? phanCaGiao.getGioBatDauThucTe() 
                : phanCaGiao.getNgayLamViec().atStartOfDay().plusHours(phanCaGiao.getCa().getGioBatDau().getHour()).plusMinutes(phanCaGiao.getCa().getGioBatDau().getMinute());
        // OPTION 1: Sử dụng giờ kết thúc thực tế của ca đã kết thúc, không phải now()
        LocalDateTime ketThucCa = phanCaGiao.getGioKetThucThucTe() != null 
                ? phanCaGiao.getGioKetThucThucTe() 
                : LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        BigDecimal tongDoanhThu = giaoCaDTO.getTongDoanhThu();
        Integer soDonHang = giaoCaDTO.getSoDonHang();
        Integer soDonHangThanhToanTienMat = giaoCaDTO.getSoDonHangThanhToanTienMat();
        Integer soDonHangThanhToanChuyenKhoan = giaoCaDTO.getSoDonHangThanhToanChuyenKhoan();

        // Nếu chưa có doanh thu hoặc số đơn hàng, tự động tính từ HoaDon
        if (tongDoanhThu == null || tongDoanhThu.compareTo(BigDecimal.ZERO) == 0) {
            try {
                tongDoanhThu = hoaDonRepository.getTotalRevenueByTimeRange(batDauCa, ketThucCa);
            } catch (Exception e) {
                tongDoanhThu = BigDecimal.ZERO;
            }
        }

        if (soDonHang == null || soDonHang == 0) {
            try {
                soDonHang = hoaDonRepository.countOrdersByTimeRange(batDauCa, ketThucCa).intValue();
            } catch (Exception e) {
                soDonHang = 0;
            }
        }

        if (soDonHangThanhToanTienMat == null || soDonHangThanhToanTienMat == 0) {
            try {
                soDonHangThanhToanTienMat = hoaDonRepository.countCashOrdersByTimeRange(batDauCa, ketThucCa).intValue();
            } catch (Exception e) {
                soDonHangThanhToanTienMat = 0;
            }
        }

        if (soDonHangThanhToanChuyenKhoan == null || soDonHangThanhToanChuyenKhoan == 0) {
            try {
                soDonHangThanhToanChuyenKhoan = hoaDonRepository.countBankTransferOrdersByTimeRange(batDauCa, ketThucCa).intValue();
            } catch (Exception e) {
                soDonHangThanhToanChuyenKhoan = 0;
            }
        }

        // Validation: Kiểm tra tiền cuối ca không được âm (nếu có)
        if (giaoCaDTO.getSoTienCuoiCa() != null && giaoCaDTO.getSoTienCuoiCa().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Số tiền cuối ca không được âm");
        }

        // Update fields - chỉ cập nhật nếu có giá trị
        existingGiaoCa.setNhanVienNhanId(nhanVienNhanId);
        if (giaoCaDTO.getSoTienCuoiCa() != null) {
            existingGiaoCa.setSoTienCuoiCa(giaoCaDTO.getSoTienCuoiCa());
        }
        existingGiaoCa.setSoTienThuThem(giaoCaDTO.getSoTienThuThem() != null ? giaoCaDTO.getSoTienThuThem() : BigDecimal.ZERO);
        existingGiaoCa.setSoTienChiRa(giaoCaDTO.getSoTienChiRa() != null ? giaoCaDTO.getSoTienChiRa() : BigDecimal.ZERO);
        existingGiaoCa.setTongDoanhThu(tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO);
        existingGiaoCa.setSoDonHang(soDonHang != null ? soDonHang : 0);
        existingGiaoCa.setSoDonHangThanhToanTienMat(soDonHangThanhToanTienMat != null ? soDonHangThanhToanTienMat : 0);
        existingGiaoCa.setSoDonHangThanhToanChuyenKhoan(soDonHangThanhToanChuyenKhoan != null ? soDonHangThanhToanChuyenKhoan : 0);
        existingGiaoCa.setBaoCaoCongViec(giaoCaDTO.getBaoCaoCongViec());
        existingGiaoCa.setSuCoBatThuong(giaoCaDTO.getSuCoBatThuong());
        existingGiaoCa.setCongViecTonDong(giaoCaDTO.getCongViecTonDong());
        existingGiaoCa.setGhiChu(giaoCaDTO.getGhiChu());
        existingGiaoCa.setTrangThai(1); // Đã xác nhận
        existingGiaoCa.setThoiGianXacNhan(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        
        // Tự động set nguoiCapNhat từ SecurityContext (nhân viên đang đăng nhập) - luôn ghi đè
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            existingGiaoCa.setNguoiCapNhat(currentUsername);
        }

        GiaoCa updatedGiaoCa = giaoCaRepository.save(existingGiaoCa);
        
        String currentUser = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        
        // Phân ca giao đã kết thúc (status = 2) từ trước, không cần cập nhật lại
        // Chỉ cần đảm bảo trạng thái đúng (đã kết thúc)
        if (phanCaGiao.getTrangThai() != 2) {
            phanCaGiao.setTrangThai(2); // Đã kết thúc
            if (phanCaGiao.getGioKetThucThucTe() == null) {
                phanCaGiao.setGioKetThucThucTe(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            }
            phanCaGiao.setNguoiCapNhat(currentUser);
            phanCaGiao.setNgayCapNhat(LocalDateTime.now());
            phanCaRepository.save(phanCaGiao);
        }
        
        // Tự động bắt đầu ca tiếp theo của nhân viên nhận khi xác nhận giao ca
        // Tìm ca tiếp theo (chưa bắt đầu, status = 0) của nhân viên nhận
        // Ưu tiên tìm trong cùng ngày, nếu không có thì tìm từ ngày tiếp theo
        PhanCa phanCaNhanTiepTheo = null;
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        
        // Bước 1: Tìm trong cùng ngày hôm nay (ca chưa bắt đầu)
        phanCaNhanTiepTheo = todayShifts.stream()
                .filter(shift -> {
                    // Chỉ lấy ca chưa bắt đầu (trangThai = 0 hoặc null)
                    if (shift.getTrangThai() != null && shift.getTrangThai() != 0) {
                        return false;
                    }
                    // Đảm bảo ca có thông tin
                    return shift.getCa() != null;
                })
                .min((s1, s2) -> {
                    // Sắp xếp theo giờ bắt đầu, lấy ca sớm nhất
                    Ca c1 = s1.getCa();
                    Ca c2 = s2.getCa();
                    if (c1 != null && c2 != null) {
                        return c1.getGioBatDau().compareTo(c2.getGioBatDau());
                    }
                    return 0;
                })
                .orElse(null);
        
        // Bước 2: Nếu không tìm thấy trong cùng ngày, tìm từ ngày tiếp theo
        if (phanCaNhanTiepTheo == null) {
            LocalDate nextDay = today.plusDays(1);
            LocalDate endDate = nextDay.plusDays(30); // Tìm trong 30 ngày tới
            
            for (LocalDate searchDate = nextDay; searchDate.isBefore(endDate) && phanCaNhanTiepTheo == null; searchDate = searchDate.plusDays(1)) {
                List<PhanCa> dayShifts = phanCaRepository.findByNhanVienIdAndNgayLamViec(nhanVienNhanId, searchDate);
                
                phanCaNhanTiepTheo = dayShifts.stream()
                        .filter(shift -> {
                            // Chỉ lấy ca chưa bắt đầu (trangThai = 0 hoặc null)
                            if (shift.getTrangThai() != null && shift.getTrangThai() != 0) {
                                return false;
                            }
                            return shift.getCa() != null;
                        })
                        .min((s1, s2) -> {
                            // Sắp xếp theo ngày rồi giờ bắt đầu, lấy ca sớm nhất
                            int dateCompare = s1.getNgayLamViec().compareTo(s2.getNgayLamViec());
                            if (dateCompare != 0) return dateCompare;
                            Ca c1 = s1.getCa();
                            Ca c2 = s2.getCa();
                            if (c1 != null && c2 != null) {
                                return c1.getGioBatDau().compareTo(c2.getGioBatDau());
                            }
                            return 0;
                        })
                        .orElse(null);
                
                if (phanCaNhanTiepTheo != null) {
                    break;
                }
            }
        }
        
        // Bước 3: Tự động bắt đầu ca tiếp theo (nếu tìm thấy)
        if (phanCaNhanTiepTheo != null) {
            phanCaNhanTiepTheo.setTrangThai(1); // Đang làm
            phanCaNhanTiepTheo.setGioBatDauThucTe(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            phanCaNhanTiepTheo.setNguoiCapNhat(currentUser);
            phanCaNhanTiepTheo.setNgayCapNhat(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
            phanCaRepository.save(phanCaNhanTiepTheo);
            phanCaRepository.flush(); // Đảm bảo thay đổi được lưu ngay lập tức
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
        existingGiaoCa.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());

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
        if (giaoCa.getNhanVienGiao() != null) {
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
                // Ignore error
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
        List<GiaoCa> giaoCaList = giaoCaRepository.findTop10ByOrderByNgayGiaoCaDesc();
        
        List<GiaoCaDTO> result = giaoCaList.stream()
                .map(this::convertToDto)
                .limit(limit)
                .collect(Collectors.toList());
        
        return result;
    }

}
