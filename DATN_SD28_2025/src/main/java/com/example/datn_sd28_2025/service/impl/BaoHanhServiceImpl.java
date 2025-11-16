package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.LichSuXuLyBaoHanhDTO;
import com.example.datn_sd28_2025.dto.PhieuBaoHanhDTO;
import com.example.datn_sd28_2025.entity.LichSuXuLyBaoHanh;
import com.example.datn_sd28_2025.entity.NhanVien;
import com.example.datn_sd28_2025.entity.PhieuBaoHanh;
import com.example.datn_sd28_2025.repository.LichSuXuLyBaoHanhRepository;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import com.example.datn_sd28_2025.repository.PhieuBaoHanhRepository;
import com.example.datn_sd28_2025.service.BaoHanhService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BaoHanhServiceImpl implements BaoHanhService {

    @Autowired
    private PhieuBaoHanhRepository phieuBaoHanhRepository;

    @Autowired
    private LichSuXuLyBaoHanhRepository lichSuXuLyBaoHanhRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Override
    public List<PhieuBaoHanhDTO> getAll() {
        return phieuBaoHanhRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<PhieuBaoHanhDTO> getById(Integer id) {
        return phieuBaoHanhRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO create(PhieuBaoHanhDTO phieuBaoHanhDTO) {
        PhieuBaoHanh phieuBaoHanh = convertToEntity(phieuBaoHanhDTO);
        
        // Set thông tin mặc định
        phieuBaoHanh.setTrangThai(0); // Mới tiếp nhận
        phieuBaoHanh.setNgayNhan(LocalDate.now());
        
        // Set nhân viên tiếp nhận từ SecurityContext
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            Optional<NhanVien> nhanVien = nhanVienRepository.findByTaiKhoan(currentUsername);
            if (nhanVien.isPresent()) {
                phieuBaoHanh.setNhanVienTiepNhanId(nhanVien.get().getId());
                phieuBaoHanh.setNguoiTao(currentUsername);
                phieuBaoHanh.setNguoiCapNhat(currentUsername);
            }
        }
        
        PhieuBaoHanh saved = phieuBaoHanhRepository.save(phieuBaoHanh);
        
        // Tạo lịch sử xử lý
        createLichSuXuLy(saved.getId(), "TIEP_NHAN", "Tiếp nhận yêu cầu bảo hành", null, null, 0);
        
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO tiepNhanYeuCau(PhieuBaoHanhDTO phieuBaoHanhDTO) {
        return create(phieuBaoHanhDTO);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO kiemTraDieuKienBaoHanh(Integer id, Boolean duDieuKien, String lyDo) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phiếu bảo hành không tồn tại với id: " + id));
        
        phieuBaoHanh.setDuDieuKienBaoHanh(duDieuKien);
        
        if (duDieuKien) {
            phieuBaoHanh.setTrangThai(1); // Đủ điều kiện bảo hành
            createLichSuXuLy(id, "DU_DIEU_KIEN", "Sản phẩm đủ điều kiện bảo hành", null, 0, 1);
        } else {
            phieuBaoHanh.setTrangThai(2); // Không đủ điều kiện bảo hành
            phieuBaoHanh.setLyDoKhongDuDieuKien(lyDo);
            createLichSuXuLy(id, "KHONG_DU_DIEU_KIEN", "Sản phẩm không đủ điều kiện bảo hành: " + lyDo, null, 0, 2);
        }
        
        PhieuBaoHanh updated = phieuBaoHanhRepository.save(phieuBaoHanh);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO danhGiaDieuKien(Integer id, Boolean duDieuKien, String lyDoKhongDu) {
        return kiemTraDieuKienBaoHanh(id, duDieuKien, lyDoKhongDu);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO suaNoiBo(Integer id, String noiDungSuaChua, String ghiChu, Integer nhanVienKyThuatId) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phiếu bảo hành không tồn tại với id: " + id));
        
        phieuBaoHanh.setHuongXuLy("SUA_TAI_CUA_HANG");
        phieuBaoHanh.setTrangThai(3); // Đang sửa chữa nội bộ
        phieuBaoHanh.setNoiDungSuaChua(noiDungSuaChua);
        phieuBaoHanh.setGhiChuKyThuatVien(ghiChu);
        phieuBaoHanh.setNhanVienKyThuatId(nhanVienKyThuatId);
        
        createLichSuXuLy(id, "SUA_NOI_BO", "Sửa chữa nội bộ: " + noiDungSuaChua, null, 1, 3);
        
        PhieuBaoHanh updated = phieuBaoHanhRepository.save(phieuBaoHanh);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO guiTTBH(Integer id, String ttbhHang, String maBaoHanhHang) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phiếu bảo hành không tồn tại với id: " + id));
        
        phieuBaoHanh.setHuongXuLy("GUI_TTBH_HANG");
        phieuBaoHanh.setTrangThai(4); // Đã gửi TTBH hãng
        phieuBaoHanh.setTtbhHang(ttbhHang);
        phieuBaoHanh.setMaBaoHanhHang(maBaoHanhHang);
        
        createLichSuXuLy(id, "GUI_TTBH", "Đã gửi đến " + ttbhHang + " với mã: " + maBaoHanhHang, null, 1, 4);
        
        PhieuBaoHanh updated = phieuBaoHanhRepository.save(phieuBaoHanh);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO nhanTuTTBH(Integer id, String noiDungSuaChua, String ghiChu) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phiếu bảo hành không tồn tại với id: " + id));
        
        phieuBaoHanh.setTrangThai(5); // Đã nhận từ TTBH
        phieuBaoHanh.setNoiDungSuaChua(noiDungSuaChua);
        phieuBaoHanh.setGhiChuKyThuatVien(ghiChu);
        
        createLichSuXuLy(id, "NHAN_TU_TTBH", "Đã nhận từ TTBH. Nội dung: " + noiDungSuaChua, null, 4, 5);
        
        PhieuBaoHanh updated = phieuBaoHanhRepository.save(phieuBaoHanh);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO kiemTraQC(Integer id, Boolean qcPass, String ghiChu) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phiếu bảo hành không tồn tại với id: " + id));
        
        phieuBaoHanh.setTrangThai(6); // Đang kiểm tra QC
        if (qcPass != null && qcPass) {
            phieuBaoHanh.setTrangThai(7); // Đã sửa xong
            createLichSuXuLy(id, "KIEM_TRA_QC", "Kiểm tra QC thành công: " + ghiChu, null, 6, 7);
        } else {
            createLichSuXuLy(id, "KIEM_TRA_QC", "Kiểm tra QC: " + ghiChu, null, 6, 6);
        }
        
        PhieuBaoHanh updated = phieuBaoHanhRepository.save(phieuBaoHanh);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO traMay(Integer id, String ghiChu) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phiếu bảo hành không tồn tại với id: " + id));
        
        phieuBaoHanh.setTrangThai(8); // Đã trả khách
        phieuBaoHanh.setNgayTraThucTe(LocalDate.now());
        
        // Set nhân viên trả máy từ SecurityContext
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            Optional<NhanVien> nhanVien = nhanVienRepository.findByTaiKhoan(currentUsername);
            if (nhanVien.isPresent()) {
                phieuBaoHanh.setNhanVienTraMayId(nhanVien.get().getId());
            }
        }
        
        createLichSuXuLy(id, "TRA_MAY", "Đã trả máy cho khách hàng: " + ghiChu, null, 7, 8);
        
        PhieuBaoHanh updated = phieuBaoHanhRepository.save(phieuBaoHanh);
        
        // Sau khi trả máy, cập nhật thành hoàn tất sau 1 ngày (hoặc có thể cập nhật thủ công)
        // Có thể thêm logic tự động hoặc để quản lý xác nhận
        
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO updateStatus(Integer id, Integer trangThai, String hanhDong, String noiDungXuLy) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phiếu bảo hành không tồn tại với id: " + id));
        
        Integer trangThaiTruoc = phieuBaoHanh.getTrangThai();
        phieuBaoHanh.setTrangThai(trangThai);
        
        createLichSuXuLy(id, hanhDong, noiDungXuLy, null, trangThaiTruoc, trangThai);
        
        PhieuBaoHanh updated = phieuBaoHanhRepository.save(phieuBaoHanh);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public PhieuBaoHanhDTO update(Integer id, PhieuBaoHanhDTO phieuBaoHanhDTO) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Phiếu bảo hành không tồn tại với id: " + id));
        
        // Cập nhật các field từ DTO
        phieuBaoHanh.setTenKhachHang(phieuBaoHanhDTO.getTenKhachHang());
        phieuBaoHanh.setSoDienThoai(phieuBaoHanhDTO.getSoDienThoai());
        phieuBaoHanh.setTenSanPham(phieuBaoHanhDTO.getTenSanPham());
        phieuBaoHanh.setImeiSerial(phieuBaoHanhDTO.getImeiSerial());
        phieuBaoHanh.setMoTaLoiKhachHang(phieuBaoHanhDTO.getMoTaLoiKhachHang());
        phieuBaoHanh.setMoTaLoiNhanVien(phieuBaoHanhDTO.getMoTaLoiNhanVien());
        phieuBaoHanh.setTinhTrangVatLy(phieuBaoHanhDTO.getTinhTrangVatLy());
        phieuBaoHanh.setPhuKienDiKem(phieuBaoHanhDTO.getPhuKienDiKem());
        phieuBaoHanh.setNgayHenTraDuKien(phieuBaoHanhDTO.getNgayHenTraDuKien());
        
        // Set nguoiCapNhat
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            phieuBaoHanh.setNguoiCapNhat(currentUsername);
        }
        
        PhieuBaoHanh updated = phieuBaoHanhRepository.save(phieuBaoHanh);
        return convertToDto(updated);
    }

    @Override
    public List<PhieuBaoHanhDTO> search(String maPhieu, String tenKhachHang, String soDienThoai, 
                                        String imeiSerial, Integer trangThai, 
                                        LocalDate ngayNhanTu, LocalDate ngayNhanDen) {
        List<PhieuBaoHanh> results = phieuBaoHanhRepository.search(
                maPhieu, tenKhachHang, soDienThoai, imeiSerial, trangThai, ngayNhanTu, ngayNhanDen
        );
        return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuXuLyBaoHanhDTO> getLichSuXuLy(Integer phieuBaoHanhId) {
        return lichSuXuLyBaoHanhRepository.findByPhieuBaoHanhId(phieuBaoHanhId).stream()
                .map(this::convertLichSuToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LichSuXuLyBaoHanhDTO addLichSuXuLy(Integer phieuBaoHanhId, LichSuXuLyBaoHanhDTO lichSuDTO) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(phieuBaoHanhId)
                .orElseThrow(() -> new RuntimeException("Phiếu bảo hành không tồn tại với id: " + phieuBaoHanhId));
        
        LichSuXuLyBaoHanh lichSu = convertLichSuToEntity(lichSuDTO);
        lichSu.setPhieuBaoHanh(phieuBaoHanh);
        lichSu.setThoiGian(LocalDateTime.now());
        
        LichSuXuLyBaoHanh saved = lichSuXuLyBaoHanhRepository.save(lichSu);
        return convertLichSuToDto(saved);
    }

    @Override
    public boolean validateImeiWithInvoice(String imei, Integer khachHangId) {
        // TODO: Implement validation logic - kiểm tra IMEI có trong hóa đơn của khách hàng không
        // Có thể query từ bảng imei_da_ban và hoa_don
        return true; // Placeholder
    }

    @Override
    public boolean checkWarrantyPeriod(Integer hoaDonId, Integer soNgayBaoHanh) {
        // TODO: Implement logic - kiểm tra xem còn trong thời hạn bảo hành không
        // Cần query hoa_don để lấy ngày mua và so sánh với ngày hiện tại
        return true; // Placeholder
    }

    @Override
    public String getTrangThaiText(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        switch (trangThai) {
            case 0: return "Mới tiếp nhận";
            case 1: return "Đủ điều kiện bảo hành";
            case 2: return "Không đủ điều kiện";
            case 3: return "Đang sửa chữa nội bộ";
            case 4: return "Đã gửi TTBH hãng";
            case 5: return "Đã nhận từ TTBH";
            case 6: return "Đang kiểm tra QC";
            case 7: return "Đã sửa xong";
            case 8: return "Đã trả khách";
            case 9: return "Hoàn tất";
            default: return "Không xác định";
        }
    }

    @Override
    public String getHanhDongText(String hanhDong) {
        if (hanhDong == null) return "Không xác định";
        switch (hanhDong) {
            case "TIEP_NHAN": return "Tiếp nhận";
            case "KIEM_TRA_DIEU_KIEN": return "Kiểm tra điều kiện";
            case "DU_DIEU_KIEN": return "Đủ điều kiện";
            case "KHONG_DU_DIEU_KIEN": return "Không đủ điều kiện";
            case "CHAN_DOAN_LOI": return "Chẩn đoán lỗi";
            case "SUA_NOI_BO": return "Sửa nội bộ";
            case "GUI_TTBH": return "Gửi TTBH hãng";
            case "NHAN_TU_TTBH": return "Nhận từ TTBH";
            case "KIEM_TRA_QC": return "Kiểm tra QC";
            case "TRA_MAY": return "Trả máy";
            case "HOAN_TAT": return "Hoàn tất";
            default: return hanhDong;
        }
    }

    @Override
    public Long countByTrangThai(Integer trangThai) {
        return (long) phieuBaoHanhRepository.findByTrangThai(trangThai).size();
    }

    @Override
    public List<PhieuBaoHanhDTO> findByTrangThai(Integer trangThai) {
        return phieuBaoHanhRepository.findByTrangThai(trangThai).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PhieuBaoHanhDTO> findByKhachHangId(Integer khachHangId) {
        return phieuBaoHanhRepository.findByKhachHangId(khachHangId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Helper methods
    private PhieuBaoHanhDTO convertToDto(PhieuBaoHanh phieuBaoHanh) {
        PhieuBaoHanhDTO dto = PhieuBaoHanhDTO.builder()
                .id(phieuBaoHanh.getId())
                .maPhieu(phieuBaoHanh.getMaPhieu())
                .khachHangId(phieuBaoHanh.getKhachHangId())
                .tenKhachHang(phieuBaoHanh.getTenKhachHang())
                .soDienThoai(phieuBaoHanh.getSoDienThoai())
                .sanPhamId(phieuBaoHanh.getSanPhamId())
                .chiTietSanPhamId(phieuBaoHanh.getChiTietSanPhamId())
                .hoaDonId(phieuBaoHanh.getHoaDonId())
                .tenSanPham(phieuBaoHanh.getTenSanPham())
                .imeiSerial(phieuBaoHanh.getImeiSerial())
                .moTaLoiKhachHang(phieuBaoHanh.getMoTaLoiKhachHang())
                .moTaLoiNhanVien(phieuBaoHanh.getMoTaLoiNhanVien())
                .tinhTrangVatLy(phieuBaoHanh.getTinhTrangVatLy())
                .phuKienDiKem(phieuBaoHanh.getPhuKienDiKem())
                .duDieuKienBaoHanh(phieuBaoHanh.getDuDieuKienBaoHanh())
                .lyDoKhongDuDieuKien(phieuBaoHanh.getLyDoKhongDuDieuKien())
                .huongXuLy(phieuBaoHanh.getHuongXuLy())
                .noiDungSuaChua(phieuBaoHanh.getNoiDungSuaChua())
                .ghiChuKyThuatVien(phieuBaoHanh.getGhiChuKyThuatVien())
                .chiPhiSuaChua(phieuBaoHanh.getChiPhiSuaChua())
                .khachDaThanhToan(phieuBaoHanh.getKhachDaThanhToan())
                .ttbhHang(phieuBaoHanh.getTtbhHang())
                .maBaoHanhHang(phieuBaoHanh.getMaBaoHanhHang())
                .nhanVienTiepNhanId(phieuBaoHanh.getNhanVienTiepNhanId())
                .nhanVienKyThuatId(phieuBaoHanh.getNhanVienKyThuatId())
                .nhanVienTraMayId(phieuBaoHanh.getNhanVienTraMayId())
                .ngayNhan(phieuBaoHanh.getNgayNhan())
                .ngayHenTraDuKien(phieuBaoHanh.getNgayHenTraDuKien())
                .ngayTraThucTe(phieuBaoHanh.getNgayTraThucTe())
                .trangThai(phieuBaoHanh.getTrangThai())
                .ngayTao(phieuBaoHanh.getNgayTao())
                .ngayCapNhat(phieuBaoHanh.getNgayCapNhat())
                .nguoiTao(phieuBaoHanh.getNguoiTao())
                .nguoiCapNhat(phieuBaoHanh.getNguoiCapNhat())
                .build();
        
        dto.setTrangThaiText(getTrangThaiText(phieuBaoHanh.getTrangThai()));
        
        // Load tên nhân viên nếu có ID
        if (phieuBaoHanh.getNhanVienTiepNhanId() != null) {
            nhanVienRepository.findById(phieuBaoHanh.getNhanVienTiepNhanId())
                    .ifPresent(nv -> dto.setTenNhanVienTiepNhan(nv.getHoTen()));
        }
        if (phieuBaoHanh.getNhanVienKyThuatId() != null) {
            nhanVienRepository.findById(phieuBaoHanh.getNhanVienKyThuatId())
                    .ifPresent(nv -> dto.setTenNhanVienKyThuat(nv.getHoTen()));
        }
        if (phieuBaoHanh.getNhanVienTraMayId() != null) {
            nhanVienRepository.findById(phieuBaoHanh.getNhanVienTraMayId())
                    .ifPresent(nv -> dto.setTenNhanVienTraMay(nv.getHoTen()));
        }
        
        // Load lịch sử xử lý
        if (phieuBaoHanh.getLichSuXuLyBaoHanhList() != null) {
            dto.setLichSuXuLyBaoHanhList(
                    phieuBaoHanh.getLichSuXuLyBaoHanhList().stream()
                            .map(this::convertLichSuToDto)
                            .collect(Collectors.toList())
            );
        }
        
        return dto;
    }

    private PhieuBaoHanh convertToEntity(PhieuBaoHanhDTO dto) {
        return PhieuBaoHanh.builder()
                .id(dto.getId())
                .maPhieu(dto.getMaPhieu())
                .khachHangId(dto.getKhachHangId())
                .tenKhachHang(dto.getTenKhachHang())
                .soDienThoai(dto.getSoDienThoai())
                .sanPhamId(dto.getSanPhamId())
                .chiTietSanPhamId(dto.getChiTietSanPhamId())
                .hoaDonId(dto.getHoaDonId())
                .tenSanPham(dto.getTenSanPham())
                .imeiSerial(dto.getImeiSerial())
                .moTaLoiKhachHang(dto.getMoTaLoiKhachHang())
                .moTaLoiNhanVien(dto.getMoTaLoiNhanVien())
                .tinhTrangVatLy(dto.getTinhTrangVatLy())
                .phuKienDiKem(dto.getPhuKienDiKem())
                .duDieuKienBaoHanh(dto.getDuDieuKienBaoHanh())
                .lyDoKhongDuDieuKien(dto.getLyDoKhongDuDieuKien())
                .huongXuLy(dto.getHuongXuLy())
                .noiDungSuaChua(dto.getNoiDungSuaChua())
                .ghiChuKyThuatVien(dto.getGhiChuKyThuatVien())
                .chiPhiSuaChua(dto.getChiPhiSuaChua())
                .khachDaThanhToan(dto.getKhachDaThanhToan())
                .ttbhHang(dto.getTtbhHang())
                .maBaoHanhHang(dto.getMaBaoHanhHang())
                .nhanVienTiepNhanId(dto.getNhanVienTiepNhanId())
                .nhanVienKyThuatId(dto.getNhanVienKyThuatId())
                .nhanVienTraMayId(dto.getNhanVienTraMayId())
                .ngayNhan(dto.getNgayNhan())
                .ngayHenTraDuKien(dto.getNgayHenTraDuKien())
                .ngayTraThucTe(dto.getNgayTraThucTe())
                .trangThai(dto.getTrangThai())
                .nguoiTao(dto.getNguoiTao())
                .nguoiCapNhat(dto.getNguoiCapNhat())
                .build();
    }

    private LichSuXuLyBaoHanhDTO convertLichSuToDto(LichSuXuLyBaoHanh lichSu) {
        LichSuXuLyBaoHanhDTO dto = LichSuXuLyBaoHanhDTO.builder()
                .id(lichSu.getId())
                .idPhieuBaoHanh(lichSu.getPhieuBaoHanh() != null ? lichSu.getPhieuBaoHanh().getId() : null)
                .thoiGian(lichSu.getThoiGian())
                .nhanVienThucHienId(lichSu.getNhanVienThucHienId())
                .tenNhanVienThucHien(lichSu.getTenNhanVienThucHien())
                .hanhDong(lichSu.getHanhDong())
                .noiDungXuLy(lichSu.getNoiDungXuLy())
                .chiPhiPhatSinh(lichSu.getChiPhiPhatSinh())
                .linhKienThayThe(lichSu.getLinhKienThayThe())
                .ghiChu(lichSu.getGhiChu())
                .trangThaiTruoc(lichSu.getTrangThaiTruoc())
                .trangThaiSau(lichSu.getTrangThaiSau())
                .ngayTao(lichSu.getNgayTao())
                .build();
        
        dto.setHanhDongText(getHanhDongText(lichSu.getHanhDong()));
        dto.setTrangThaiTruocText(getTrangThaiText(lichSu.getTrangThaiTruoc()));
        dto.setTrangThaiSauText(getTrangThaiText(lichSu.getTrangThaiSau()));
        
        return dto;
    }

    private LichSuXuLyBaoHanh convertLichSuToEntity(LichSuXuLyBaoHanhDTO dto) {
        return LichSuXuLyBaoHanh.builder()
                .id(dto.getId())
                .thoiGian(dto.getThoiGian())
                .nhanVienThucHienId(dto.getNhanVienThucHienId())
                .tenNhanVienThucHien(dto.getTenNhanVienThucHien())
                .hanhDong(dto.getHanhDong())
                .noiDungXuLy(dto.getNoiDungXuLy())
                .chiPhiPhatSinh(dto.getChiPhiPhatSinh())
                .linhKienThayThe(dto.getLinhKienThayThe())
                .ghiChu(dto.getGhiChu())
                .trangThaiTruoc(dto.getTrangThaiTruoc())
                .trangThaiSau(dto.getTrangThaiSau())
                .build();
    }

    private void createLichSuXuLy(Integer phieuBaoHanhId, String hanhDong, String noiDungXuLy, 
                                   String linhKienThayThe, Integer trangThaiTruoc, Integer trangThaiSau) {
        PhieuBaoHanh phieuBaoHanh = phieuBaoHanhRepository.findById(phieuBaoHanhId)
                .orElseThrow(() -> new RuntimeException("Phiếu bảo hành không tồn tại"));
        
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        Integer nhanVienId = null;
        String tenNhanVien = "Hệ thống";
        
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            Optional<NhanVien> nhanVien = nhanVienRepository.findByTaiKhoan(currentUsername);
            if (nhanVien.isPresent()) {
                nhanVienId = nhanVien.get().getId();
                tenNhanVien = nhanVien.get().getHoTen();
            }
        }
        
        LichSuXuLyBaoHanh lichSu = LichSuXuLyBaoHanh.builder()
                .phieuBaoHanh(phieuBaoHanh)
                .thoiGian(LocalDateTime.now())
                .nhanVienThucHienId(nhanVienId)
                .tenNhanVienThucHien(tenNhanVien)
                .hanhDong(hanhDong)
                .noiDungXuLy(noiDungXuLy)
                .linhKienThayThe(linhKienThayThe)
                .trangThaiTruoc(trangThaiTruoc)
                .trangThaiSau(trangThaiSau)
                .build();
        
        lichSuXuLyBaoHanhRepository.save(lichSu);
    }
}

