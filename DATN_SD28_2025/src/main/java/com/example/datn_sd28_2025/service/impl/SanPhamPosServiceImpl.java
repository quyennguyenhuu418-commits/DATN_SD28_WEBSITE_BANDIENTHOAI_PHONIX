package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.SanPhamPosDTO;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.HinhAnh;
import com.example.datn_sd28_2025.entity.Imei;
import com.example.datn_sd28_2025.entity.DotGiamGiaSanPham;
import com.example.datn_sd28_2025.entity.KhuyenMai;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.repository.HinhAnhRepository;
import com.example.datn_sd28_2025.repository.ImeiRepository;
import com.example.datn_sd28_2025.repository.DotGiamGiaSanPhamRepository;
import com.example.datn_sd28_2025.service.SanPhamPosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SanPhamPosServiceImpl implements SanPhamPosService {

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private HinhAnhRepository hinhAnhRepository;
    
    @Autowired
    private ImeiRepository imeiRepository;

    @Autowired
    private DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<SanPhamPosDTO> getAllProductsForPos() {
        try {
            System.out.println("Service: Getting all products for POS...");
            
            // Thử lấy tất cả ChiTietSanPham trước
            List<ChiTietSanPham> allChiTiet = chiTietSanPhamRepository.findAll();
            System.out.println("Service: Total ChiTietSanPham records: " + allChiTiet.size());
            
            // Lọc theo điều kiện - chỉ lấy CTSP active (trangThai == 1) và SP active (sanPham.trangThai == 1)
            List<ChiTietSanPham> chiTietSanPhams = allChiTiet.stream()
                    .filter(ct -> {
                        // Filter CTSP status
                        if (ct.getTrangThai() == null || ct.getTrangThai() != 1) {
                            return false;
                        }
                        // Filter parent SP status - prevent inactive products
                        if (ct.getSanPham() == null || ct.getSanPham().getTrangThai() == null || ct.getSanPham().getTrangThai() != 1) {
                            return false;
                        }
                        return true;
                    })
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
            
            // Xử lý từng item với try-catch để tránh toàn bộ transaction bị rollback
            List<SanPhamPosDTO> result = chiTietSanPhams.stream()
                    .map(ct -> {
                        try {
                            return convertToDTO(ct);
                        } catch (Exception e) {
                            System.err.println("Error converting ChiTietSanPham to DTO: " + e.getMessage());
                            e.printStackTrace();
                            return null; // Skip item có lỗi
                        }
                    })
                    .filter(dto -> dto != null && dto.getSoLuongTon() > 0) // Loại bỏ null DTOs và sản phẩm không có IMEI
                    .collect(Collectors.toList());
            
            System.out.println("Service: Converted to " + result.size() + " DTOs");
            return result;
        } catch (Exception e) {
            // Nếu có lỗi nghiêm trọng, return empty list thay vì throw exception
            System.err.println("Error getting products: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public List<SanPhamPosDTO> getProductsByHang(Integer hangId) {
        List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.findBySanPhamHangIdAndTrangThaiAndSoLuongGreaterThan(hangId, 1, 0);
        return chiTietSanPhams.stream()
                .filter(ct -> {
                    // Filter parent SP status - prevent inactive products
                    if (ct.getSanPham() == null || ct.getSanPham().getTrangThai() == null || ct.getSanPham().getTrangThai() != 1) {
                        return false;
                    }
                    return true;
                })
                .map(this::convertToDTO)
                .filter(dto -> dto != null && dto.getSoLuongTon() > 0) // Filter by IMEI count
                .collect(Collectors.toList());
    }

    @Override
    public List<SanPhamPosDTO> searchProducts(String keyword) {
        List<ChiTietSanPham> chiTietSanPhams = chiTietSanPhamRepository.searchByKeyword(keyword);
        return chiTietSanPhams.stream()
                .filter(ct -> {
                    // Filter CTSP status
                    if (ct.getTrangThai() == null || ct.getTrangThai() != 1) {
                        return false;
                    }
                    // Filter parent SP status - prevent inactive products
                    if (ct.getSanPham() == null || ct.getSanPham().getTrangThai() == null || ct.getSanPham().getTrangThai() != 1) {
                        return false;
                    }
                    return true;
                })
                .map(this::convertToDTO)
                .filter(dto -> dto != null && dto.getSoLuongTon() > 0) // Filter by IMEI count
                .collect(Collectors.toList());
    }

    @Override
    public SanPhamPosDTO getProductById(Integer id) {
        Optional<ChiTietSanPham> chiTietSanPham = chiTietSanPhamRepository.findById(id);
        if (chiTietSanPham.isEmpty()) {
            return null;
        }
        
        ChiTietSanPham ct = chiTietSanPham.get();
        
        // Validate CTSP status - only return active products
        if (ct.getTrangThai() == null || ct.getTrangThai() != 1) {
            return null; // Product detail is inactive
        }
        
        // Validate parent SP status - only return if parent product is active
        if (ct.getSanPham() == null || ct.getSanPham().getTrangThai() == null || ct.getSanPham().getTrangThai() != 1) {
            return null; // Parent product is inactive
        }
        
        return convertToDTO(ct);
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
            double basePrice = chiTiet.getGiaBan() != null ? chiTiet.getGiaBan().doubleValue() : 0.0;
            dto.setGiaGoc(basePrice);
            // Count available IMEI records for this ChiTietSanPham
            int soLuongTon = (int) imeiRepository.countAvailableByChiTiet(chiTiet.getId());
            dto.setSoLuongTon(soLuongTon);
            dto.setTrangThai(chiTiet.getTrangThai() != null ? chiTiet.getTrangThai() : 0);
            
            // Thông tin sản phẩm
            if (chiTiet.getSanPham() != null) {
                dto.setId(chiTiet.getSanPham().getId());
                dto.setTenSanPham(chiTiet.getSanPham().getTenSanPham() != null ? 
                                 chiTiet.getSanPham().getTenSanPham() : "Sản phẩm không tên");
                
                // Set parent product status for frontend validation
                dto.setTrangThaiSanPham(chiTiet.getSanPham().getTrangThai() != null ? chiTiet.getSanPham().getTrangThai() : 0);
                
                // Danh mục
                if (chiTiet.getSanPham().getDanhMuc() != null) {
                    dto.setDanhMucId(chiTiet.getSanPham().getDanhMuc().getId());
                    dto.setTenDanhMuc(chiTiet.getSanPham().getDanhMuc().getTenDanhMuc());
                }
                
                // Thông tin hãng
                if (chiTiet.getSanPham().getHang() != null) {
                    dto.setHangId(chiTiet.getSanPham().getHang().getId());
                    dto.setTenHang(chiTiet.getSanPham().getHang().getTen() != null ? 
                                  chiTiet.getSanPham().getHang().getTen() : "Không rõ hãng");
                } else {
                    dto.setTenHang("Không rõ hãng");
                }
            } else {
                dto.setTenSanPham("Sản phẩm không tên");
                dto.setTenHang("Không rõ hãng");
                dto.setTrangThaiSanPham(0); // No parent product = inactive
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
            
            // Xử lý màu sắc với try-catch để tránh lỗi khi thiếu cột ma_hex
            try {
                if (chiTiet.getMauSac() != null) {
                    dto.setMauSacId(chiTiet.getMauSac().getId());
                    dto.setTenMauSac(chiTiet.getMauSac().getTenMau() != null ? 
                                    chiTiet.getMauSac().getTenMau() : "N/A");
                } else {
                    dto.setTenMauSac("N/A");
                }
            } catch (Exception e) {
                // Nếu có lỗi khi load MauSac (ví dụ: thiếu cột ma_hex), set giá trị mặc định
                System.err.println("Error loading MauSac for CTSP ID=" + chiTiet.getId() + ": " + e.getMessage());
                dto.setTenMauSac("N/A");
                dto.setMauSacId(chiTiet.getMauSac() != null ? chiTiet.getMauSac().getId() : null);
            }
            
            // Áp dụng ĐỢT GIẢM GIÁ (đợt trước, voucher sau)
            try {
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                java.util.List<DotGiamGiaSanPham> campaigns = dotGiamGiaSanPhamRepository.findActiveByChiTietSanPham(chiTiet.getId(), now);
                System.out.println("[DEBUG] CTSP ID=" + chiTiet.getId() + ", campaign count=" + campaigns.size());
                // Chỉ áp dụng theo phần trăm, chọn phần trăm lớn nhất nếu trùng
                // Kiểm tra số lượng đã bán: nếu đã hết số lượng tối đa thì không áp dụng giảm giá
                Double bestPercent = null;
                String bestName = null;
                BigDecimal bestGiamToiDa = null; // Lưu giá trị giảm tối đa của campaign tốt nhất
                for (DotGiamGiaSanPham d : campaigns) {
                    // Kiểm tra số lượng: nếu có giới hạn và đã hết thì skip
                    if (d.getSoLuongToiDa() != null) {
                        int soLuongDaBan = d.getSoLuongDaBan() != null ? d.getSoLuongDaBan() : 0;
                        if (soLuongDaBan >= d.getSoLuongToiDa()) {
                            System.out.println("[DEBUG]   Dot id=" + d.getId() + " đã hết số lượng giảm giá (đã bán: " + soLuongDaBan + "/" + d.getSoLuongToiDa() + ")");
                            continue; // Đã hết số lượng, không áp dụng giảm giá
                        }
                    }
                    
                    KhuyenMai km = d.getKhuyenMai();
                    double percent = 0.0;
                    if (d.getPhanTramGiam() != null) percent = d.getPhanTramGiam().doubleValue();
                    else if (km != null && km.getPhanTramGiam() != null) percent = km.getPhanTramGiam().doubleValue();
                    
                    BigDecimal giamToiDa = km != null ? km.getGiamToiDa() : null;
                    System.out.println("[DEBUG]   Dot id=" + d.getId() + ", phanTramGiam(Dot)=" + d.getPhanTramGiam() + ", phanTramGiam(KhuyenMai)=" + (km != null ? km.getPhanTramGiam() : null) + ", percentUsed=" + percent + ", giamToiDa=" + giamToiDa + ", campaignName=" + (km != null ? km.getTenKhuyenMai() : null) + ", soLuongDaBan=" + (d.getSoLuongDaBan() != null ? d.getSoLuongDaBan() : 0) + ", soLuongToiDa=" + d.getSoLuongToiDa());
                    if (percent <= 0) continue;
                    if (bestPercent == null || percent > bestPercent) {
                        bestPercent = percent;
                        bestName = km != null ? km.getTenKhuyenMai() : null;
                        bestGiamToiDa = giamToiDa;
                    }
                }
                double finalPrice = basePrice;
                if (bestPercent != null && bestPercent > 0) {
                    // Tính giá giảm theo phần trăm
                    double discountAmount = basePrice * (bestPercent / 100.0);
                    
                    // Nếu có giới hạn giảm tối đa, áp dụng giới hạn đó
                    if (bestGiamToiDa != null && bestGiamToiDa.doubleValue() > 0) {
                        double maxDiscount = bestGiamToiDa.doubleValue();
                        if (discountAmount > maxDiscount) {
                            discountAmount = maxDiscount;
                            System.out.println("[DEBUG]   Áp dụng giới hạn giảm tối đa: " + maxDiscount + " thay vì " + (basePrice * (bestPercent / 100.0)));
                        }
                    }
                    
                    finalPrice = basePrice - discountAmount;
                }
                dto.setGiaSauGiam(finalPrice > 0 ? finalPrice : basePrice);
                dto.setGia(dto.getGiaSauGiam()); // giá hiển thị để bán ở POS
                dto.setGiamPhanTram(bestPercent);
                dto.setTenDotGiam(bestName);
            } catch (Exception ex) {
                // fallback
                dto.setGia(basePrice);
                dto.setGiaSauGiam(basePrice);
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


