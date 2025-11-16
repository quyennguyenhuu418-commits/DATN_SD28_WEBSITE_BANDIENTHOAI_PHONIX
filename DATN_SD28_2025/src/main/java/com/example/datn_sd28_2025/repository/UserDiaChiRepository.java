package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.UserDiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDiaChiRepository extends JpaRepository<UserDiaChi, Integer> {
    
    @Query("SELECT udc FROM UserDiaChi udc WHERE udc.khachHang.id = :idKhachHang AND udc.trangThai = 1")
    List<UserDiaChi> findByKhachHangIdAndTrangThai(@Param("idKhachHang") Integer idKhachHang);
    
    @Query("SELECT udc FROM UserDiaChi udc WHERE udc.khachHang.id = :idKhachHang AND udc.macDinh = true AND udc.trangThai = 1")
    UserDiaChi findDiaChiMacDinhByKhachHangId(@Param("idKhachHang") Integer idKhachHang);
    
    @Query("SELECT udc FROM UserDiaChi udc WHERE udc.khachHang.id = :idKhachHang AND udc.loaiDiaChi = :loaiDiaChi AND udc.trangThai = 1")
    List<UserDiaChi> findByKhachHangIdAndLoaiDiaChi(@Param("idKhachHang") Integer idKhachHang, @Param("loaiDiaChi") String loaiDiaChi);
}

