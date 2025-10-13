package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Hang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HangRepository extends JpaRepository<Hang, Integer> {
    Optional<Hang> findByTen(String ten);
    
    @Query("SELECT h FROM Hang h WHERE h.trangThai = 1")
    List<Hang> findAllActive();
    
    @Query("SELECT h FROM Hang h WHERE h.trangThai = 1")
    Page<Hang> findAllActive(Pageable pageable);
    
    @Query("SELECT h FROM Hang h WHERE h.ten LIKE %:ten%")
    List<Hang> findByTenContaining(@Param("ten") String ten);
    
    @Query("SELECT h FROM Hang h WHERE h.xuatXu LIKE %:xuatXu%")
    List<Hang> findByXuatXuContaining(@Param("xuatXu") String xuatXu);
}
