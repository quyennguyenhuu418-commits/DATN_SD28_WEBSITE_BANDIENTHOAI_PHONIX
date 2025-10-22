package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Chip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChipRepository extends JpaRepository<Chip, Integer> {
    Optional<Chip> findByMaChip(String maChip);
    
    @Query("SELECT c FROM Chip c WHERE c.trangThai = 1")
    List<Chip> findAllActive();
    
    @Query("SELECT c FROM Chip c WHERE c.trangThai = 1")
    Page<Chip> findAllActive(Pageable pageable);
    
    @Query("SELECT c FROM Chip c WHERE c.tenChip LIKE %:ten%")
    List<Chip> findByTenChipContaining(@Param("ten") String ten);
    
    @Query("SELECT c FROM Chip c WHERE c.maChip LIKE %:ma%")
    List<Chip> findByMaChipContaining(@Param("ma") String ma);
}
