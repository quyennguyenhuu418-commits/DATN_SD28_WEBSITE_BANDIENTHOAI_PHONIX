package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.DotGiamGiaSanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DotGiamGiaSanPhamRepository extends JpaRepository<DotGiamGiaSanPham, Integer> {

    // Tìm tất cả sản phẩm áp dụng cho 1 đợt giảm giá
    List<DotGiamGiaSanPham> findByKhuyenMaiId(Integer idKhuyenMai);

    // Tìm tất cả đợt giảm giá áp dụng cho 1 sản phẩm
    List<DotGiamGiaSanPham> findBySanPhamId(Integer idSanPham);

    // Kiểm tra xem sản phẩm đã được thêm vào đợt giảm giá chưa
    Optional<DotGiamGiaSanPham> findByKhuyenMaiIdAndSanPhamId(Integer idKhuyenMai, Integer idSanPham);

    // Xóa sản phẩm khỏi đợt giảm giá
    void deleteByKhuyenMaiIdAndSanPhamId(Integer idKhuyenMai, Integer idSanPham);

    // Xóa tất cả sản phẩm của 1 đợt giảm giá
    void deleteByKhuyenMaiId(Integer idKhuyenMai);

    // Kiểm tra xem đợt giảm giá có sản phẩm nào không
    boolean existsByKhuyenMaiId(Integer idKhuyenMai);

    // Đếm số sản phẩm trong đợt giảm giá
    long countByKhuyenMaiId(Integer idKhuyenMai);

    // Lấy danh sách sản phẩm áp dụng với thông tin đầy đủ
    @Query("SELECT d FROM DotGiamGiaSanPham d " +
            "LEFT JOIN FETCH d.sanPham sp " +
            "LEFT JOIN FETCH sp.hang " +
            "LEFT JOIN FETCH sp.danhMuc " +
            "WHERE d.khuyenMai.id = :idKhuyenMai")
    List<DotGiamGiaSanPham> findByKhuyenMaiIdWithDetails(@Param("idKhuyenMai") Integer idKhuyenMai);
}


