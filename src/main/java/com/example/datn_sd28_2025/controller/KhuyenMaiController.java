package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.DotGiamGiaSanPhamDTO;
import com.example.datn_sd28_2025.dto.KhuyenMaiDTO;
import com.example.datn_sd28_2025.service.KhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(defaultValue = "Admin") String nguoiTao) {
        try {
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
            @SuppressWarnings("unchecked")
            List<Integer> idSanPhams = (List<Integer>) payload.get("idSanPhams");
            String nguoiTao = (String) payload.getOrDefault("nguoiTao", "Admin");

            khuyenMaiService.addMultipleProducts(id, idSanPhams, nguoiTao);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã thêm sản phẩm thành công");
            response.put("count", idSanPhams.size());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
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

