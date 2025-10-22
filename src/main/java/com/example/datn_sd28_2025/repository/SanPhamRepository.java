package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    Optional<SanPham> findByMaSanPham(String maSanPham);
    
    @Query("SELECT s FROM SanPham s LEFT JOIN FETCH s.danhMuc LEFT JOIN FETCH s.hang WHERE s.trangThai = 1")
    List<SanPham> findAllActive();
    
    @Query("SELECT s FROM SanPham s LEFT JOIN FETCH s.danhMuc LEFT JOIN FETCH s.hang WHERE s.danhMuc.id = :danhMucId AND s.trangThai = 1")
    List<SanPham> findByDanhMucIdAndTrangThaiTrue(@Param("danhMucId") Integer danhMucId);
    
    @Query("SELECT s FROM SanPham s LEFT JOIN FETCH s.danhMuc LEFT JOIN FETCH s.hang WHERE s.hang.id = :hangId AND s.trangThai = 1")
    List<SanPham> findByHangIdAndTrangThaiTrue(@Param("hangId") Integer hangId);
    
    // Broad search over name, code and brand (case-insensitive)
    @Query("SELECT s FROM SanPham s LEFT JOIN s.hang h WHERE s.trangThai = 1 AND (LOWER(s.tenSanPham) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(s.maSanPham) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(h.ten) LIKE LOWER(CONCAT('%', :kw, '%'))) ")
    List<SanPham> searchBroad(@Param("kw") String kw);

    // Advanced filtered search across relations and price range
    @Query("""
        SELECT s FROM SanPham s
        LEFT JOIN s.hang h
        LEFT JOIN s.chip ch
        LEFT JOIN s.gpu gp
        LEFT JOIN s.cpu cpu
        LEFT JOIN s.sim sim
        LEFT JOIN s.manHinh mh
        LEFT JOIN s.cameraSau camSau
        LEFT JOIN s.cameraTruoc camTruoc
        LEFT JOIN s.pin pn
        LEFT JOIN s.heDieuHanh os
        LEFT JOIN ChiTietSanPham ct ON ct.sanPham = s AND ct.trangThai = 1
        LEFT JOIN ct.ram r
        LEFT JOIN ct.rom rm
        WHERE s.trangThai = 1
          AND (:kw IS NULL OR :kw = '' OR 
               LOWER(s.tenSanPham) LIKE LOWER(CONCAT('%', :kw, '%')) OR 
               LOWER(s.maSanPham) LIKE LOWER(CONCAT('%', :kw, '%')) OR 
               LOWER(h.ten) LIKE LOWER(CONCAT('%', :kw, '%')) OR
               LOWER(ch.tenChip) LIKE LOWER(CONCAT('%', :kw, '%')) OR
               LOWER(cpu.tenCpu) LIKE LOWER(CONCAT('%', :kw, '%')) OR
               LOWER(gp.tenGpu) LIKE LOWER(CONCAT('%', :kw, '%')) OR
               LOWER(os.tenHeDieuHanh) LIKE LOWER(CONCAT('%', :kw, '%')))
          AND (:brandId IS NULL OR h.id = :brandId)
          AND (:brandName IS NULL OR :brandName = '' OR LOWER(h.ten) LIKE LOWER(CONCAT('%', :brandName, '%')))
          AND (:chip IS NULL OR :chip = '' OR LOWER(ch.tenChip) LIKE LOWER(CONCAT('%', :chip, '%')))
          AND (:gpu IS NULL OR :gpu = '' OR LOWER(gp.tenGpu) LIKE LOWER(CONCAT('%', :gpu, '%')))
          AND (:cpuName IS NULL OR :cpuName = '' OR LOWER(cpu.tenCpu) LIKE LOWER(CONCAT('%', :cpuName, '%')))
          AND (:simType IS NULL OR :simType = '' OR LOWER(sim.loaiSim) LIKE LOWER(CONCAT('%', :simType, '%')))
          AND (:ramId IS NULL OR r.id = :ramId)
          AND (:romId IS NULL OR rm.id = :romId)
          AND (:osName IS NULL OR :osName = '' OR LOWER(os.tenHeDieuHanh) LIKE LOWER(CONCAT('%', :osName, '%')))
          /* Battery and screen may be stored as NVARCHAR in some databases; use textual contains to avoid conversion errors */
          AND (:minBattery IS NULL OR (pn.dungLuongPin IS NOT NULL AND LOWER(pn.dungLuongPin) LIKE LOWER(CONCAT('%', :minBattery, '%'))))
          AND (:maxBattery IS NULL OR (pn.dungLuongPin IS NOT NULL AND LOWER(pn.dungLuongPin) LIKE LOWER(CONCAT('%', :maxBattery, '%'))))
          AND (:minScreen IS NULL OR (mh.kichThuoc IS NOT NULL AND LOWER(mh.kichThuoc) LIKE LOWER(CONCAT('%', :minScreen, '%'))))
          AND (:maxScreen IS NULL OR (mh.kichThuoc IS NOT NULL AND LOWER(mh.kichThuoc) LIKE LOWER(CONCAT('%', :maxScreen, '%'))))
          AND (:rearCam IS NULL OR :rearCam = '' OR LOWER(camSau.thongSo) LIKE LOWER(CONCAT('%', :rearCam, '%')))
          AND (:frontCam IS NULL OR :frontCam = '' OR LOWER(camTruoc.thongSo) LIKE LOWER(CONCAT('%', :frontCam, '%')))
        GROUP BY s
        HAVING (:minPrice IS NULL OR MIN(ct.giaBan) >= :minPrice)
           AND (:maxPrice IS NULL OR MIN(ct.giaBan) <= :maxPrice)
        ORDER BY MIN(ct.giaBan) ASC
    """)
    List<SanPham> searchAdvanced(
            @Param("kw") String kw,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("chip") String chip,
            @Param("ramId") Integer ramId,
            @Param("romId") Integer romId,
            @Param("osName") String osName,
            @Param("brandId") Integer brandId,
            @Param("brandName") String brandName,
            @Param("gpu") String gpu,
            @Param("cpuName") String cpuName,
            @Param("simType") String simType,
            @Param("minBattery") String minBattery,
            @Param("maxBattery") String maxBattery,
            @Param("minScreen") String minScreen,
            @Param("maxScreen") String maxScreen,
            @Param("rearCam") String rearCam,
            @Param("frontCam") String frontCam
    );
}
