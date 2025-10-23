package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.SanPhamDTO;
import com.example.datn_sd28_2025.dto.SanPhamEditDTO;
import com.example.datn_sd28_2025.dto.SanPhamViewDTO;
import com.example.datn_sd28_2025.dto.ChiTietSanPhamCreateDTO;
import com.example.datn_sd28_2025.entity.SanPham;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.HinhAnh;
import com.example.datn_sd28_2025.service.SanPhamService;
import com.example.datn_sd28_2025.service.dto.SanPhamFullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/san-pham")
@CrossOrigin(origins = "*")
public class SanPhamController {

    @Autowired
    private SanPhamService sanPhamService;

    @GetMapping
    public ResponseEntity<List<SanPhamDTO>> getAllSanPham(@RequestParam(required = false) String search) {
        List<SanPhamDTO> sanPhams;
        if (search != null && !search.trim().isEmpty()) {
            sanPhams = sanPhamService.searchSanPham(search.trim());
        } else {
            sanPhams = sanPhamService.getAllSanPham();
        }
        return ResponseEntity.ok(sanPhams);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SanPhamDTO>> getActiveSanPham() {
        List<SanPhamDTO> sanPhams = sanPhamService.getActiveSanPham();
        return ResponseEntity.ok(sanPhams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SanPhamDTO> getSanPhamById(@PathVariable Integer id) {
        Optional<SanPhamDTO> sanPham = sanPhamService.getSanPhamById(id);
        return sanPham.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/edit")
    public ResponseEntity<SanPhamEditDTO> getSanPhamForEdit(@PathVariable Integer id) {
        Optional<SanPhamEditDTO> sanPham = sanPhamService.getSanPhamForEdit(id);
        return sanPham.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<SanPhamViewDTO> getSanPhamForView(@PathVariable Integer id) {
        Optional<SanPhamViewDTO> sanPham = sanPhamService.getSanPhamForView(id);
        return sanPham.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ma/{maSanPham}")
    public ResponseEntity<SanPhamDTO> getSanPhamByMa(@PathVariable String maSanPham) {
        Optional<SanPhamDTO> sanPham = sanPhamService.getSanPhamByMa(maSanPham);
        return sanPham.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SanPham> createSanPham(@RequestBody SanPham sanPham) {
        SanPham createdSanPham = sanPhamService.createSanPham(sanPham);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSanPham);
    }

    @PostMapping("/full")
    public ResponseEntity<SanPhamDTO> createSanPhamFull(@RequestBody com.example.datn_sd28_2025.service.dto.SanPhamFullRequest request) {
        SanPhamDTO sp = sanPhamService.createSanPhamFull(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sp);
    }

    @PutMapping("/{id}/full")
    public ResponseEntity<?> updateSanPhamFull(@PathVariable Integer id, @RequestBody com.example.datn_sd28_2025.service.dto.SanPhamFullRequest request) {
        try {
            SanPhamDTO sp = sanPhamService.updateSanPhamFull(id, request);
            return ResponseEntity.ok(sp);
        } catch (RuntimeException e) {
            // Return detailed error message instead of 404
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cập nhật sản phẩm thất bại",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            // Handle other errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Lỗi hệ thống",
                    "message", e.getMessage(),
                    "timestamp", java.time.LocalDateTime.now()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SanPham> updateSanPham(@PathVariable Integer id, @RequestBody SanPham sanPham) {
        try {
            SanPham updatedSanPham = sanPhamService.updateSanPham(id, sanPham);
            return ResponseEntity.ok(updatedSanPham);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSanPham(@PathVariable Integer id) {
        try {
            sanPhamService.deleteSanPham(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/danh-muc/{danhMucId}")
    public ResponseEntity<List<SanPhamDTO>> getSanPhamByDanhMuc(@PathVariable Integer danhMucId) {
        List<SanPhamDTO> sanPhams = sanPhamService.getSanPhamByDanhMuc(danhMucId);
        return ResponseEntity.ok(sanPhams);
    }

    @GetMapping("/hang/{hangId}")
    public ResponseEntity<List<SanPhamDTO>> getSanPhamByHang(@PathVariable Integer hangId) {
        List<SanPhamDTO> sanPhams = sanPhamService.getSanPhamByHang(hangId);
        return ResponseEntity.ok(sanPhams);
    }

    // Chi tiết sản phẩm endpoints
    @GetMapping("/{sanPhamId}/chi-tiet")
    public ResponseEntity<List<ChiTietSanPham>> getChiTietSanPhamBySanPhamId(@PathVariable Integer sanPhamId) {
        List<ChiTietSanPham> chiTietSanPhams = sanPhamService.getChiTietSanPhamBySanPhamId(sanPhamId);
        return ResponseEntity.ok(chiTietSanPhams);
    }

    @GetMapping("/chi-tiet/available")
    public ResponseEntity<List<ChiTietSanPham>> getAvailableChiTietSanPham() {
        List<ChiTietSanPham> chiTietSanPhams = sanPhamService.getAvailableChiTietSanPham();
        return ResponseEntity.ok(chiTietSanPhams);
    }

    @GetMapping("/chi-tiet/{id}")
    public ResponseEntity<ChiTietSanPham> getChiTietSanPhamById(@PathVariable Integer id) {
        Optional<ChiTietSanPham> chiTietSanPham = sanPhamService.getChiTietSanPhamById(id);
        return chiTietSanPham.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/chi-tiet")
    public ResponseEntity<ChiTietSanPham> createChiTietSanPham(@RequestBody ChiTietSanPhamCreateDTO dto) {
        ChiTietSanPham createdChiTiet = sanPhamService.createChiTietSanPhamFromDTO(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChiTiet);
    }

    @PutMapping("/chi-tiet/{id}")
    public ResponseEntity<ChiTietSanPham> updateChiTietSanPham(@PathVariable Integer id, @RequestBody ChiTietSanPham chiTietSanPham) {
        try {
            ChiTietSanPham updatedChiTiet = sanPhamService.updateChiTietSanPham(id, chiTietSanPham);
            return ResponseEntity.ok(updatedChiTiet);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/chi-tiet/{id}")
    public ResponseEntity<Void> deleteChiTietSanPham(@PathVariable Integer id) {
        try {
            sanPhamService.deleteChiTietSanPham(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // API để quản lý hình ảnh của chi tiết sản phẩm
    @PostMapping("/chi-tiet/{id}/images")
    public ResponseEntity<HinhAnh> addImageToChiTiet(@PathVariable Integer id, @RequestBody HinhAnh hinhAnh) {
        try {
            HinhAnh savedImage = sanPhamService.addImageToChiTiet(id, hinhAnh);
            return ResponseEntity.ok(savedImage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/chi-tiet/{id}/images")
    public ResponseEntity<Void> deleteAllImagesFromChiTiet(@PathVariable Integer id) {
        try {
            sanPhamService.deleteAllImagesFromChiTiet(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Object> cleanupDatabase() {
        try {
            // Xóa dữ liệu NULL trong chi_tiet_san_pham
            int deletedChiTiet = sanPhamService.cleanupNullData();

            return ResponseEntity.ok(Map.of(
                    "message", "Database cleanup completed",
                    "deletedChiTiet", deletedChiTiet
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "message", "Lỗi khi cleanup database"
            ));
        }
    }

    @PostMapping("/test-imei")
    public ResponseEntity<Object> testImei() {
        try {
            // Test payload với IMEI
            SanPhamFullRequest request = new SanPhamFullRequest();
            request.setMaSanPham("test-imei-product");
            request.setTenSanPham("Test IMEI Product");
            request.setMoTa("Test product with IMEI");
            request.setIdDanhMuc(1);
            request.setIdHang(1);

            // Tạo variant với IMEI
            SanPhamFullRequest.Variant variant = new SanPhamFullRequest.Variant();
            variant.setIdRam(1);
            variant.setIdRom(1);
            variant.setIdMauSac(1);
            variant.setSoLuong(2);
            variant.setDonGia(1000000L);
            variant.setImeis(List.of("123456789012345", "987654321098765"));

            request.setVariants(List.of(variant));

            SanPhamDTO result = sanPhamService.createSanPhamFull(request);

            return ResponseEntity.ok(Map.of(
                    "message", "Test IMEI product created successfully",
                    "product", result
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "message", "Lỗi khi test IMEI"
            ));
        }
    }

    @GetMapping("/check-imei/{productId}")
    public ResponseEntity<Object> checkImei(@PathVariable Integer productId) {
        try {
            // Lấy chi tiết sản phẩm và IMEI
            List<ChiTietSanPham> chiTietList = sanPhamService.getChiTietSanPhamBySanPhamId(productId);

            return ResponseEntity.ok(Map.of(
                    "message", "IMEI check completed",
                    "productId", productId,
                    "chiTietCount", chiTietList.size(),
                    "chiTietList", chiTietList
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "message", "Lỗi khi kiểm tra IMEI"
            ));
        }
    }

    @GetMapping("/test-imei-simple")
    public ResponseEntity<Object> testImeiSimple() {
        try {
            // Test đơn giản để kiểm tra IMEI
            return ResponseEntity.ok(Map.of(
                    "message", "IMEI test endpoint working",
                    "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "message", "Lỗi khi test IMEI"
            ));
        }
    }

    @GetMapping("/{id}/debug")
    public ResponseEntity<Object> debugProduct(@PathVariable Integer id) {
        try {
            // Debug endpoint để kiểm tra dữ liệu sản phẩm
            var product = sanPhamService.getSanPhamForView(id);
            var chiTietList = sanPhamService.getChiTietSanPhamBySanPhamId(id);

            return ResponseEntity.ok(Map.of(
                    "message", "Debug product data",
                    "productId", id,
                    "product", product.orElse(null),
                    "chiTietList", chiTietList,
                    "chiTietCount", chiTietList.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "message", "Lỗi khi debug sản phẩm"
            ));
        }
    }
}
