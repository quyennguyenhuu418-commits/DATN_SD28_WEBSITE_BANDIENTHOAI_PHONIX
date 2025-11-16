package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.DotGiamGiaSanPhamDTO;
import com.example.datn_sd28_2025.dto.KhuyenMaiDTO;
import com.example.datn_sd28_2025.service.KhuyenMaiService;
import com.example.datn_sd28_2025.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/khuyen-mai")
@CrossOrigin(origins = "*")
public class KhuyenMaiController {

    @Autowired
    private KhuyenMaiService khuyenMaiService;

    @GetMapping
    public ResponseEntity<List<KhuyenMaiDTO>> getAll() {
        List<KhuyenMaiDTO> khuyenMais = khuyenMaiService.getAll();
        return ResponseEntity.ok(khuyenMais);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhuyenMaiDTO> getById(@PathVariable Integer id) {
        Optional<KhuyenMaiDTO> khuyenMai = khuyenMaiService.getById(id);
        return khuyenMai.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<KhuyenMaiDTO>> getActivePromotions() {
        List<KhuyenMaiDTO> activePromotions = khuyenMaiService.getActivePromotions();
        return ResponseEntity.ok(activePromotions);
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<KhuyenMaiDTO> getByCode(@PathVariable String code) {
        Optional<KhuyenMaiDTO> khuyenMai = khuyenMaiService.getByCode(code);
        return khuyenMai.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<KhuyenMaiDTO> create(@RequestBody KhuyenMaiDTO khuyenMaiDTO) {
        try {
            KhuyenMaiDTO created = khuyenMaiService.save(khuyenMaiDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<KhuyenMaiDTO> update(@PathVariable Integer id, @RequestBody KhuyenMaiDTO khuyenMaiDTO) {
        try {
            KhuyenMaiDTO updated = khuyenMaiService.update(id, khuyenMaiDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            khuyenMaiService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<KhuyenMaiDTO> toggleStatus(@PathVariable Integer id) {
        try {
            KhuyenMaiDTO updated = khuyenMaiService.toggleStatus(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============ API QUẢN LÝ SẢN PHẨM ÁP DỤNG ĐỢT GIẢM GIÁ ============

    /**
     * Lấy danh sách sản phẩm áp dụng cho đợt giảm giá
     */
    @GetMapping("/{id}/san-pham")
    public ResponseEntity<List<DotGiamGiaSanPhamDTO>> getAppliedProducts(@PathVariable Integer id) {
        try {
            List<DotGiamGiaSanPhamDTO> products = khuyenMaiService.getAppliedProducts(id);
            System.out.println("=== Controller Response Debug ===");
            System.out.println("Products count: " + products.size());
            if (!products.isEmpty()) {
                System.out.println("First product: " + products.get(0));
                System.out.println("First product tenDanhMuc: " + products.get(0).getTenDanhMuc());
            }
            System.out.println("=== End Controller Response Debug ===");
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Thêm 1 sản phẩm vào đợt giảm giá
     */
    @PostMapping("/{idKhuyenMai}/san-pham/{idSanPham}")
    public ResponseEntity<?> addProductToPromotion(
            @PathVariable Integer idKhuyenMai,
            @PathVariable Integer idSanPham,
            @RequestParam(required = false) String nguoiTao) {
        try {
            // Nếu không có nguoiTao trong request, lấy từ user đăng nhập
            if (nguoiTao == null || nguoiTao.trim().isEmpty()) {
                nguoiTao = SecurityUtil.getCurrentUsername();
            }
            DotGiamGiaSanPhamDTO result = khuyenMaiService.addProductToPromotion(idKhuyenMai, idSanPham, nguoiTao);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Thêm nhiều sản phẩm vào đợt giảm giá
     */
    @PostMapping("/{id}/san-pham/batch")
    public ResponseEntity<?> addMultipleProducts(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) {
        try {
            // Lấy nguoiTao từ payload hoặc từ user đăng nhập
            String nguoiTao = (String) payload.get("nguoiTao");
            if (nguoiTao == null || nguoiTao.trim().isEmpty()) {
                nguoiTao = SecurityUtil.getCurrentUsername();
            }
            
            // Support both old format (idChiTietSanPhams) and new format (products)
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> products = (List<Map<String, Object>>) payload.get("products");
            @SuppressWarnings("unchecked")
            List<Integer> idChiTietSanPhams = (List<Integer>) payload.get("idChiTietSanPhams");
            
            if (products != null && !products.isEmpty()) {
                // New format: products array with idChiTietSanPham and soLuongToiDa
                List<ProductQuantityData> productData = new ArrayList<>();
                for (Map<String, Object> product : products) {
                    Integer ctspId = (Integer) product.get("idChiTietSanPham");
                    Object soLuongObj = product.get("soLuongToiDa");
                    Integer soLuongToiDa = null;
                    if (soLuongObj != null) {
                        if (soLuongObj instanceof Number) {
                            soLuongToiDa = ((Number) soLuongObj).intValue();
                        } else if (soLuongObj instanceof String && !((String) soLuongObj).trim().isEmpty()) {
                            try {
                                soLuongToiDa = Integer.parseInt((String) soLuongObj);
                            } catch (NumberFormatException e) {
                                // Keep null if parsing fails
                            }
                        }
                    }
                    productData.add(new ProductQuantityData(ctspId, soLuongToiDa));
                }
                khuyenMaiService.addMultipleProductsWithQuantity(id, productData, nguoiTao);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Đã thêm sản phẩm thành công");
                response.put("count", productData.size());
                return ResponseEntity.ok(response);
            } else if (idChiTietSanPhams != null && !idChiTietSanPhams.isEmpty()) {
                // Old format: backward compatibility
                List<ProductQuantityData> productData = idChiTietSanPhams.stream()
                        .map(ctspId -> new ProductQuantityData(ctspId, null))
                        .toList();
                khuyenMaiService.addMultipleProductsWithQuantity(id, productData, nguoiTao);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Đã thêm sản phẩm thành công");
                response.put("count", idChiTietSanPhams.size());
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Không có sản phẩm nào được chọn");
                return ResponseEntity.badRequest().body(error);
            }
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    // Helper class for product quantity data
    public static class ProductQuantityData {
        private Integer idChiTietSanPham;
        private Integer soLuongToiDa;
        
        public ProductQuantityData(Integer idChiTietSanPham, Integer soLuongToiDa) {
            this.idChiTietSanPham = idChiTietSanPham;
            this.soLuongToiDa = soLuongToiDa;
        }
        
        public Integer getIdChiTietSanPham() {
            return idChiTietSanPham;
        }
        
        public Integer getSoLuongToiDa() {
            return soLuongToiDa;
        }
    }

    /**
     * Xóa sản phẩm khỏi đợt giảm giá
     */
    @DeleteMapping("/{idKhuyenMai}/san-pham/{idSanPham}")
    public ResponseEntity<?> removeProductFromPromotion(
            @PathVariable Integer idKhuyenMai,
            @PathVariable Integer idSanPham) {
        try {
            khuyenMaiService.removeProductFromPromotion(idKhuyenMai, idSanPham);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa sản phẩm khỏi đợt giảm giá");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Đếm số sản phẩm áp dụng trong đợt giảm giá
     */
    @GetMapping("/{id}/san-pham/count")
    public ResponseEntity<Map<String, Long>> countAppliedProducts(@PathVariable Integer id) {
        try {
            long count = khuyenMaiService.countAppliedProducts(id);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

