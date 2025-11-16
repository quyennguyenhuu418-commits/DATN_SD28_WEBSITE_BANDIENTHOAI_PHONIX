package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.PhieuBaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuBaoHanhRepository extends JpaRepository<PhieuBaoHanh, Integer> {
    Optional<PhieuBaoHanh> findByMaPhieu(String maPhieu);

    List<PhieuBaoHanh> findByKhachHangId(Integer khachHangId);

    List<PhieuBaoHanh> findByTrangThai(Integer trangThai);

    List<PhieuBaoHanh> findByImeiSerial(String imeiSerial);

    @Query("SELECT p FROM PhieuBaoHanh p WHERE " +
           "(:maPhieu IS NULL OR p.maPhieu LIKE %:maPhieu%) AND " +
           "(:tenKhachHang IS NULL OR p.tenKhachHang LIKE %:tenKhachHang%) AND " +
           "(:soDienThoai IS NULL OR p.soDienThoai LIKE %:soDienThoai%) AND " +
           "(:imeiSerial IS NULL OR p.imeiSerial LIKE %:imeiSerial%) AND " +
           "(:trangThai IS NULL OR p.trangThai = :trangThai) AND " +
           "(:ngayNhanTu IS NULL OR p.ngayNhan >= :ngayNhanTu) AND " +
           "(:ngayNhanDen IS NULL OR p.ngayNhan <= :ngayNhanDen)")
    List<PhieuBaoHanh> search(
            @Param("maPhieu") String maPhieu,
            @Param("tenKhachHang") String tenKhachHang,
            @Param("soDienThoai") String soDienThoai,
            @Param("imeiSerial") String imeiSerial,
            @Param("trangThai") Integer trangThai,
            @Param("ngayNhanTu") LocalDate ngayNhanTu,
            @Param("ngayNhanDen") LocalDate ngayNhanDen
    );
}

