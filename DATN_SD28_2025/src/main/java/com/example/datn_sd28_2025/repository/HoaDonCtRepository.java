package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.HoaDonCt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoaDonCtRepository extends JpaRepository<HoaDonCt, Integer> {

    @Query("SELECT h FROM HoaDonCt h WHERE h.hoaDon.id = :idHoaDon")
    List<HoaDonCt> findByIdHoaDon(@Param("idHoaDon") Integer idHoaDon);

    @Query("SELECT h FROM HoaDonCt h WHERE h.chiTietSanPham.id = :idCtsp")
    List<HoaDonCt> findByIdCtsp(@Param("idCtsp") Integer idCtsp);

    @Query("SELECT h FROM HoaDonCt h WHERE h.trangThai = :trangThai")
    List<HoaDonCt> findByTrangThai(@Param("trangThai") Integer trangThai);
}



