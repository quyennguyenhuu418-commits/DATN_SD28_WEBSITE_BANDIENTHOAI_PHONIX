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
import java.util.ArrayList;

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

    // Strict price filter using variant min price from database
    @GetMapping("/price-strict")
    public ResponseEntity<?> getByStrictPrice(
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice) {
        try {
            List<SanPhamDTO> all = sanPhamService.getActiveSanPham();
            java.util.List<SanPhamDTO> result = new java.util.ArrayList<>();
            java.util.List<java.util.Map<String, Object>> debug = new java.util.ArrayList<>();
            for (SanPhamDTO sp : all) {
                var optView = ((com.example.datn_sd28_2025.service.impl.SanPhamServiceImpl) sanPhamService).getSanPhamForView(sp.getId());
                if (optView.isPresent()) {
                    var view = optView.get();
                    Long min = null;
                    if (view.getVariants() != null) {
                        for (var v : view.getVariants()) {
                            if (v.getDonGia() != null) {
                                min = (min == null) ? v.getDonGia() : Math.min(min, v.getDonGia());
                            }
                        }
                    }
                    debug.add(java.util.Map.of(
                            "id", sp.getId(),
                            "name", sp.getTenSanPham(),
                            "minVariantPrice", min
                    ));
                    boolean ok = true;
                    if (minPrice != null) ok = ok && (min != null && min >= minPrice);
                    if (maxPrice != null) ok = ok && (min != null && min <= maxPrice);
                    if (ok) result.add(sp);
                }
            }
            // Sort products by minVariantPrice ascending to surface rẻ nhất trước
            java.util.Map<Integer, Long> idToMin = new java.util.HashMap<>();
            for (var d : debug) {
                idToMin.put((Integer) d.get("id"), (Long) d.get("minVariantPrice"));
            }
            result.sort((a,b) -> {
                Long va = idToMin.get(a.getId());
                Long vb = idToMin.get(b.getId());
                if (va == null && vb == null) return 0;
                if (va == null) return 1;
                if (vb == null) return -1;
                return va.compareTo(vb);
            });
            return ResponseEntity.ok(java.util.Map.of(
                    "products", result,
                    "debug", debug
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/search-advanced")
    public ResponseEntity<List<SanPhamDTO>> searchAdvanced(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) String chip,
            @RequestParam(required = false) Integer ramId,
            @RequestParam(required = false) Integer romId,
            @RequestParam(required = false, name = "os") String osName,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) String brandName,
            @RequestParam(required = false) String gpu,
            @RequestParam(required = false) String cpuName,
            @RequestParam(required = false) String simType,
            @RequestParam(required = false) String minBattery,
            @RequestParam(required = false) String maxBattery,
            @RequestParam(required = false) String minScreen,
            @RequestParam(required = false) String maxScreen,
            @RequestParam(required = false) String rearCam,
            @RequestParam(required = false) String frontCam
    ) {
        java.math.BigDecimal min = minPrice != null ? java.math.BigDecimal.valueOf(minPrice) : null;
        java.math.BigDecimal max = maxPrice != null ? java.math.BigDecimal.valueOf(maxPrice) : null;
        List<SanPhamDTO> result = ((com.example.datn_sd28_2025.service.impl.SanPhamServiceImpl) sanPhamService)
                .searchAdvanced(q, min, max, chip, ramId, romId, osName, brandId, brandName, gpu, cpuName, simType,
                        minBattery, maxBattery, minScreen, maxScreen, rearCam, frontCam);
        return ResponseEntity.ok(result);
    }

    // Compare multiple products by ids and return normalized specs for UI comparison
    @GetMapping("/compare")
    public ResponseEntity<Map<String, Object>> compare(@RequestParam("ids") List<Integer> ids) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (Integer id : ids) {
            Optional<SanPhamViewDTO> opt = sanPhamService.getSanPhamForView(id);
            if (opt.isPresent()) {
                SanPhamViewDTO v = opt.get();
                // Derive min price and first image from variants
                Long minPrice = null;
                Long maxPrice = null;
                String image = null;
                java.util.List<String> variantSummaries = new java.util.ArrayList<>();
                String bestRam = null;
                if (v.getVariants() != null && !v.getVariants().isEmpty()) {
                    for (SanPhamViewDTO.VariantViewDTO vi : v.getVariants()) {
                        if (vi.getDonGia() != null) {
                            minPrice = (minPrice == null) ? vi.getDonGia() : Math.min(minPrice, vi.getDonGia());
                            maxPrice = (maxPrice == null) ? vi.getDonGia() : Math.max(maxPrice, vi.getDonGia());
                        }
                        if (image == null && vi.getImageUrls() != null && !vi.getImageUrls().isEmpty()) {
                            image = vi.getImageUrls().get(0);
                        }
                        String varLabel = String.format("%s/%s %s", 
                                vi.getTenRam() != null ? vi.getTenRam() : "",
                                vi.getTenRom() != null ? vi.getTenRom() : "",
                                vi.getTenMauSac() != null ? vi.getTenMauSac() : "").trim();
                        if (!varLabel.isBlank()) variantSummaries.add(varLabel);
                        // track best RAM
                        if (vi.getTenRam() != null) {
                            if (bestRam == null || extractFirstNumber(vi.getTenRam()) > extractFirstNumber(bestRam)) {
                                bestRam = vi.getTenRam();
                            }
                        }
                    }
                }
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("id", v.getId());
                item.put("name", v.getTenSanPham());
                item.put("imageUrl", image);
                item.put("price", minPrice);
                if (minPrice != null && maxPrice != null && !minPrice.equals(maxPrice)) {
                    item.put("giaThamKhao", String.format("%d – %d", minPrice, maxPrice));
                } else if (minPrice != null) {
                    item.put("giaThamKhao", String.valueOf(minPrice));
                }
                // Main specs
                item.put("tenHang", v.getTenHang());
                item.put("tenChip", v.getTenChip());
                item.put("tenManHinh", v.getTenManHinh());
                item.put("tenCameraSau", v.getTenCameraSau());
                item.put("tenCameraTruoc", v.getTenCameraTruoc());
                item.put("tenPin", v.getTenPin());
                item.put("tenHeDieuHanh", v.getTenHeDieuHanh());
                item.put("tenCpu", v.getTenCpu());
                item.put("tenGpu", v.getTenGpu());
                item.put("tenSim", v.getTenSim());
                if (bestRam != null) item.put("tenRam", bestRam);
                item.put("variantSummaries", variantSummaries);
                results.add(item);
            }
        }
        // Winners per spec
        java.util.Map<String, String> winners = new java.util.HashMap<>();
        if (results.size() >= 2) {
            java.util.Map<String, Object> a = results.get(0);
            java.util.Map<String, Object> b = results.get(1);
            winners.put("giaThamKhao", betterByPrice((String)a.get("giaThamKhao"),(String)b.get("giaThamKhao"), a.get("name"), b.get("name")));
            winners.put("tenManHinh", betterByNumber((String)a.get("tenManHinh"),(String)b.get("tenManHinh"), a.get("name"), b.get("name")));
            winners.put("tenPin", betterByNumber((String)a.get("tenPin"),(String)b.get("tenPin"), a.get("name"), b.get("name")));
            winners.put("tenCameraSau", betterByMp((String)a.get("tenCameraSau"),(String)b.get("tenCameraSau"), a.get("name"), b.get("name")));
            winners.put("tenCameraTruoc", betterByMp((String)a.get("tenCameraTruoc"),(String)b.get("tenCameraTruoc"), a.get("name"), b.get("name")));
            winners.put("tenChip", betterByChip((String)a.get("tenChip"),(String)b.get("tenChip"), a.get("name"), b.get("name")));
            winners.put("tenCpu", betterByString((String)a.get("tenCpu"),(String)b.get("tenCpu"), a.get("name"), b.get("name")));
            winners.put("tenGpu", betterByString((String)a.get("tenGpu"),(String)b.get("tenGpu"), a.get("name"), b.get("name")));
            winners.put("tenSim", betterByString((String)a.get("tenSim"),(String)b.get("tenSim"), a.get("name"), b.get("name")));
            winners.put("tenRam", betterByNumber((String)a.get("tenRam"),(String)b.get("tenRam"), a.get("name"), b.get("name")));
        }
        Integer winnerId = null; String winnerName = null;
        if (results.size() >= 2) {
            // Prefer chip comparison for overall verdict
            String chipWinner = winners.get("tenChip");
            if (chipWinner != null && !chipWinner.equals("Tie")) {
                for (java.util.Map<String,Object> r : results) {
                    if (chipWinner.equals(r.get("name"))) { winnerId = (Integer) r.get("id"); winnerName = chipWinner; break; }
                }
            }
        }
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("products", results);
        payload.put("winnerId", winnerId);
        payload.put("winners", winners);
        payload.put("verdict", (winnerName != null ? ("\uD83C\uDFC6 " + winnerName + " nhỉnh hơn dựa trên các thông số chính.") : ""));

        // Quick, human-friendly review suggestions per product (rule-based)
        java.util.Map<String, String> quickReviews = new java.util.HashMap<>();
        if (results.size() >= 2) {
            java.util.Map<String, Object> p1 = results.get(0);
            java.util.Map<String, Object> p2 = results.get(1);
            quickReviews.put(String.valueOf(p1.get("name")), buildQuickReview(p1));
            quickReviews.put(String.valueOf(p2.get("name")), buildQuickReview(p2));
        } else if (results.size() == 1) {
            java.util.Map<String, Object> p = results.get(0);
            quickReviews.put(String.valueOf(p.get("name")), buildQuickReview(p));
        }
        payload.put("quickReviews", quickReviews);
        return ResponseEntity.ok(payload);
    }

    // --- Simple scoring helpers ---
    private int scoreChip(String chip) {
        if (chip == null) return 0;
        String c = chip.toLowerCase();
        if (c.contains("8 gen 3") || c.contains("a17") || c.contains("dimensity 9200") || c.contains("exynos 2400")) return 100;
        if (c.contains("8 gen") || c.contains("a16") || c.contains("888") || c.contains("8+")) return 90;
        if (c.contains("snapdragon 7") || c.matches(".*\\b7\\d{2}\\b.*")) return 70;
        if (c.contains("snapdragon 6") || c.contains("665")) return 50;
        if (c.contains("snapdragon 4") || c.contains("4 gen") || c.contains("4xx")) return 35;
        return 40;
    }

    private int scoreBattery(String pin) {
        if (pin == null) return 0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\\\d{3,5})").matcher(pin);
        int val = 0;
        if (m.find()) {
            try { val = Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
        }
        if (val >= 6000) return 100;
        if (val >= 5000) return 85;
        if (val >= 4500) return 70;
        if (val >= 4000) return 55;
        return 40;
    }

    private int scoreCamera(String cam) {
        if (cam == null) return 0;
        String c = cam.toLowerCase();
        if (c.contains("200mp")) return 100;
        if (c.contains("108mp")) return 90;
        if (c.contains("64mp") || c.contains("50mp")) return 75;
        if (c.contains("48mp")) return 65;
        return 50;
    }

    private String betterByNumber(String a, String b, Object nameA, Object nameB) {
        double va = extractFirstNumber(a);
        double vb = extractFirstNumber(b);
        if (va == 0 && vb == 0) return "Tie";
        return va >= vb ? String.valueOf(nameA) : String.valueOf(nameB);
    }

    private String betterByMp(String a, String b, Object nameA, Object nameB) {
        int va = (int) Math.round(extractFirstNumber(a));
        int vb = (int) Math.round(extractFirstNumber(b));
        if (va == 0 && vb == 0) return "Tie";
        return va >= vb ? String.valueOf(nameA) : String.valueOf(nameB);
    }

    private String betterByChip(String a, String b, Object nameA, Object nameB) {
        int sa = scoreChip(a);
        int sb = scoreChip(b);
        if (sa == sb) return "Tie";
        return sa > sb ? String.valueOf(nameA) : String.valueOf(nameB);
    }

    private String betterByString(String a, String b, Object nameA, Object nameB) {
        if (a == null && b == null) return "Tie";
        if (a != null && (b == null || a.length() >= b.length())) return String.valueOf(nameA);
        return String.valueOf(nameB);
    }

    private double extractFirstNumber(String s) {
        if (s == null) return 0;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\\\d+(?:[.,]\\\\d+)?)").matcher(s);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1).replace(',', '.')); } catch (Exception ignored) {}
        }
        return 0;
    }

    private String betterByPrice(String a, String b, Object nameA, Object nameB) {
        // Lower is better for price
        double va = extractFirstNumber(a);
        double vb = extractFirstNumber(b);
        if (va == 0 && vb == 0) return "Tie";
        return va <= vb ? String.valueOf(nameA) : String.valueOf(nameB);
    }

    private String buildQuickReview(java.util.Map<String, Object> p) {
        String name = String.valueOf(p.get("name"));
        String os = (String) p.get("tenHeDieuHanh");
        String screen = (String) p.get("tenManHinh");
        String hz = extractFirstNumber(screen) > 0 ? (screen.toLowerCase().contains("120hz") ? "120Hz" : (screen.toLowerCase().contains("90hz") ? "90Hz" : null)) : null;
        String chip = (String) p.get("tenChip");
        String pin = (String) p.get("tenPin");
        String camera = (String) p.get("tenCameraSau");
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(": ");
        if (chip != null) sb.append("hiệu năng tốt (" + chip + ")");
        if (hz != null) sb.append(", màn hình mượt " + hz);
        if (camera != null) sb.append(", camera đa năng (" + camera + ")");
        if (pin != null) sb.append(", pin " + pin);
        if (os != null) {
            if (os.toLowerCase().contains("ios")) sb.append(", hệ sinh thái Apple mượt, hỗ trợ lâu dài");
            else sb.append(", Android linh hoạt, tuỳ biến cao");
        }
        return sb.toString();
    }

    // Expose view DTO including variants (prices, images)
    // Note: keep only one mapping for /{id}/view to avoid ambiguity

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
            // Trả về thông báo lỗi chi tiết thay vì 404
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Cập nhật sản phẩm thất bại",
                "message", e.getMessage(),
                "timestamp", java.time.LocalDateTime.now()
            ));
        } catch (Exception e) {
            // Xử lý các lỗi khác
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

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            Integer trangThai = request.get("trangThai");
            if (trangThai == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "trangThai is required"));
            }
            sanPhamService.updateStatus(id, trangThai);
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
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
}
