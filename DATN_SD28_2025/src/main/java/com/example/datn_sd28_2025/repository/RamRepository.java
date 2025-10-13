package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Ram;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RamRepository extends JpaRepository<Ram, Integer> {
    Optional<Ram> findByMaRam(String maRam);
    
    @Query("SELECT r FROM Ram r WHERE r.trangThai = 1")
    List<Ram> findAllActive();
    
    @Query("SELECT r FROM Ram r WHERE r.trangThai = 1")
    Page<Ram> findAllActive(Pageable pageable);
    
    @Query("SELECT r FROM Ram r WHERE r.tenRam LIKE %:ten%")
    List<Ram> findByTenRamContaining(@Param("ten") String ten);
    
    @Query("SELECT r FROM Ram r WHERE r.maRam LIKE %:ma%")
    List<Ram> findByMaRamContaining(@Param("ma") String ma);
}
