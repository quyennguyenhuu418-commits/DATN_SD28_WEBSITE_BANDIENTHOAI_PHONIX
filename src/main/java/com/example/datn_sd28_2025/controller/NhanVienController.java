package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.NhanVienDTO;
import com.example.datn_sd28_2025.dto.QRDataDTO;
import com.example.datn_sd28_2025.service.NhanVienService;
import com.example.datn_sd28_2025.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/nhan-vien")
@CrossOrigin(origins = "*")
public class NhanVienController {

    @Autowired
    private NhanVienService nhanVienService;
    
    @Autowired
    private QRCodeService qrCodeService;

    @GetMapping
    public ResponseEntity<List<NhanVienDTO>> getAllNhanVien() {
        try {
            List<NhanVienDTO> nhanVienList = nhanVienService.getAll();
            return ResponseEntity.ok(nhanVienList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NhanVienDTO> getNhanVienById(@PathVariable Integer id) {
        try {
            Optional<NhanVienDTO> nhanVien = nhanVienService.getById(id);
            return nhanVien.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createNhanVien(@RequestBody NhanVienDTO nhanVienDTO) {
        try {
            NhanVienDTO createdNhanVien = nhanVienService.save(nhanVienDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNhanVien);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi tạo nhân viên: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNhanVien(@PathVariable Integer id, @RequestBody NhanVienDTO nhanVienDTO) {
        try {
            NhanVienDTO updatedNhanVien = nhanVienService.update(id, nhanVienDTO);
            return ResponseEntity.ok(updatedNhanVien);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi cập nhật nhân viên: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNhanVien(@PathVariable Integer id) {
        try {
            nhanVienService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi xóa nhân viên: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<NhanVienDTO>> searchNhanVien(@RequestParam String query) {
        try {
            List<NhanVienDTO> results = nhanVienService.searchByQuery(query);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/chuc-vu/{chucVu}")
    public ResponseEntity<List<NhanVienDTO>> getNhanVienByChucVu(@PathVariable String chucVu) {
        try {
            List<NhanVienDTO> results = nhanVienService.findByChucVu(chucVu);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<NhanVienDTO>> getActiveNhanVien() {
        try {
            List<NhanVienDTO> results = nhanVienService.findAllActive();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/process-qr")
    public ResponseEntity<?> processQRCode(@RequestBody Map<String, String> request) {
        try {
            String qrText = request.get("qrText");
            if (qrText == null || qrText.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "QR text không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, String> cccdData = qrCodeService.parseCCCDData(qrText);
            
            if (cccdData.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không thể parse dữ liệu từ QR code");
                return ResponseEntity.badRequest().body(error);
            }

            // Convert to QRDataDTO
            QRDataDTO qrDataDTO = QRDataDTO.builder()
                    .cccd(cccdData.get("cccd"))
                    .hoTen(cccdData.get("hoTen"))
                    .ngaySinh(cccdData.get("ngaySinh"))
                    .gioiTinh(cccdData.get("gioiTinh"))
                    .diaChi(cccdData.get("diaChi"))
                    .build();

            return ResponseEntity.ok(qrDataDTO);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi xử lý QR code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/process-qr-image")
    public ResponseEntity<?> processQRImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, String> cccdData = qrCodeService.processCCCDQRCode(file);
            
            if (cccdData.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Không thể đọc dữ liệu từ ảnh QR code");
                return ResponseEntity.badRequest().body(error);
            }

            // Convert to QRDataDTO
            QRDataDTO qrDataDTO = QRDataDTO.builder()
                    .cccd(cccdData.get("cccd"))
                    .hoTen(cccdData.get("hoTen"))
                    .ngaySinh(cccdData.get("ngaySinh"))
                    .gioiTinh(cccdData.get("gioiTinh"))
                    .diaChi(cccdData.get("diaChi"))
                    .build();

            return ResponseEntity.ok(qrDataDTO);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi xử lý ảnh QR code: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/check-cccd/{cccd}")
    public ResponseEntity<Map<String, Boolean>> checkCccdExists(@PathVariable String cccd) {
        try {
            boolean exists = nhanVienService.existsByCccd(cccd);
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", false);
            return ResponseEntity.ok(response);
        }
    }
}




