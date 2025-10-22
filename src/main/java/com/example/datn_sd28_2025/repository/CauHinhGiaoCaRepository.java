package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.CauHinhGiaoCa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CauHinhGiaoCaRepository extends JpaRepository<CauHinhGiaoCa, Integer> {
    
    Optional<CauHinhGiaoCa> findByTenCauHinh(String tenCauHinh);
    
    List<CauHinhGiaoCa> findByTrangThai(Integer trangThai);
    
    List<CauHinhGiaoCa> findByLoaiCauHinh(String loaiCauHinh);
    
    @Query("SELECT chgc FROM CauHinhGiaoCa chgc WHERE chgc.trangThai = 1 ORDER BY chgc.tenCauHinh")
    List<CauHinhGiaoCa> findActiveConfigurations();
    
    @Query("SELECT chgc FROM CauHinhGiaoCa chgc WHERE chgc.tenCauHinh LIKE %:tenCauHinh% AND chgc.trangThai = :trangThai")
    List<CauHinhGiaoCa> findByTenCauHinhContainingAndTrangThai(@Param("tenCauHinh") String tenCauHinh, 
                                                               @Param("trangThai") Integer trangThai);
    
    @Query("SELECT chgc FROM CauHinhGiaoCa chgc WHERE chgc.loaiCauHinh = :loaiCauHinh AND chgc.trangThai = 1 ORDER BY chgc.tenCauHinh")
    List<CauHinhGiaoCa> findActiveByLoaiCauHinh(@Param("loaiCauHinh") String loaiCauHinh);
    
    boolean existsByTenCauHinh(String tenCauHinh);
    
    boolean existsByTenCauHinhAndIdNot(String tenCauHinh, Integer id);
}

























