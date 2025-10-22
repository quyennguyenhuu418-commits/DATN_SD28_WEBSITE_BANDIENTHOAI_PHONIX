package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.MauSac;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    Optional<MauSac> findByMaMau(String maMau);
    
    @Query("SELECT m FROM MauSac m WHERE m.trangThai = 1")
    List<MauSac> findAllActive();
    
    @Query("SELECT m FROM MauSac m WHERE m.trangThai = 1")
    Page<MauSac> findAllActive(Pageable pageable);
    
    @Query("SELECT m FROM MauSac m WHERE m.tenMau LIKE %:ten%")
    List<MauSac> findByTenMauContaining(@Param("ten") String ten);
    
    @Query("SELECT m FROM MauSac m WHERE m.maMau LIKE %:ma%")
    List<MauSac> findByMaMauContaining(@Param("ma") String ma);
}
