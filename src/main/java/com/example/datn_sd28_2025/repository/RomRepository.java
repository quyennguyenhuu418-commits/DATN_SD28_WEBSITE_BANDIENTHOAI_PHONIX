package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Rom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RomRepository extends JpaRepository<Rom, Integer> {
    Optional<Rom> findByMaRom(String maRom);
    
    @Query("SELECT r FROM Rom r WHERE r.trangThai = 1")
    List<Rom> findAllActive();
    
    @Query("SELECT r FROM Rom r WHERE r.trangThai = 1")
    Page<Rom> findAllActive(Pageable pageable);
    
    @Query("SELECT r FROM Rom r WHERE r.dungLuong LIKE %:dungLuong%")
    List<Rom> findByDungLuongContaining(@Param("dungLuong") String dungLuong);
    
    @Query("SELECT r FROM Rom r WHERE r.maRom LIKE %:ma%")
    List<Rom> findByMaRomContaining(@Param("ma") String ma);
}
