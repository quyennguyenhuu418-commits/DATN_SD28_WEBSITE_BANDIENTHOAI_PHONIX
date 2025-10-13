package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.HinhAnh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HinhAnhRepository extends JpaRepository<HinhAnh, Integer> {
    List<HinhAnh> findByChiTietSanPhamId(Integer chiTietSanPhamId);
    
    @Query("SELECT h FROM HinhAnh h WHERE h.trangThai = 1")
    List<HinhAnh> findAllActive();
    
    @Query("SELECT h FROM HinhAnh h WHERE h.trangThai = 1")
    Page<HinhAnh> findAllActive(Pageable pageable);
    
    @Query("SELECT h FROM HinhAnh h WHERE h.chiTietSanPham.id = :idCtsp AND h.trangThai = 1")
    List<HinhAnh> findActiveByIdCtsp(@Param("idCtsp") Integer idCtsp);
    
    @Query("SELECT h FROM HinhAnh h WHERE h.urlAnh LIKE %:url%")
    List<HinhAnh> findByUrlAnhContaining(@Param("url") String url);
}
