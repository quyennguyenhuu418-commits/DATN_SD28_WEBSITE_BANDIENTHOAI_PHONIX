package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.LichSuXuLyBaoHanh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuXuLyBaoHanhRepository extends JpaRepository<LichSuXuLyBaoHanh, Integer> {
    @Query("SELECT l FROM LichSuXuLyBaoHanh l WHERE l.phieuBaoHanh.id = :phieuBaoHanhId ORDER BY l.thoiGian DESC")
    List<LichSuXuLyBaoHanh> findByPhieuBaoHanhId(@Param("phieuBaoHanhId") Integer phieuBaoHanhId);
}

