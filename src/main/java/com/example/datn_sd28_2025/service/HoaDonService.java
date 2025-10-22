package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.HoaDonDTO;
import com.example.datn_sd28_2025.dto.HoaDonTrackingDTO;
import com.example.datn_sd28_2025.dto.HoaDonSearchRequestDTO;
import com.example.datn_sd28_2025.dto.PosOrderRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface HoaDonService {

    List<HoaDonDTO> getAll();

    HoaDonDTO getById(Integer id);

    HoaDonDTO getByMaHoaDon(String maHoaDon);

    List<HoaDonDTO> getByKhachHangId(Integer khachHangId);

    List<HoaDonDTO> getByTrangThai(Integer trangThai);

    List<HoaDonDTO> getByLoaiHoaDon(String loaiHoaDon);

    List<HoaDonDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Page<HoaDonDTO> searchHoaDon(String keyword, Pageable pageable);

    HoaDonDTO createOrder(PosOrderRequest request);

    HoaDonDTO updateHoaDon(Integer id, HoaDonDTO hoaDonDTO);

    HoaDonDTO updateTrangThai(Integer id, Integer trangThai);

    void deleteHoaDon(Integer id);

    // Thống kê
    Long countByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Double getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    // Advanced search
    Page<HoaDonDTO> searchHoaDonAdvanced(HoaDonSearchRequestDTO searchRequest);

    // Tracking
    HoaDonTrackingDTO getTrackingInfo(String maHoaDon);

    // Update sample data for testing
    void updateSampleDataWithDiverseStatuses();

    // Get products of a specific invoice
    List<Map<String, Object>> getHoaDonProducts(Integer hoaDonId);

    // Update status by maHoaDon
    HoaDonDTO updateTrangThaiByMaHoaDon(String maHoaDon, Integer trangThai);
}