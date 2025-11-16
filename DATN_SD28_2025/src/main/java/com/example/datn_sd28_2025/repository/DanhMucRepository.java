package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer> {
    Optional<DanhMuc> findByMaDanhMuc(String maDanhMuc);
    Optional<DanhMuc> findByTenDanhMuc(String tenDanhMuc);
    
    @Query("SELECT d FROM DanhMuc d WHERE d.trangThai = 1")
    List<DanhMuc> findAllActive();
}
