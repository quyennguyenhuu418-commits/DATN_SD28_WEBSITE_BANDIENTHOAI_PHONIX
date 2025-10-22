package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.SanPhamPosDTO;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.HinhAnh;
import com.example.datn_sd28_2025.entity.Imei;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.repository.HoaDonCtRepository;
import com.example.datn_sd28_2025.repository.HinhAnhRepository;
import com.example.datn_sd28_2025.repository.ImeiRepository;
import com.example.datn_sd28_2025.service.SanPhamPosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SanPhamPosServiceImpl implements SanPhamPosService {

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private HoaDonCtRepository hoaDonCtRepository;

    @Autowired
    private HinhAnhRepository hinhAnhRepository;
    
    @Autowired
    private ImeiRepository imeiRepository;

    @Override
    public List<SanPhamPosDTO> getAllProductsForPos() {
        System.out.println("Service: Getting all products for POS...");
        
        // Thử lấy tất cả ChiTietSanPham trước
        List<ChiTietSanPham> allChiTiet = chiTietSanPhamRepository.findAll();
        System.out.println("Service: Total ChiTietSanPham records: " + allChiTiet.size());
        
        // Lọc theo điều kiện - chỉ lọc theo trạng thái, số lượng sẽ được tính từ IMEI
        List<ChiTietSanPham> chiTietSanPhams = allChiTiet.stream()
                .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai() == 1)
                .collect(Collectors.toList());
        
        System.out.println("Service: Found " + chiTietSanPhams.size() + " eligible ChiTietSanPham records");
        
        // Debug: In ra 3 records đầu tiên
        chiTietSanPhams.stream().limit(3).forEach(ct -> {
            System.out.println("ChiTiet ID: " + ct.getId() + 
                             ", MaCtsp: " + ct.getMaCtsp() + 
                             ", TrangThai: " + ct.getTrangThai() + 
                             ", SoLuong: " + ct.getSoLuong() +
                             ", SanPham: " + (ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : "null"));
        });
        
        List<SanPhamPosDTO> result = chiTietSanPhams.stream()
                .map(this::convertToDTO)
                .filter(dto -> dto != null && dto.getSoLuongTon() > 0) // Loại bỏ null DTOs và sản phẩm không có IMEI
                .collect(Collectors.toList());
        
        System.out.println("Service: Converted to " + result.size() + " DTOs");
        return result;
    }

    @Override
    public List<SanPhamPosDTO> getProductsByHang(Integer hangId) {
        List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.findBySanPhamHangIdAndTrangThaiAndSoLuongGreaterThan(hangId, 1, 0);
        return chiTietSanPhams.stream()
                .map(this::convertToDTO)
                .filter(dto -> dto != null && dto.getSoLuongTon() > 0) // Filter by IMEI count
                .collect(Collectors.toList());
    }

    @Override
    public List<SanPhamPosDTO> searchProducts(String keyword) {
        List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.searchByKeyword(keyword);
        return chiTietSanPhams.stream()
                .filter(ct -> ct.getTrangThai() == 1) // Only filter by status, quantity will be calculated from IMEI
                .map(this::convertToDTO)
                .filter(dto -> dto != null && dto.getSoLuongTon() > 0) // Filter by IMEI count
                .collect(Collectors.toList());
    }

    @Override
    public SanPhamPosDTO getProductById(Integer id) {
        Optional<ChiTietSanPham> chiTietSanPham = chiTietSanPhamRepository.findById(id);
        return chiTietSanPham.map(this::convertToDTO).orElse(null);
    }

    @Override
    @Transactional
    public boolean updateStock(Integer chiTietSanPhamId, Integer quantity) {
        try {
            // Check if we have enough available IMEI
            long availableImeiCount = imeiRepository.countAvailableByChiTiet(chiTietSanPhamId);
            if (availableImeiCount < quantity) {
                return false;
            }
            
            // Get available IMEI records to mark as sold
            List<Imei> availableImeis = imeiRepository.findByChiTietAndStatus(chiTietSanPhamId, 1);
            if (availableImeis.size() < quantity) {
                return false;
            }
            
            // Mark the required number of IMEI as sold (status = 0)
            for (int i = 0; i < quantity; i++) {
                Imei imei = availableImeis.get(i);
                imei.setTrangThai(0); // Mark as sold
                imeiRepository.save(imei);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating stock: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean checkStock(Integer chiTietSanPhamId, Integer quantity) {
        // Check stock based on available IMEI count
        long availableImeiCount = imeiRepository.countAvailableByChiTiet(chiTietSanPhamId);
        return availableImeiCount >= quantity;
    }

    private SanPhamPosDTO convertToDTO(ChiTietSanPham chiTiet) {
        try {
            SanPhamPosDTO dto = new SanPhamPosDTO();
            
            // Thông tin cơ bản
            dto.setChiTietSanPhamId(chiTiet.getId());
            dto.setMaCtsp(chiTiet.getMaCtsp() != null ? chiTiet.getMaCtsp() : "N/A");
            dto.setGia(chiTiet.getGiaBan() != null ? chiTiet.getGiaBan().doubleValue() : 0.0);
            // Count available IMEI records for this ChiTietSanPham
            int soLuongTon = (int) imeiRepository.countAvailableByChiTiet(chiTiet.getId());
            dto.setSoLuongTon(soLuongTon);
            
            // Tính số lượng đã bán từ ImeiDaBan (qua HoaDonCt)
            Long soLuongDaBan = hoaDonCtRepository.countSoLuongDaBanByChiTietSanPhamId(chiTiet.getId());
            dto.setSoLuongDaBan(soLuongDaBan != null ? soLuongDaBan.intValue() : 0);
            
            dto.setTrangThai(chiTiet.getTrangThai() != null ? chiTiet.getTrangThai() : 0);
            
            // Thông tin sản phẩm
            if (chiTiet.getSanPham() != null) {
                dto.setId(chiTiet.getSanPham().getId());
                dto.setTenSanPham(chiTiet.getSanPham().getTenSanPham() != null ? 
                                 chiTiet.getSanPham().getTenSanPham() : "Sản phẩm không tên");
                
                // Thông tin hãng
                if (chiTiet.getSanPham().getHang() != null) {
                    dto.setHangId(chiTiet.getSanPham().getHang().getId());
                    dto.setTenHang(chiTiet.getSanPham().getHang().getTen() != null ? 
                                  chiTiet.getSanPham().getHang().getTen() : "Không rõ hãng");
                } else {
                    dto.setTenHang("Không rõ hãng");
                }
                
                // Thông tin danh mục
                if (chiTiet.getSanPham().getDanhMuc() != null) {
                    dto.setDanhMucId(chiTiet.getSanPham().getDanhMuc().getId());
                    dto.setTenDanhMuc(chiTiet.getSanPham().getDanhMuc().getTenDanhMuc() != null ? 
                                     chiTiet.getSanPham().getDanhMuc().getTenDanhMuc() : "Không rõ danh mục");
                } else {
                    dto.setTenDanhMuc("Không rõ danh mục");
                }
            } else {
                dto.setTenSanPham("Sản phẩm không tên");
                dto.setTenHang("Không rõ hãng");
            }
            
            // Thông tin ROM, RAM, màu sắc
            if (chiTiet.getRom() != null) {
                dto.setRomId(chiTiet.getRom().getId());
                dto.setTenRom(chiTiet.getRom().getDungLuong() != null ? 
                             chiTiet.getRom().getDungLuong() : "N/A");
            } else {
                dto.setTenRom("N/A");
            }
            
            if (chiTiet.getRam() != null) {
                dto.setRamId(chiTiet.getRam().getId());
                dto.setTenRam(chiTiet.getRam().getTenRam() != null ? 
                             chiTiet.getRam().getTenRam() : "N/A");
            } else {
                dto.setTenRam("N/A");
            }
            
            if (chiTiet.getMauSac() != null) {
                dto.setMauSacId(chiTiet.getMauSac().getId());
                dto.setTenMauSac(chiTiet.getMauSac().getTenMau() != null ? 
                                chiTiet.getMauSac().getTenMau() : "N/A");
            } else {
                dto.setTenMauSac("N/A");
            }
            
            // Lấy hình ảnh đại diện
            try {
                List<HinhAnh> hinhAnhs = hinhAnhRepository.findByChiTietSanPhamId(chiTiet.getId());
                if (!hinhAnhs.isEmpty() && hinhAnhs.get(0).getUrlAnh() != null) {
                    dto.setHinhAnh(hinhAnhs.get(0).getUrlAnh());
                } else {
                    dto.setHinhAnh("/default-product.jpg"); // Default image
                }
            } catch (Exception e) {
                dto.setHinhAnh("/default-product.jpg"); // Default image
                System.out.println("Error loading image for product " + chiTiet.getId() + ": " + e.getMessage());
            }
            
            return dto;
        } catch (Exception e) {
            System.err.println("Error converting ChiTietSanPham to DTO: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public java.util.Map<String, Object> getDatabaseStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        try {
            // Tổng số ChiTietSanPham
            List<ChiTietSanPham> allChiTiet = chiTietSanPhamRepository.findAll();
            stats.put("totalChiTietSanPham", allChiTiet.size());
            
            // Số ChiTietSanPham có trạng thái = 1
            long activeCount = allChiTiet.stream().filter(ct -> ct.getTrangThai() == 1).count();
            stats.put("activeChiTietSanPham", activeCount);
            
            // Số ChiTietSanPham có số lượng > 0
            long inStockCount = allChiTiet.stream().filter(ct -> ct.getSoLuong() > 0).count();
            stats.put("inStockChiTietSanPham", inStockCount);
            
            // Số ChiTietSanPham đủ điều kiện (trạng thái = 1 và số lượng > 0)
            long eligibleCount = allChiTiet.stream()
                .filter(ct -> ct.getTrangThai() == 1 && ct.getSoLuong() > 0)
                .count();
            stats.put("eligibleChiTietSanPham", eligibleCount);
            
            // Lấy 5 records đầu tiên để xem chi tiết
            List<java.util.Map<String, Object>> samples = allChiTiet.stream()
                .limit(5)
                .map(ct -> {
                    java.util.Map<String, Object> sample = new java.util.HashMap<>();
                    sample.put("id", ct.getId());
                    sample.put("maCtsp", ct.getMaCtsp());
                    sample.put("trangThai", ct.getTrangThai());
                    sample.put("soLuong", ct.getSoLuong());
                    sample.put("giaBan", ct.getGiaBan());
                    sample.put("sanPhamId", ct.getSanPham() != null ? ct.getSanPham().getId() : null);
                    sample.put("tenSanPham", ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : null);
                    return sample;
                })
                .collect(java.util.stream.Collectors.toList());
            stats.put("sampleRecords", samples);
            
        } catch (Exception e) {
            stats.put("error", e.getMessage());
            stats.put("stackTrace", java.util.Arrays.toString(e.getStackTrace()));
        }
        
        return stats;
    }
}
