package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.UserDiaChiDTO;
import com.example.datn_sd28_2025.service.UserDiaChiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user-dia-chi")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserDiaChiController {
    
    private final UserDiaChiService userDiaChiService;
    
    @GetMapping("/khach-hang/{idKhachHang}")
    public ResponseEntity<List<UserDiaChiDTO>> getByKhachHangId(@PathVariable Integer idKhachHang) {
        List<UserDiaChiDTO> addresses = userDiaChiService.getByKhachHangId(idKhachHang);
        return ResponseEntity.ok(addresses);
    }
    
    @GetMapping("/khach-hang/{idKhachHang}/mac-dinh")
    public ResponseEntity<UserDiaChiDTO> getDiaChiMacDinh(@PathVariable Integer idKhachHang) {
        UserDiaChiDTO address = userDiaChiService.getDiaChiMacDinhByKhachHangId(idKhachHang);
        return address != null ? ResponseEntity.ok(address) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/khach-hang/{idKhachHang}/loai/{loaiDiaChi}")
    public ResponseEntity<List<UserDiaChiDTO>> getByLoaiDiaChi(@PathVariable Integer idKhachHang, @PathVariable String loaiDiaChi) {
        List<UserDiaChiDTO> addresses = userDiaChiService.getByKhachHangIdAndLoaiDiaChi(idKhachHang, loaiDiaChi);
        return ResponseEntity.ok(addresses);
    }
    
    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserDiaChiDTO userDiaChiDTO) {
        try {
            UserDiaChiDTO created = userDiaChiService.create(userDiaChiDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            System.err.println("Error creating UserDiaChi: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Lỗi khi tạo địa chỉ: " + e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDiaChiDTO> update(@PathVariable Integer id, @RequestBody UserDiaChiDTO userDiaChiDTO) {
        try {
            UserDiaChiDTO updated = userDiaChiService.update(id, userDiaChiDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            userDiaChiService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{id}/mac-dinh")
    public ResponseEntity<UserDiaChiDTO> setDiaChiMacDinh(@PathVariable Integer id) {
        try {
            UserDiaChiDTO updated = userDiaChiService.setDiaChiMacDinh(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

