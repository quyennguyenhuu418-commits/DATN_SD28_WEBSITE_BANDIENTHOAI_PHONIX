package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.HoaDonDTO;
import com.example.datn_sd28_2025.dto.PosOrderRequest;
import com.example.datn_sd28_2025.dto.ChiTietHoaDonDTO;
import com.example.datn_sd28_2025.dto.HoaDonCtDTO;
import com.example.datn_sd28_2025.entity.HoaDon;
import com.example.datn_sd28_2025.entity.HoaDonCt;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.repository.HoaDonRepository;
import com.example.datn_sd28_2025.repository.HoaDonCtRepository;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.service.HoaDonService;
import com.example.datn_sd28_2025.service.ImeiDaBanService;
import com.example.datn_sd28_2025.util.OrderStatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    @Autowired
    private HoaDonCtRepository hoaDonCtRepository;
    
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;
    
    @Autowired
    private ImeiDaBanService imeiDaBanService;

    @Override
    public List<HoaDonDTO> getAll() {
        return hoaDonRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public HoaDonDTO getById(Integer id) {
        return hoaDonRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public HoaDonDTO getByMaHoaDon(String maHoaDon) {
        return hoaDonRepository.findByMaHoaDon(maHoaDon)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public List<HoaDonDTO> getByKhachHangId(Integer khachHangId) {
        return hoaDonRepository.findByKhachHangId(khachHangId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<HoaDonDTO> getByTrangThai(Integer trangThai) {
        return hoaDonRepository.findByTrangThai(trangThai)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<HoaDonDTO> getByLoaiHoaDon(String loaiHoaDon) {
        return hoaDonRepository.findByLoaiHoaDon(loaiHoaDon)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<HoaDonDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return hoaDonRepository.findByNgayTaoBetween(startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public Page<HoaDonDTO> searchHoaDon(String keyword, Pageable pageable) {
        return hoaDonRepository.searchHoaDon(keyword, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public HoaDonDTO createOrder(PosOrderRequest request) {
        // Create the main invoice
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHangId(request.getHoaDon().getKhachHangId());
        hoaDon.setNhanVienId(request.getHoaDon().getNhanVienId());
        hoaDon.setPhieuGiamGiaId(request.getHoaDon().getPhieuGiamGiaId());
        hoaDon.setTongTien(request.getHoaDon().getTongTien());
        hoaDon.setTenKhachHang(request.getHoaDon().getTenKhachHang());
        hoaDon.setSoDienThoai(request.getHoaDon().getSoDienThoai());
        hoaDon.setDiaChi(request.getHoaDon().getDiaChi());
        hoaDon.setTongTienSauGiam(request.getHoaDon().getTongTienSauGiam());
        hoaDon.setNgayThanhToan(request.getHoaDon().getNgayThanhToan());
        hoaDon.setLoaiHoaDon(request.getHoaDon().getLoaiHoaDon());
        
        // Set status based on order type
        hoaDon.setTrangThai(OrderStatusUtil.getInitialStatus(
            request.getHoaDon().getLoaiHoaDon()
        ));
        hoaDon.setGhiChu(request.getHoaDon().getGhiChu());
        hoaDon.setNguoiTao("admin"); // Default value
        
        // Save the invoice first to get the ID
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
        
        // Create ChiTietHoaDon records
        if (request.getChiTietHoaDon() != null && !request.getChiTietHoaDon().isEmpty()) {
            for (ChiTietHoaDonDTO chiTietDTO : request.getChiTietHoaDon()) {
                HoaDonCt hoaDonCt = new HoaDonCt();
                hoaDonCt.setHoaDon(savedHoaDon);
                
                // Get ChiTietSanPham
                ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietDTO.getSanPhamId())
                        .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại: " + chiTietDTO.getSanPhamId()));
                hoaDonCt.setChiTietSanPham(chiTietSanPham);
                
                hoaDonCt.setDonGia(BigDecimal.valueOf(chiTietDTO.getDonGia()));
                hoaDonCt.setThanhTien(BigDecimal.valueOf(chiTietDTO.getThanhTien()));
                hoaDonCt.setTrangThai(1); // Active
                
                HoaDonCt savedHoaDonCt = hoaDonCtRepository.save(hoaDonCt);
                
                // Update stock quantity
                int newQuantity = chiTietSanPham.getSoLuong() - chiTietDTO.getSoLuong();
                if (newQuantity < 0) {
                    throw new RuntimeException("Không đủ hàng trong kho cho sản phẩm: " + chiTietSanPham.getSanPham().getTenSanPham());
                }
                chiTietSanPham.setSoLuong(newQuantity);
                chiTietSanPhamRepository.save(chiTietSanPham);
                
                // Handle IMEI sales if IMEIs are provided
                if (chiTietDTO.getSelectedImeis() != null && !chiTietDTO.getSelectedImeis().isEmpty()) {
                    try {
                        for (String imei : chiTietDTO.getSelectedImeis()) {
                            imeiDaBanService.markImeiAsSold(imei, savedHoaDonCt.getId());
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Lỗi khi xử lý IMEI: " + e.getMessage(), e);
                    }
                }
            }
        }
        
        return convertToDto(savedHoaDon);
    }

    @Override
    public HoaDonDTO updateHoaDon(Integer id, HoaDonDTO hoaDonDTO) {
        return hoaDonRepository.findById(id)
                .map(existingHoaDon -> {
                    existingHoaDon.setKhachHangId(hoaDonDTO.getKhachHangId());
                    existingHoaDon.setNhanVienId(hoaDonDTO.getNhanVienId());
                    existingHoaDon.setPhieuGiamGiaId(hoaDonDTO.getPhieuGiamGiaId());
                    existingHoaDon.setTongTien(hoaDonDTO.getTongTien());
                    existingHoaDon.setTenKhachHang(hoaDonDTO.getTenKhachHang());
                    existingHoaDon.setSoDienThoai(hoaDonDTO.getSoDienThoai());
                    existingHoaDon.setDiaChi(hoaDonDTO.getDiaChi());
                    existingHoaDon.setTongTienSauGiam(hoaDonDTO.getTongTienSauGiam());
                    existingHoaDon.setNgayThanhToan(hoaDonDTO.getNgayThanhToan());
                    existingHoaDon.setLoaiHoaDon(hoaDonDTO.getLoaiHoaDon());
                    existingHoaDon.setTrangThai(hoaDonDTO.getTrangThai());
                    existingHoaDon.setGhiChu(hoaDonDTO.getGhiChu());
                    existingHoaDon.setNguoiCapNhat("admin"); // Update user
                    existingHoaDon.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(hoaDonRepository.save(existingHoaDon));
                })
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại với id: " + id));
    }

    @Override
    public HoaDonDTO updateTrangThai(Integer id, Integer trangThai) {
        return hoaDonRepository.findById(id)
                .map(existingHoaDon -> {
                    existingHoaDon.setTrangThai(trangThai);
                    existingHoaDon.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(hoaDonRepository.save(existingHoaDon));
                })
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại với id: " + id));
    }

    @Override
    public void deleteHoaDon(Integer id) {
        if (!hoaDonRepository.existsById(id)) {
            throw new RuntimeException("Hóa đơn không tồn tại với id: " + id);
        }
        hoaDonRepository.deleteById(id);
    }

    @Override
    public Long countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return hoaDonRepository.countByNgayTaoBetween(startDate, endDate);
    }

    @Override
    public Double getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Double revenue = hoaDonRepository.getTotalRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : 0.0;
    }

    private HoaDonDTO convertToDto(HoaDon hoaDon) {
        HoaDonDTO dto = new HoaDonDTO();
        dto.setId(hoaDon.getId());
        dto.setMaHoaDon(hoaDon.getMaHoaDon());
        dto.setKhachHangId(hoaDon.getKhachHangId());
        dto.setNhanVienId(hoaDon.getNhanVienId());
        dto.setPhieuGiamGiaId(hoaDon.getPhieuGiamGiaId());
        dto.setTongTien(hoaDon.getTongTien());
        dto.setTenKhachHang(hoaDon.getTenKhachHang());
        dto.setSoDienThoai(hoaDon.getSoDienThoai());
        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setTongTienSauGiam(hoaDon.getTongTienSauGiam());
        dto.setNgayThanhToan(hoaDon.getNgayThanhToan());
        dto.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
//        dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        dto.setTrangThai(hoaDon.getTrangThai());
        dto.setGhiChu(hoaDon.getGhiChu());
        dto.setNgayTao(hoaDon.getNgayTao());
        dto.setNgayCapNhat(hoaDon.getNgayCapNhat());
        dto.setNguoiTao(hoaDon.getNguoiTao());
        dto.setNguoiCapNhat(hoaDon.getNguoiCapNhat());
        return dto;
    }
}
