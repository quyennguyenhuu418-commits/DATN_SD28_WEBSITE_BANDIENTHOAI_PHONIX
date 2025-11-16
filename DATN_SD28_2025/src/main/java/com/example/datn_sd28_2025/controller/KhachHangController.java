package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.KhachHangDTO;
import com.example.datn_sd28_2025.service.KhachHangService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = "*")
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;

    @GetMapping
    public ResponseEntity<List<KhachHangDTO>> getAll() {
        List<KhachHangDTO> khachHangs = khachHangService.getAll();
        return ResponseEntity.ok(khachHangs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhachHangDTO> getById(@PathVariable Integer id) {
        Optional<KhachHangDTO> khachHang = khachHangService.getById(id);
        return khachHang.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<KhachHangDTO>> searchByQuery(@RequestParam String query) {
        List<KhachHangDTO> khachHangs = khachHangService.searchByQuery(query);
        return ResponseEntity.ok(khachHangs);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody KhachHangDTO khachHangDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errors);
        }
        
        try {
            // Tạm thời bỏ qua duplicate check để tránh lỗi query
            // TODO: Fix duplicate data trong database trước khi enable lại
            /*
            // Kiểm tra email trùng lặp (email bắt buộc)
            if (khachHangService.findByEmail(khachHangDTO.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("email", "Email đã tồn tại trong hệ thống"));
            }
            
            // Kiểm tra số điện thoại trùng lặp
            if (khachHangService.findBySoDienThoai(khachHangDTO.getSoDienThoai()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("soDienThoai", "Số điện thoại đã tồn tại trong hệ thống"));
            }
            */
            
            KhachHangDTO created = khachHangService.save(khachHangDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            System.err.println("Error creating customer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi tạo khách hàng: " + e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<KhachHangDTO>> getActiveCustomers() {
        List<KhachHangDTO> activeCustomers = khachHangService.getActiveCustomers();
        return ResponseEntity.ok(activeCustomers);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody KhachHangDTO khachHangDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errors);
        }
        
        try {
            System.out.println("Updating customer with ID: " + id);
            System.out.println("Received data: " + khachHangDTO);
            
            // Kiểm tra email trùng lặp (trừ khách hàng hiện tại)
            Optional<KhachHangDTO> existingByEmail = khachHangService.findByEmail(khachHangDTO.getEmail());
            if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("email", "Email đã tồn tại trong hệ thống"));
            }
            
            // Kiểm tra số điện thoại trùng lặp (trừ khách hàng hiện tại)
            Optional<KhachHangDTO> existingByPhone = khachHangService.findBySoDienThoai(khachHangDTO.getSoDienThoai());
            if (existingByPhone.isPresent() && !existingByPhone.get().getId().equals(id)) {
                return ResponseEntity.badRequest().body(Map.of("soDienThoai", "Số điện thoại đã tồn tại trong hệ thống"));
            }
            
            KhachHangDTO updated = khachHangService.update(id, khachHangDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            System.err.println("Error updating customer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi cập nhật khách hàng: " + e.getMessage()));
        }
    }

    // Simple toggle status endpoint
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            System.out.println("=== TOGGLE CUSTOMER STATUS ===");
            System.out.println("Customer ID: " + id);
            System.out.println("Request body: " + request);
            
            Integer newStatus = request.get("trangThai");
            if (newStatus == null || (newStatus != 0 && newStatus != 1)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Trạng thái không hợp lệ. Chỉ chấp nhận 0 hoặc 1"));
            }
            
            // Update only status using repository directly
            boolean updated = khachHangService.updateStatusOnly(id, newStatus);
            if (!updated) {
                return ResponseEntity.notFound().build();
            }
            
            System.out.println("Status updated successfully: " + newStatus);
            return ResponseEntity.ok(Map.of("id", id, "trangThai", newStatus, "message", "Cập nhật trạng thái thành công"));
            
        } catch (Exception e) {
            System.err.println("Error toggling customer status: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi cập nhật trạng thái: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            khachHangService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveCustomers() {
        Long count = khachHangService.countActiveCustomers();
        return ResponseEntity.ok(count);
    }
}





