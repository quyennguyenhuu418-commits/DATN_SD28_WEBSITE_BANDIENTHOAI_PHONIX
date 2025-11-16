package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.KhachHangDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface KhachHangService {
    List<KhachHangDTO> getAll();
    Optional<KhachHangDTO> getById(Integer id);
    KhachHangDTO save(KhachHangDTO khachHangDTO);
    KhachHangDTO update(Integer id, KhachHangDTO khachHangDTO);
    void delete(Integer id);
    
    // Search methods
    List<KhachHangDTO> searchByQuery(String query);
    List<KhachHangDTO> findByHoTenContaining(String hoTen);
    List<KhachHangDTO> findBySoDienThoaiContaining(String soDienThoai);
    
    // Statistics
    Long countActiveCustomers();
    List<KhachHangDTO> getActiveCustomers();
    
    // Validation methods
    Optional<KhachHangDTO> findByEmail(String email);
    Optional<KhachHangDTO> findBySoDienThoai(String soDienThoai);
    
    // Simple status update
    boolean updateStatusOnly(Integer id, Integer trangThai);
}





