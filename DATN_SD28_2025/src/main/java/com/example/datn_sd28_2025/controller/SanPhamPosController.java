package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.SanPhamPosDTO;
import com.example.datn_sd28_2025.service.SanPhamPosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.repository.ImeiRepository;
import com.example.datn_sd28_2025.repository.SanPhamRepository;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.SanPham;

@RestController
@RequestMapping("/api/san-pham-pos")
@CrossOrigin(origins = "*")
public class SanPhamPosController {

    @Autowired
    private SanPhamPosService sanPhamPosService;
    
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;
    
    @Autowired
    private ImeiRepository imeiRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("SanPhamPosController is working!");
    }
    
    @GetMapping("/force-products")
    public ResponseEntity<List<SanPhamPosDTO>> forceGetProducts() {
        System.out.println("=== FORCE GET PRODUCTS ===");
        
        try {
            // Lấy tất cả SanPham
            List<SanPham> allSanPham = sanPhamRepository.findAll();
            System.out.println("Total SanPham: " + allSanPham.size());
            
            // Lấy tất cả ChiTietSanPham
            List<ChiTietSanPham> allChiTiet = chiTietSanPhamRepository.findAll();
            System.out.println("Total ChiTietSanPham: " + allChiTiet.size());
            
            // In ra 3 records đầu tiên
            for (int i = 0; i < Math.min(3, allChiTiet.size()); i++) {
                ChiTietSanPham ct = allChiTiet.get(i);
                System.out.println("ChiTiet " + i + ": ID=" + ct.getId() + 
                                 ", MaCtsp=" + ct.getMaCtsp() + 
                                 ", TrangThai=" + ct.getTrangThai() + 
                                 ", SoLuong=" + ct.getSoLuong() +
                                 ", GiaBan=" + ct.getGiaBan() +
                                 ", SanPham=" + (ct.getSanPham() != null ? ct.getSanPham().getTenSanPham() : "NULL"));
            }
            
            // Tạo danh sách sản phẩm từ tất cả ChiTietSanPham (không lọc gì cả)
            List<SanPhamPosDTO> products = new java.util.ArrayList<>();
            
            for (ChiTietSanPham ct : allChiTiet) {
                try {
                    SanPhamPosDTO dto = new SanPhamPosDTO();
                    dto.setChiTietSanPhamId(ct.getId());
                    dto.setMaCtsp(ct.getMaCtsp() != null ? ct.getMaCtsp() : "SP" + ct.getId());
                    dto.setGia(ct.getGiaBan() != null ? ct.getGiaBan().doubleValue() : 100000.0);
                    dto.setSoLuongTon(ct.getSoLuong() != null ? ct.getSoLuong() : 1);
                    dto.setTrangThai(ct.getTrangThai() != null ? ct.getTrangThai() : 1);
                    
                    if (ct.getSanPham() != null) {
                        dto.setId(ct.getSanPham().getId());
                        dto.setTenSanPham(ct.getSanPham().getTenSanPham() != null ? 
                                         ct.getSanPham().getTenSanPham() : "Sản phẩm " + ct.getId());
                        
                        if (ct.getSanPham().getHang() != null) {
                            dto.setHangId(ct.getSanPham().getHang().getId());
                            dto.setTenHang(ct.getSanPham().getHang().getTen() != null ? 
                                          ct.getSanPham().getHang().getTen() : "Unknown");
                        } else {
                            dto.setTenHang("No Brand");
                        }
                    } else {
                        dto.setId(ct.getId());
                        dto.setTenSanPham("Sản phẩm " + ct.getId());
                        dto.setTenHang("No Brand");
                    }
                    
                    dto.setHinhAnh("/default-product.jpg");
                    dto.setTenRom(ct.getRom() != null ? ct.getRom().getDungLuong() : "64GB");
                    dto.setTenRam(ct.getRam() != null ? ct.getRam().getTenRam() : "4GB");
                    dto.setTenMauSac(ct.getMauSac() != null ? ct.getMauSac().getTenMau() : "Đen");
                    
                    products.add(dto);
                    
                    if (products.size() >= 10) break; // Chỉ lấy 10 sản phẩm đầu tiên
                    
                } catch (Exception e) {
                    System.err.println("Error converting ChiTiet " + ct.getId() + ": " + e.getMessage());
                }
            }
            
            System.out.println("Created " + products.size() + " products from database");
            return ResponseEntity.ok(products);
            
        } catch (Exception e) {
            System.err.println("Error in forceGetProducts: " + e.getMessage());
            e.printStackTrace();
            
            // Nếu tất cả fail, trả về mock data
            return ResponseEntity.ok(getMockProducts());
        }
    }
    
    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugDatabase() {
        Map<String, Object> debug = new HashMap<>();
        try {
            // Lấy thống kê database
            Map<String, Object> dbStats = sanPhamPosService.getDatabaseStats();
            debug.putAll(dbStats);
            
            // Kiểm tra tổng số ChiTietSanPham qua service
            List<SanPhamPosDTO> allProducts = sanPhamPosService.getAllProductsForPos();
            debug.put("totalProductsFromService", allProducts.size());
            
            // Lấy thông tin chi tiết 3 sản phẩm đầu tiên từ service
            debug.put("sampleProductsFromService", allProducts.stream().limit(3).collect(java.util.stream.Collectors.toList()));
            
            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            debug.put("error", e.getMessage());
            debug.put("stackTrace", java.util.Arrays.toString(e.getStackTrace()));
            return ResponseEntity.ok(debug);
        }
    }
    
    @GetMapping("/raw-data")
    public ResponseEntity<Map<String, Object>> getRawData() {
        Map<String, Object> data = new HashMap<>();
        try {
            // Kiểm tra SanPham table
            List<SanPham> allSanPham = sanPhamRepository.findAll();
            data.put("totalSanPham", allSanPham.size());
            
            // Kiểm tra ChiTietSanPham table
            List<ChiTietSanPham> allChiTiet = chiTietSanPhamRepository.findAll();
            data.put("totalChiTietSanPham", allChiTiet.size());
            
            // Lấy 3 SanPham đầu tiên
            List<Map<String, Object>> sanPhamSamples = allSanPham.stream()
                .limit(3)
                .map(sp -> {
                    Map<String, Object> sample = new HashMap<>();
                    sample.put("id", sp.getId());
                    sample.put("tenSanPham", sp.getTenSanPham());
                    sample.put("trangThai", sp.getTrangThai());
                    sample.put("hangId", sp.getHang() != null ? sp.getHang().getId() : null);
                    sample.put("tenHang", sp.getHang() != null ? sp.getHang().getTen() : null);
                    return sample;
                })
                .collect(java.util.stream.Collectors.toList());
            data.put("sanPhamSamples", sanPhamSamples);
            
            // Lấy 3 ChiTietSanPham đầu tiên
            List<Map<String, Object>> chiTietSamples = allChiTiet.stream()
                .limit(3)
                .map(ct -> {
                    Map<String, Object> sample = new HashMap<>();
                    sample.put("id", ct.getId());
                    sample.put("maCtsp", ct.getMaCtsp());
                    sample.put("trangThai", ct.getTrangThai());
                    sample.put("soLuong", ct.getSoLuong());
                    sample.put("giaBan", ct.getGiaBan());
                    sample.put("sanPhamId", ct.getSanPham() != null ? ct.getSanPham().getId() : null);
                    return sample;
                })
                .collect(java.util.stream.Collectors.toList());
            data.put("chiTietSamples", chiTietSamples);
            
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            data.put("error", e.getMessage());
            data.put("stackTrace", java.util.Arrays.toString(e.getStackTrace()));
            return ResponseEntity.ok(data);
        }
    }

    @GetMapping
    public ResponseEntity<List<SanPhamPosDTO>> getAllProductsForPos() {
        try {
            System.out.println("Getting all products for POS...");
            List<SanPhamPosDTO> products = sanPhamPosService.getAllProductsForPos();
            System.out.println("Found " + products.size() + " products");
            
            // Nếu không có sản phẩm, thử method backup
            if (products.isEmpty()) {
                System.out.println("No products found, trying backup method...");
                products = getBackupProducts();
                System.out.println("Backup method found " + products.size() + " products");
            }
            
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            System.err.println("Error getting products: " + e.getMessage());
            e.printStackTrace();
            
            // Trả về mock data nếu có lỗi
            List<SanPhamPosDTO> mockProducts = getMockProducts();
            return ResponseEntity.ok(mockProducts);
        }
    }
    
    private List<SanPhamPosDTO> getBackupProducts() {
        try {
            // Lấy trực tiếp từ repository
            List<ChiTietSanPham> allChiTiet = chiTietSanPhamRepository.findAll();
            System.out.println("Backup: Found " + allChiTiet.size() + " total ChiTietSanPham");
            
            return allChiTiet.stream()
                    .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai() == 1)
                    .map(this::convertToSimpleDTO)
                    .filter(dto -> dto.getSoLuongTon() > 0) // Filter by IMEI count
                    .limit(10) // Giới hạn 10 sản phẩm để test
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            System.err.println("Backup method failed: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
    
    private SanPhamPosDTO convertToSimpleDTO(ChiTietSanPham chiTiet) {
        SanPhamPosDTO dto = new SanPhamPosDTO();
        dto.setChiTietSanPhamId(chiTiet.getId());
        dto.setMaCtsp(chiTiet.getMaCtsp() != null ? chiTiet.getMaCtsp() : "SP" + chiTiet.getId());
        dto.setGia(chiTiet.getGiaBan() != null ? chiTiet.getGiaBan().doubleValue() : 100000.0);
        // Count available IMEI records for this ChiTietSanPham
        int soLuongTon = (int) imeiRepository.countAvailableByChiTiet(chiTiet.getId());
        dto.setSoLuongTon(soLuongTon);
        dto.setTrangThai(chiTiet.getTrangThai() != null ? chiTiet.getTrangThai() : 0);
        
        if (chiTiet.getSanPham() != null) {
            dto.setId(chiTiet.getSanPham().getId());
            dto.setTenSanPham(chiTiet.getSanPham().getTenSanPham() != null ? 
                             chiTiet.getSanPham().getTenSanPham() : "Sản phẩm " + chiTiet.getId());
            
            // Set parent product status for frontend validation
            dto.setTrangThaiSanPham(chiTiet.getSanPham().getTrangThai() != null ? chiTiet.getSanPham().getTrangThai() : 0);
            
            if (chiTiet.getSanPham().getHang() != null) {
                dto.setHangId(chiTiet.getSanPham().getHang().getId());
                dto.setTenHang(chiTiet.getSanPham().getHang().getTen() != null ? 
                              chiTiet.getSanPham().getHang().getTen() : "Unknown");
            }
        } else {
            dto.setTenSanPham("Sản phẩm " + chiTiet.getId());
            dto.setTenHang("Unknown");
            dto.setTrangThaiSanPham(0); // No parent product = inactive
        }
        
        dto.setHinhAnh("/default-product.jpg");
        dto.setTenRom("64GB");
        dto.setTenRam("4GB");
        dto.setTenMauSac("Đen");
        
        return dto;
    }
    
    private List<SanPhamPosDTO> getMockProducts() {
        List<SanPhamPosDTO> mockProducts = new java.util.ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            SanPhamPosDTO product = new SanPhamPosDTO();
            product.setChiTietSanPhamId(i);
            product.setId(i);
            product.setTenSanPham("iPhone " + (12 + i));
            product.setMaCtsp("IP" + (12 + i) + "00" + i);
            product.setGia(15000000.0 + (i * 1000000));
            product.setSoLuongTon(10 + i);
            product.setTrangThai(1);
            product.setHangId(1);
            product.setTenHang("Apple");
            product.setHinhAnh("/default-product.jpg");
            product.setTenRom("128GB");
            product.setTenRam("6GB");
            product.setTenMauSac("Đen");
            mockProducts.add(product);
        }
        
        System.out.println("Created " + mockProducts.size() + " mock products");
        return mockProducts;
    }
    
    @PostMapping("/fix-products")
    public ResponseEntity<String> fixProducts() {
        try {
            System.out.println("=== FIXING PRODUCTS ===");
            
            List<ChiTietSanPham> allChiTiet = chiTietSanPhamRepository.findAll();
            System.out.println("Found " + allChiTiet.size() + " ChiTietSanPham records");
            
            int fixed = 0;
            for (ChiTietSanPham ct : allChiTiet) {
                boolean needUpdate = false;
                
                // Sửa trạng thái null hoặc 0 thành 1
                if (ct.getTrangThai() == null || ct.getTrangThai() == 0) {
                    ct.setTrangThai(1);
                    needUpdate = true;
                }
                
                // Sửa số lượng null hoặc 0 thành 1
                if (ct.getSoLuong() == null || ct.getSoLuong() <= 0) {
                    ct.setSoLuong(10);
                    needUpdate = true;
                }
                
                // Sửa giá bán null thành giá mặc định
                if (ct.getGiaBan() == null) {
                    ct.setGiaBan(new java.math.BigDecimal("100000"));
                    needUpdate = true;
                }
                
                if (needUpdate) {
                    chiTietSanPhamRepository.save(ct);
                    fixed++;
                    System.out.println("Fixed ChiTiet ID: " + ct.getId());
                }
            }
            
            System.out.println("Fixed " + fixed + " products");
            return ResponseEntity.ok("Fixed " + fixed + " products successfully!");
            
        } catch (Exception e) {
            System.err.println("Error fixing products: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/hang/{hangId}")
    public ResponseEntity<List<SanPhamPosDTO>> getProductsByHang(@PathVariable Integer hangId) {
        try {
            List<SanPhamPosDTO> products = sanPhamPosService.getProductsByHang(hangId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<SanPhamPosDTO>> searchProducts(@RequestParam String keyword) {
        try {
            List<SanPhamPosDTO> products = sanPhamPosService.searchProducts(keyword);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SanPhamPosDTO> getProductById(@PathVariable Integer id) {
        try {
            SanPhamPosDTO product = sanPhamPosService.getProductById(id);
            if (product != null) {
                return ResponseEntity.ok(product);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/check-stock")
    public ResponseEntity<Map<String, Object>> checkStock(@RequestBody Map<String, Integer> request) {
        try {
            Integer chiTietSanPhamId = request.get("chiTietSanPhamId");
            Integer quantity = request.get("quantity");
            
            boolean available = sanPhamPosService.checkStock(chiTietSanPhamId, quantity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("available", available);
            response.put("message", available ? "Đủ hàng" : "Không đủ hàng");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("available", false);
            error.put("message", "Lỗi kiểm tra tồn kho");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/update-stock")
    public ResponseEntity<Map<String, Object>> updateStock(@RequestBody Map<String, Integer> request) {
        try {
            Integer chiTietSanPhamId = request.get("chiTietSanPhamId");
            Integer quantity = request.get("quantity");
            
            boolean success = sanPhamPosService.updateStock(chiTietSanPhamId, quantity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Cập nhật tồn kho thành công" : "Cập nhật tồn kho thất bại");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi cập nhật tồn kho");
            return ResponseEntity.badRequest().body(error);
        }
    }
}
