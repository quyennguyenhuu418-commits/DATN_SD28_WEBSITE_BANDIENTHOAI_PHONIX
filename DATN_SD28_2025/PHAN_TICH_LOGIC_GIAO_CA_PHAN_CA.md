# 📊 PHÂN TÍCH LOGIC GIAO CA - PHÂN CA - CA LÀM VIỆC

## 🔍 TỔNG QUAN HỆ THỐNG

### **Cấu Trúc Database**

1. **Bảng `ca`** - Quản lý ca làm việc
   - Định nghĩa các ca: Sáng, Chiều, Tối, Đêm
   - Lưu giờ bắt đầu/kết thúc, trạng thái hoạt động

2. **Bảng `phan_ca`** - Phân ca cho nhân viên
   - Liên kết: `nhan_vien_id` + `ca_id` + `ngay_lam_viec`
   - Trạng thái: `0` (Chưa bắt đầu) → `1` (Đang làm) → `2` (Đã kết thúc) → `3` (Vắng mặt)
   - Lưu giờ bắt đầu/kết thúc thực tế
   - **Constraint:** UNIQUE(nhan_vien_id, ca_id, ngay_lam_viec)

3. **Bảng `giao_ca`** - Bàn giao ca và tiền mặt
   - Liên kết với `phan_ca_id`
   - Lưu thông tin: Tiền đầu ca, tiền cuối ca, doanh thu, đơn hàng
   - Trạng thái: `0` (Chờ xác nhận) → `1` (Đã xác nhận) → `2` (Đã hủy)

4. **Bảng `lich_su_giao_ca`** - Lưu lịch sử thao tác
   - Ghi lại: TAO_MOI, CAP_NHAT, XAC_NHAN, HUY

5. **Bảng `chi_tiet_giao_ca`** - Chi tiết các chỉ số trong ca
   - Phân loại: DOANH_THU, DON_HANG, TIEN_MAT, SU_CO, CONG_VIEC

---

## 🔄 LUỒNG HOẠT ĐỘNG HIỆN TẠI

### **BƯỚC 1: QUẢN LÝ CA LÀM VIỆC (Ca)**

**Mục đích:** Định nghĩa các ca làm việc trong ngày

**Logic hiện tại:**
- ✅ CRUD cơ bản hoạt động tốt
- ✅ Validation: Mã ca unique, giờ bắt đầu < giờ kết thúc
- ✅ Trạng thái: 1 (Hoạt động) / 0 (Tạm dừng)
- ✅ Hỗ trợ tìm kiếm và lọc theo trạng thái

**Đánh giá:** ✅ **HỢP LÝ** - Logic đơn giản, rõ ràng

---

### **BƯỚC 2: PHÂN CA CHO NHÂN VIÊN (PhanCa)**

**Mục đích:** Quản lý lịch làm việc của nhân viên

**Logic hiện tại:**

#### **2.1. Tạo Phân Ca (save)**
```
✅ Kiểm tra: Nhân viên đã được phân ca này trong ngày chưa?
✅ Kiểm tra: Ca này đã được phân cho nhân viên KHÁC chưa?
✅ Cho phép: 1 nhân viên có thể làm NHIỀU CA khác nhau trong 1 ngày
✅ Ngăn chặn: 2 nhân viên khác nhau làm CÙNG 1 ca trong cùng ngày
```

**Ví dụ hợp lệ:**
- ✅ Nhân viên A: Ca Sáng (07:00-12:00) + Ca Chiều (12:00-17:00)
- ❌ Nhân viên A & B: Cùng Ca Sáng (07:00-12:00) → **KHÔNG ĐƯỢC**

**Đánh giá:** ✅ **HỢP LÝ** - Cho phép linh hoạt, tránh trùng ca

#### **2.2. Quản Lý Trạng Thái Phân Ca**

**Các trạng thái:**
- `0`: Chưa bắt đầu (Mặc định khi tạo)
- `1`: Đang làm (Khi nhân viên bắt đầu ca)
- `2`: Đã kết thúc (Khi hoàn thành giao ca)
- `3`: Vắng mặt (Nếu nhân viên không đi làm)

**Các hành động:**

**a) `startShift(phanCaId)`**
```java
✅ Kiểm tra: Trạng thái phải là 0 (Chưa bắt đầu)
✅ Cập nhật: 
   - Trạng thái → 1 (Đang làm)
   - GioBatDauThucTe → LocalDateTime.now()
   - NguoiCapNhat → Current user
```

**b) `endShift(phanCaId)`**
```java
✅ Kiểm tra: Trạng thái phải là 1 (Đang làm)
✅ Cập nhật:
   - Trạng thái → 2 (Đã kết thúc)
   - GioKetThucThucTe → LocalDateTime.now()
   - NguoiCapNhat → Current user
```

**c) `markAbsent(phanCaId, ghiChu)`**
```java
✅ Cập nhật:
   - Trạng thái → 3 (Vắng mặt)
   - GhiChu → Lý do vắng mặt
   - NguoiCapNhat → Current user
```

**Đánh giá:** ✅ **HỢP LÝ** - Logic chuyển trạng thái rõ ràng, có validation

---

### **BƯỚC 3: GIAO CA VÀ BÀN GIAO (GiaoCa)**

**Mục đích:** Bàn giao tiền mặt, doanh thu và công việc giữa các ca

**Logic hiện tại:**

#### **3.1. Tạo Giao Ca (createGiaoCa)**

```java
✅ Kiểm tra: Phân ca phải tồn tại
✅ Kiểm tra: Nhân viên giao phải là nhân viên trong phân ca đó
✅ Kiểm tra: Phân ca phải ở trạng thái 1 (Đang làm)
✅ Kiểm tra: Chưa có giao ca chờ xác nhận cho phân ca này
✅ Tạo giao ca với:
   - Trạng thái: 0 (Chờ xác nhận)
   - SoTienDauCa: Tiền đầu ca (từ ca trước hoặc 0)
   - NgayGiaoCa: Thời gian hiện tại
```

**Đánh giá:** ✅ **HỢP LÝ** - Đảm bảo chỉ nhân viên đang trong ca mới tạo được giao ca

#### **3.2. Xác Nhận Giao Ca (confirmGiaoCa)**

```java
✅ Kiểm tra: Giao ca phải ở trạng thái 0 (Chờ xác nhận)
✅ Kiểm tra: Nhân viên nhận phải có ca làm việc hôm nay
✅ Kiểm tra: Nhân viên nhận phải đang trong ca (trạng thái = 1)
✅ Cập nhật giao ca:
   - NhanVienNhanId → Nhân viên nhận ca
   - SoTienCuoiCa → Tiền cuối ca
   - TongDoanhThu → Tổng doanh thu trong ca
   - SoDonHang → Số đơn hàng
   - BaoCaoCongViec → Báo cáo công việc
   - SuCoBatThuong → Sự cố bất thường
   - CongViecTonDong → Công việc tồn đọng
   - Trạng thái → 1 (Đã xác nhận)
   - ThoiGianXacNhan → Thời gian xác nhận

✅ Tự động cập nhật PhanCa:
   - Phân ca của nhân viên NHẬN → Trạng thái = 1 (Đang làm)
   - Phân ca của nhân viên GIAO → Trạng thái = 2 (Đã kết thúc)
```

**Đánh giá:** ✅ **HỢP LÝ** - Tự động cập nhật trạng thái phân ca, logic rõ ràng

#### **3.3. Hủy Giao Ca (cancelGiaoCa)**

```java
✅ Kiểm tra: Giao ca phải tồn tại
✅ Cập nhật:
   - Trạng thái → 2 (Đã hủy)
   - GhiChu → Lý do hủy
   - NguoiCapNhat → Current user
```

**Đánh giá:** ✅ **HỢP LÝ** - Cho phép hủy khi cần thiết

---

## ⚠️ VẤN ĐỀ PHÁT HIỆN VÀ ĐÁNH GIÁ

### **1. Logic Tính Chênh Lệch Tiền**

**Hiện tại:**
```java
chenhLech = soTienCuoiCa - soTienDauCa - soTienThuThem + soTienChiRa
```

**Đánh giá:** ⚠️ **CẦN XEM XÉT**

**Công thức đúng nên là:**
```
Chênh lệch = (Tiền cuối ca) - (Tiền đầu ca) - (Tiền thu thêm) + (Tiền chi ra)

Trong đó:
- Tiền cuối ca = Tiền đầu ca + Tiền thu trong ca - Tiền chi trong ca
- Nếu chênh lệch = 0 → Đúng
- Nếu chênh lệch > 0 → Thừa tiền
- Nếu chênh lệch < 0 → Thiếu tiền
```

**Kết luận:** ✅ Logic hiện tại **ĐÚNG**, công thức hợp lý

---

### **2. Logic Tự Động Cập Nhật Trạng Thái Phân Ca**

**Vấn đề:** Khi xác nhận giao ca, hệ thống tự động:
- Set tất cả phân ca của nhân viên NHẬN hôm nay → Trạng thái = 1
- Set tất cả phân ca của nhân viên GIAO hôm nay → Trạng thái = 2

**Đánh giá:** ⚠️ **CÓ VẤN ĐỀ**

**Vấn đề:**
- Nếu nhân viên có nhiều ca trong ngày (Ca Sáng + Ca Chiều), khi xác nhận giao ca Sáng:
  - ✅ Phân ca Chiều của nhân viên NHẬN → Trạng thái = 1 (Đúng, vì nhận ca Sáng)
  - ❌ Phân ca Chiều của nhân viên GIAO → Trạng thái = 2 (SAI, ca Chiều chưa bắt đầu)

**Giải pháp đề xuất:**
```java
// Chỉ cập nhật phân ca LIÊN QUAN đến giao ca này
PhanCa phanCaGiao = existingGiaoCa.getPhanCa();
phanCaGiao.setTrangThai(2); // Đã kết thúc
phanCaGiao.setGioKetThucThucTe(LocalDateTime.now());

// Tìm phân ca của nhân viên NHẬN có cùng ca_id và ngay_lam_viec
// HOẶC tìm phân ca SÁU NHẤT của nhân viên nhận (nếu ca này đã kết thúc)
// Chỉ set trạng thái = 1 cho phân ca SÁU NHẤT, không set tất cả
```

---

### **3. Logic Tự Động Tính Doanh Thu**

**Hiện tại:** Doanh thu được nhập thủ công trong `confirmGiaoCa`

**Đánh giá:** ⚠️ **CÓ THỂ CẢI THIỆN**

**Đề xuất:**
- Tự động tính `tongDoanhThu` từ các `HoaDon` được tạo trong khoảng thời gian của ca
- Tự động tính `soDonHang`, `soDonHangThanhToanTienMat`, `soDonHangThanhToanChuyenKhoan`
- Cho phép nhập thủ công để điều chỉnh nếu cần

---

### **4. Logic Bắt Đầu Ca Tự Động**

**Hiện tại:** Nhân viên phải gọi `startShift()` thủ công

**Đánh giá:** ⚠️ **CÓ THỂ CẢI THIỆN**

**Đề xuất:**
- Khi nhân viên đăng nhập, tự động check xem có ca trong giờ hiện tại không
- Nếu có và trạng thái = 0 → Tự động gọi `startShift()` hoặc hiển thị nhắc nhở
- Hoặc tự động start khi đến giờ bắt đầu ca

---

### **5. Logic Kiểm Tra Giao Ca Trùng Lặp**

**Hiện tại:**
```java
Optional<GiaoCa> existingGiaoCa = giaoCaRepository.findByPhanCaIdAndTrangThai(phanCaId, 0);
if (existingGiaoCa.isPresent()) {
    throw new RuntimeException("Đã tồn tại giao ca chưa xác nhận cho phân ca này");
}
```

**Đánh giá:** ✅ **HỢP LÝ** - Ngăn chặn tạo nhiều giao ca chờ xác nhận cho cùng 1 phân ca

---

### **6. Logic Tiền Đầu Ca**

**Vấn đề:** `soTienDauCa` được nhập thủ công khi tạo giao ca

**Đánh giá:** ⚠️ **CÓ THỂ CẢI THIỆN**

**Đề xuất:**
- Tự động lấy `soTienCuoiCa` của giao ca TRƯỚC ĐÓ (nếu có)
- Nếu không có giao ca trước → Mặc định = 0
- Cho phép điều chỉnh thủ công nếu cần

---

## 🎯 LUỒNG CHUẨN ĐỀ XUẤT

### **QUY TRÌNH HOÀN CHỈNH (7 BƯỚC)**

#### **BƯỚC 1: QUẢN LÝ CA LÀM VIỆC** 
*(Quản lý thực hiện)*
```
Tạo các ca làm việc:
- Ca Sáng: 07:00 - 12:00
- Ca Chiều: 12:00 - 17:00
- Ca Tối: 17:00 - 22:00
- Ca Đêm: 22:00 - 07:00 (ngày hôm sau)
```

#### **BƯỚC 2: PHÂN CA CHO NHÂN VIÊN**
*(Quản lý thực hiện - Hàng tuần/tháng)*
```
✅ Chọn nhân viên
✅ Chọn ca làm việc
✅ Chọn ngày làm việc
✅ Kiểm tra không trùng ca (cùng ca + cùng ngày)
✅ Cho phép 1 nhân viên nhiều ca/ngày
```

**Kết quả:** `PhanCa` với trạng thái = 0 (Chưa bắt đầu)

---

#### **BƯỚC 3: NHÂN VIÊN BẮT ĐẦU CA**
*(Nhân viên thực hiện khi đến giờ làm việc)*
```
✅ Nhân viên đăng nhập
✅ Hệ thống hiển thị danh sách ca hôm nay
✅ Nhân viên chọn ca và bấm "Bắt đầu ca"
✅ Hoặc tự động bắt đầu khi đến giờ (tùy cấu hình)

Logic:
- Gọi startShift(phanCaId)
- PhanCa.trangThai → 1 (Đang làm)
- PhanCa.gioBatDauThucTe → Thời gian hiện tại
```

**Kết quả:** `PhanCa` với trạng thái = 1 (Đang làm)

---

#### **BƯỚC 4: TRONG CA - NHÂN VIÊN LÀM VIỆC**
*(Nhân viên thực hiện)*
```
✅ Bán hàng, tạo đơn hàng
✅ Ghi nhận các sự cố (nếu có)
✅ Chuẩn bị báo cáo công việc
✅ Theo dõi tiền mặt, đơn hàng
```

**Ghi chú:** Hệ thống có thể tự động tính doanh thu từ `HoaDon` trong khoảng thời gian ca

---

#### **BƯỚC 5: CUỐI CA - TẠO GIAO CA**
*(Nhân viên GIAO ca thực hiện)*
```
✅ Nhân viên chuẩn bị kết thúc ca (15-30 phút trước)
✅ Điền thông tin:
   - SoTienDauCa: Lấy từ giao ca trước (tự động) hoặc nhập thủ công
   - SoTienCuoiCa: Đếm tiền mặt hiện tại
   - SoTienThuThem: Tiền thu thêm ngoài bán hàng (nếu có)
   - SoTienChiRa: Tiền chi ra (nếu có)
   - TongDoanhThu: Tự động tính từ HoaDon HOẶC nhập thủ công
   - SoDonHang: Tự động tính HOẶC nhập thủ công
   - BaoCaoCongViec: Báo cáo công việc trong ca
   - SuCoBatThuong: Sự cố bất thường (nếu có)
   - CongViecTonDong: Công việc tồn đọng

✅ Gọi createGiaoCa():
   - Tạo GiaoCa với trạng thái = 0 (Chờ xác nhận)
   - Gửi cho nhân viên ca sau
```

**Kết quả:** `GiaoCa` với trạng thái = 0 (Chờ xác nhận)

---

#### **BƯỚC 6: NHÂN VIÊN CA SAU XÁC NHẬN GIAO CA**
*(Nhân viên NHẬN ca thực hiện)*
```
✅ Nhân viên ca sau đăng nhập
✅ Hệ thống hiển thị giao ca chờ xác nhận (nếu có)
✅ Nhân viên đọc báo cáo:
   - Tiền đầu ca (sẽ nhận)
   - Doanh thu ca trước
   - Sự cố, công việc tồn đọng

✅ Nhân viên đếm tiền đầu ca thực tế
✅ So sánh với SoTienCuoiCa trong giao ca
✅ Nếu khớp → Xác nhận
✅ Nếu không khớp → Điều chỉnh và ghi chú

✅ Gọi confirmGiaoCa():
   - GiaoCa.trangThai → 1 (Đã xác nhận)
   - PhanCa (nhân viên GIAO) → Trạng thái = 2 (Đã kết thúc)
   - PhanCa (nhân viên NHẬN) → Trạng thái = 1 (Đang làm) - CHỈ phân ca tương ứng
```

**Kết quả:** 
- `GiaoCa` với trạng thái = 1 (Đã xác nhận)
- `PhanCa` (nhân viên giao) → Trạng thái = 2
- `PhanCa` (nhân viên nhận) → Trạng thái = 1

---

#### **BƯỚC 7: NHÂN VIÊN NHẬN CA TIẾP TỤC LÀM VIỆC**
*(Lặp lại từ Bước 4)*
```
✅ Nhân viên nhận ca bắt đầu làm việc
✅ Lặp lại quy trình từ Bước 4 → Bước 5 → Bước 6
```

---

## 🔧 CẢI THIỆN ĐỀ XUẤT

### **1. Sửa Logic Cập Nhật Phân Ca Khi Xác Nhận Giao Ca**

**Vấn đề hiện tại:**
- Cập nhật TẤT CẢ phân ca của nhân viên trong ngày
- Gây lỗi nếu nhân viên có nhiều ca/ngày

**Giải pháp:**
```java
// CHỈ cập nhật phân ca LIÊN QUAN đến giao ca này

// 1. Cập nhật phân ca của nhân viên GIAO (ca đã kết thúc)
PhanCa phanCaGiao = existingGiaoCa.getPhanCa();
phanCaGiao.setTrangThai(2); // Đã kết thúc
phanCaGiao.setGioKetThucThucTe(LocalDateTime.now());

// 2. Tìm phân ca của nhân viên NHẬN (ca tương ứng)
// Option A: Tìm phân ca có cùng ca_id, cùng ngay_lam_viec
List<PhanCa> phanCaNhan = phanCaRepository.findByNhanVienIdAndNgayLamViec(
    nhanVienNhanId, today);

// Tìm phân ca của ca tiếp theo (theo thứ tự thời gian)
PhanCa phanCaTiepTheo = phanCaNhan.stream()
    .filter(pc -> pc.getCa().getGioBatDau()
            .isAfter(phanCaGiao.getCa().getGioKetThuc()))
    .min(Comparator.comparing(pc -> pc.getCa().getGioBatDau()))
    .orElse(null);

if (phanCaTiepTheo != null && phanCaTiepTheo.getTrangThai() == 0) {
    phanCaTiepTheo.setTrangThai(1); // Đang làm
    phanCaTiepTheo.setGioBatDauThucTe(LocalDateTime.now());
    phanCaRepository.save(phanCaTiepTheo);
}

// Option B: Tìm phân ca SỚM NHẤT chưa bắt đầu của nhân viên nhận
PhanCa phanCaSomNhat = phanCaNhan.stream()
    .filter(pc -> pc.getTrangThai() == 0)
    .min(Comparator.comparing(pc -> pc.getCa().getGioBatDau()))
    .orElse(null);
```

---

### **2. Tự Động Tính Doanh Thu**

**Giải pháp:**
```java
// Trong confirmGiaoCa, tự động tính nếu chưa có
if (giaoCaDTO.getTongDoanhThu() == null || giaoCaDTO.getTongDoanhThu().equals(BigDecimal.ZERO)) {
    // Tính doanh thu từ HoaDon trong khoảng thời gian ca
    LocalDateTime batDauCa = phanCa.getGioBatDauThucTe();
    LocalDateTime ketThucCa = LocalDateTime.now();
    
    BigDecimal tongDoanhThu = hoaDonRepository.getTotalRevenueByDateRange(batDauCa, ketThucCa);
    Integer soDonHang = hoaDonRepository.countByDateRange(batDauCa, ketThucCa);
    
    existingGiaoCa.setTongDoanhThu(tongDoanhThu);
    existingGiaoCa.setSoDonHang(soDonHang);
}
```

---

### **3. Tự Động Lấy Tiền Đầu Ca**

**Giải pháp:**
```java
// Trong createGiaoCa, tự động lấy từ giao ca trước
BigDecimal soTienDauCa = BigDecimal.ZERO;

// Tìm giao ca của ca trước (cùng ngày)
List<GiaoCa> giaoCaTruoc = giaoCaRepository.findByPhanCaIdAndDate(
    phanCaGiao.getCa().getGioBatDau(), // Ca trước đó
    phanCaGiao.getNgayLamViec()
);

if (!giaoCaTruoc.isEmpty() && giaoCaTruoc.get(0).getTrangThai() == 1) {
    soTienDauCa = giaoCaTruoc.get(0).getSoTienCuoiCa();
}

// Hoặc cho phép override nếu nhập thủ công
if (soTienDauCaParam != null) {
    soTienDauCa = soTienDauCaParam;
}
```

---

### **4. Tự Động Bắt Đầu Ca**

**Giải pháp:**
```java
// Tạo scheduled job hoặc check khi đăng nhập
@Scheduled(fixedRate = 60000) // Mỗi phút
public void autoStartShifts() {
    LocalDateTime now = LocalDateTime.now();
    LocalDate today = LocalDate.now();
    
    // Tìm các phân ca chưa bắt đầu nhưng đã đến giờ
    List<PhanCa> phanCaCanStart = phanCaRepository.findByNgayLamViecAndTrangThai(today, 0);
    
    for (PhanCa phanCa : phanCaCanStart) {
        Ca ca = phanCa.getCa();
        LocalTime gioBatDau = ca.getGioBatDau();
        
        // Nếu đã đến giờ (cho phép trễ 5 phút)
        if (now.toLocalTime().isAfter(gioBatDau) 
            && now.toLocalTime().isBefore(gioBatDau.plusMinutes(5))) {
            
            // Tự động bắt đầu hoặc gửi thông báo
            phanCaService.startShift(phanCa.getId());
            // Hoặc gửi notification cho nhân viên
        }
    }
}
```

---

### **5. Validation Bổ Sung**

**Đề xuất thêm:**
```java
// 1. Kiểm tra thời gian hợp lý
- GioBatDauThucTe không được quá sớm (trước giờ bắt đầu ca > 1 tiếng)
- GioKetThucThucTe không được quá muộn (sau giờ kết thúc ca > 1 tiếng)

// 2. Kiểm tra tiền mặt hợp lý
- SoTienCuoiCa không được âm
- ChenhLech không được quá lớn (theo cấu hình)

// 3. Kiểm tra doanh thu hợp lý
- TongDoanhThu không được âm
- SoDonHang phải >= 0
```

---

## 📋 TÓM TẮT ĐÁNH GIÁ

### **✅ PHẦN TỐT**

1. ✅ Cấu trúc database hợp lý, rõ ràng
2. ✅ Logic phân ca cho phép linh hoạt (nhiều ca/ngày)
3. ✅ Validation đầy đủ, tránh lỗi
4. ✅ Có lịch sử thao tác (LichSuGiaoCa)
5. ✅ Tự động cập nhật trạng thái khi xác nhận giao ca
6. ✅ Tính chênh lệch tiền chính xác

### **⚠️ PHẦN CẦN CẢI THIỆN**

1. ⚠️ **Logic cập nhật phân ca khi xác nhận:** Cần chỉ cập nhật phân ca LIÊN QUAN, không cập nhật tất cả
2. ⚠️ **Tự động tính doanh thu:** Nên tự động tính từ HoaDon thay vì chỉ nhập thủ công
3. ⚠️ **Tự động lấy tiền đầu ca:** Nên lấy từ giao ca trước
4. ⚠️ **Bắt đầu ca tự động:** Có thể tự động hoặc nhắc nhở khi đến giờ
5. ⚠️ **Validation bổ sung:** Thêm validation thời gian, tiền mặt, doanh thu

### **🎯 ĐỀ XUẤT ƯU TIÊN**

**Ưu tiên cao:**
1. 🔴 Sửa logic cập nhật phân ca (chỉ cập nhật phân ca liên quan)
2. 🟡 Tự động tính doanh thu từ HoaDon
3. 🟡 Tự động lấy tiền đầu ca từ giao ca trước

**Ưu tiên trung bình:**
4. 🟢 Tự động bắt đầu ca hoặc nhắc nhở
5. 🟢 Thêm validation bổ sung

---

## 📝 KẾT LUẬN

**Logic hiện tại:** ✅ **NHÌN CHUNG HỢP LÝ**, có một số điểm cần cải thiện

**Hướng luồng chuẩn:** Đã nêu ở trên với 7 bước rõ ràng

**Khuyến nghị:** Ưu tiên sửa logic cập nhật phân ca để tránh lỗi khi nhân viên có nhiều ca/ngày.






