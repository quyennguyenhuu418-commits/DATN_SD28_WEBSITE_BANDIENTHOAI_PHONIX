# 📘 HƯỚNG DẪN CHI TIẾT LUỒNG GIAO CA - PHÂN CA

## 🎯 MỤC ĐÍCH

Tài liệu này giải thích chi tiết từng bước trong quy trình **Quản lý Ca Làm Việc**, **Phân Ca** và **Giao Ca**, giúp người dùng hiểu rõ cách hệ thống hoạt động.

---

## 📋 TỔNG QUAN HỆ THỐNG

### **Các Thành Phần Chính:**

1. **CA** - Định nghĩa ca làm việc (Sáng, Chiều, Tối, Đêm)
2. **PHAN_CA** - Phân ca cho nhân viên (Lịch làm việc)
3. **GIAO_CA** - Bàn giao ca và tiền mặt giữa các ca
4. **LICH_SU_GIAO_CA** - Lịch sử các thao tác giao ca

### **Các Trạng Thái:**

#### **Phân Ca (PhanCa):**
- `0` - Chưa bắt đầu
- `1` - Đang làm
- `2` - Đã kết thúc
- `3` - Vắng mặt

#### **Giao Ca (GiaoCa):**
- `0` - Chờ xác nhận
- `1` - Đã xác nhận
- `2` - Đã hủy

---

## 🔄 LUỒNG HOẠT ĐỘNG CHI TIẾT

### **BƯỚC 1: QUẢN LÝ CA LÀM VIỆC**

**Người thực hiện:** Quản lý

**Mục đích:** Định nghĩa các ca làm việc trong ngày

#### **Các Bước:**

1. **Vào trang quản lý Ca:**
   - Truy cập: `/quan-ly-ca` hoặc menu "Quản lý Ca"

2. **Tạo ca mới hoặc chỉnh sửa ca hiện có:**
   - **Mã ca:** Ví dụ: `CA_SANG`, `CA_CHIEU`
   - **Tên ca:** Ví dụ: "Ca Sáng", "Ca Chiều"
   - **Giờ bắt đầu:** Ví dụ: `07:00:00`
   - **Giờ kết thúc:** Ví dụ: `12:00:00`
   - **Trạng thái:** Hoạt động (1) hoặc Tạm dừng (0)

3. **Lưu ca:**
   - Hệ thống kiểm tra:
     - ✅ Mã ca phải unique
     - ✅ Giờ bắt đầu < Giờ kết thúc
     - ✅ Các field bắt buộc không được trống

4. **Kết quả:**
   - Ca được lưu vào database
   - Có thể sử dụng để phân ca cho nhân viên

---

### **BƯỚC 2: PHÂN CA CHO NHÂN VIÊN**

**Người thực hiện:** Quản lý

**Mục đích:** Tạo lịch làm việc cho nhân viên (hàng tuần/tháng)

#### **Các Bước:**

1. **Vào trang Phân Ca:**
   - Truy cập: `/phan-ca` hoặc menu "Phân Ca"

2. **Tạo phân ca mới:**
   - **Nhân viên:** Chọn nhân viên
   - **Ca làm việc:** Chọn ca (Sáng, Chiều, Tối, Đêm)
   - **Ngày làm việc:** Chọn ngày
   - **Ghi chú:** (Tùy chọn) Ghi chú về phân ca

3. **Hệ thống kiểm tra:**
   - ✅ **Cho phép:** 1 nhân viên có thể làm NHIỀU CA khác nhau trong 1 ngày
     - Ví dụ: Nhân viên A làm Ca Sáng + Ca Chiều cùng ngày ✅
   - ❌ **Ngăn chặn:** 2 nhân viên KHÁC NHAU làm CÙNG 1 ca trong cùng ngày
     - Ví dụ: Nhân viên A và B cùng làm Ca Sáng ngày 01/01 ❌

4. **Kết quả:**
   - Phân ca được tạo với trạng thái = 0 (Chưa bắt đầu)
   - Nhân viên có thể xem lịch làm việc của mình

---

### **BƯỚC 3: NHÂN VIÊN BẮT ĐẦU CA**

**Người thực hiện:** Nhân viên

**Mục đích:** Bắt đầu ca làm việc khi đến giờ

#### **Các Bước:**

1. **Nhân viên đăng nhập vào hệ thống**

2. **Vào trang Giao Ca:**
   - Truy cập: `/giao-ca` hoặc menu "Giao Ca"
   - Hệ thống hiển thị danh sách ca hôm nay của nhân viên

3. **Chọn ca và bấm "Bắt đầu ca":**
   - Nhân viên chọn ca cần bắt đầu
   - Bấm nút "Bắt đầu ca" hoặc "Start Shift"

4. **Hệ thống xử lý:**
   ```java
   // Backend: PhanCaServiceImpl.startShift()
   - Kiểm tra: Trạng thái phải là 0 (Chưa bắt đầu)
   - Cập nhật:
     * Trạng thái → 1 (Đang làm)
     * GioBatDauThucTe → Thời gian hiện tại
     * NguoiCapNhat → Tên nhân viên đang đăng nhập
   ```

5. **Kết quả:**
   - Phân ca chuyển sang trạng thái = 1 (Đang làm)
   - Nhân viên bắt đầu làm việc

---

### **BƯỚC 4: TRONG CA - NHÂN VIÊN LÀM VIỆC**

**Người thực hiện:** Nhân viên

**Mục đích:** Thực hiện công việc trong ca

#### **Các Hoạt Động:**

1. **Bán hàng:**
   - Tạo đơn hàng tại quầy (POS)
   - Xử lý đơn hàng online
   - Nhận thanh toán (tiền mặt, chuyển khoản)

2. **Ghi nhận các sự cố:**
   - Nếu có sự cố bất thường, ghi lại để báo cáo
   - Chuẩn bị thông tin cho báo cáo cuối ca

3. **Theo dõi tiền mặt:**
   - Đếm tiền thu được
   - Ghi nhận tiền chi ra (nếu có)
   - Ghi nhận tiền thu thêm (nếu có)

4. **Chuẩn bị báo cáo:**
   - Tổng hợp số đơn hàng
   - Tổng hợp doanh thu
   - Ghi chú công việc tồn đọng

---

### **BƯỚC 5: CUỐI CA - TẠO GIAO CA**

**Người thực hiện:** Nhân viên GIAO ca

**Mục đích:** Tạo giao ca để bàn giao cho nhân viên ca sau

#### **Các Bước:**

1. **Vào trang Giao Ca:**
   - Truy cập: `/giao-ca`
   - Hệ thống hiển thị thông tin ca đang làm

2. **Điền thông tin giao ca:**

   **a) Tiền mặt:**
   - **Số tiền đầu ca:** 
     - ✅ **TỰ ĐỘNG:** Hệ thống tự động lấy từ `soTienCuoiCa` của giao ca trước
     - ✅ **CÓ THỂ NHẬP THỦ CÔNG:** Nếu cần điều chỉnh
     - Ví dụ: Nếu giao ca trước có `soTienCuoiCa = 5,000,000₫`, hệ thống tự động điền
   
   - **Số tiền cuối ca:** 
     - Nhân viên đếm tiền mặt hiện tại
     - Nhập số tiền cuối ca
     - Ví dụ: `8,500,000₫`
   
   - **Số tiền thu thêm:** 
     - Tiền thu thêm ngoài bán hàng (nếu có)
     - Ví dụ: `500,000₫` (tiền phụ phí)
   
   - **Số tiền chi ra:** 
     - Tiền chi ra trong ca (nếu có)
     - Ví dụ: `300,000₫` (tiền mua đồ dùng)

   **b) Doanh thu và đơn hàng:**
   - **Tổng doanh thu:**
     - ✅ **TỰ ĐỘNG:** Hệ thống tự động tính từ các `HoaDon` trong khoảng thời gian ca
     - ✅ **CÓ THỂ NHẬP THỦ CÔNG:** Nếu cần điều chỉnh
     - Logic: `SUM(HoaDon.tongTienSauGiam)` với `ngayTao` trong khoảng `[gioBatDauThucTe, now]`
   
   - **Số đơn hàng:**
     - ✅ **TỰ ĐỘNG:** Hệ thống tự động đếm số `HoaDon` trong ca
     - ✅ **CÓ THỂ NHẬP THỦ CÔNG:** Nếu cần điều chỉnh
   
   - **Số đơn thanh toán tiền mặt:**
     - ✅ **TỰ ĐỘNG:** Đếm các `HoaDon` có `ChiTietThanhToan` với `PhuongThucThanhToan` là "Tiền mặt"
   
   - **Số đơn thanh toán chuyển khoản:**
     - ✅ **TỰ ĐỘNG:** Đếm các `HoaDon` có `ChiTietThanhToan` với `PhuongThucThanhToan` là "Chuyển khoản/VNPAY/ZaloPay"

   **c) Báo cáo:**
   - **Báo cáo công việc:** Mô tả công việc đã làm trong ca
   - **Sự cố bất thường:** Ghi lại các sự cố (nếu có)
   - **Công việc tồn đọng:** Công việc chưa hoàn thành, cần ca sau tiếp tục
   - **Ghi chú:** Ghi chú khác

3. **Bấm "Tạo Giao Ca":**

4. **Hệ thống xử lý:**
   ```java
   // Backend: GiaoCaServiceImpl.createGiaoCa()
   
   1. Kiểm tra:
      ✅ Phân ca phải tồn tại
      ✅ Nhân viên giao phải là nhân viên trong phân ca
      ✅ Phân ca phải ở trạng thái = 1 (Đang làm)
      ✅ Chưa có giao ca chờ xác nhận cho phân ca này
   
   2. Tự động lấy tiền đầu ca:
      - Tìm giao ca trước (cùng ca hoặc ca trước) đã xác nhận
      - Lấy soTienCuoiCa từ giao ca trước
      - Nếu không có → Mặc định = 0
      - Nếu người dùng nhập thủ công → Ưu tiên giá trị nhập vào
   
   3. Tạo giao ca:
      - Trạng thái = 0 (Chờ xác nhận)
      - NgayGiaoCa = Thời gian hiện tại
      - Lưu tất cả thông tin đã nhập
   ```

5. **Kết quả:**
   - Giao ca được tạo với trạng thái = 0 (Chờ xác nhận)
   - Gửi thông báo cho nhân viên ca sau (nếu có)
   - Nhân viên ca sau sẽ thấy giao ca chờ xác nhận khi đăng nhập

---

### **BƯỚC 6: NHÂN VIÊN CA SAU XÁC NHẬN GIAO CA**

**Người thực hiện:** Nhân viên NHẬN ca

**Mục đích:** Xác nhận và nhận giao ca từ nhân viên ca trước

#### **Các Bước:**

1. **Nhân viên ca sau đăng nhập:**
   - Hệ thống tự động hiển thị modal "Xác nhận giao ca" (nếu có giao ca chờ xác nhận)
   - Hoặc vào trang `/giao-ca` để xem danh sách giao ca chờ xác nhận

2. **Đọc báo cáo giao ca:**
   - Xem thông tin tiền đầu ca (sẽ nhận)
   - Xem tổng doanh thu ca trước
   - Xem số đơn hàng
   - Xem báo cáo công việc
   - Xem sự cố bất thường (nếu có)
   - Xem công việc tồn đọng (nếu có)

3. **Đếm tiền đầu ca thực tế:**
   - Nhân viên đếm tiền mặt thực tế
   - So sánh với `soTienCuoiCa` trong giao ca
   - Nếu khớp → Tiến hành xác nhận
   - Nếu không khớp → Điều chỉnh và ghi chú

4. **Điều chỉnh thông tin (nếu cần):**
   - **Số tiền cuối ca:** Nhập số tiền thực tế
   - **Số tiền thu thêm:** Nhập nếu có
   - **Số tiền chi ra:** Nhập nếu có
   - **Doanh thu và đơn hàng:** 
     - ✅ **TỰ ĐỘNG:** Hệ thống tự động tính từ `HoaDon` nếu chưa có hoặc = 0
     - ✅ **CÓ THỂ ĐIỀU CHỈNH:** Nếu cần sửa lại

5. **Bấm "Xác nhận giao ca":**

6. **Hệ thống xử lý:**
   ```java
   // Backend: GiaoCaServiceImpl.confirmGiaoCa()
   
   1. Kiểm tra:
      ✅ Giao ca phải ở trạng thái = 0 (Chờ xác nhận)
      ✅ Nhân viên nhận phải có ca làm việc hôm nay
      ✅ Nhân viên nhận phải đang trong ca (trạng thái = 1)
      ✅ Số tiền cuối ca không được âm
   
   2. Tự động tính doanh thu (nếu chưa có hoặc = 0):
      - TongDoanhThu = SUM(HoaDon.tongTienSauGiam) 
        trong khoảng [gioBatDauThucTe, now]
      - SoDonHang = COUNT(HoaDon) trong khoảng thời gian ca
      - SoDonHangThanhToanTienMat = COUNT với điều kiện tiền mặt
      - SoDonHangThanhToanChuyenKhoan = COUNT với điều kiện chuyển khoản
   
   3. Cập nhật giao ca:
      - Trạng thái → 1 (Đã xác nhận)
      - ThoiGianXacNhan → Thời gian hiện tại
      - Lưu tất cả thông tin đã điều chỉnh
   
   4. Tự động cập nhật phân ca:
      a) Phân ca của nhân viên GIAO:
         - Trạng thái → 2 (Đã kết thúc)
         - GioKetThucThucTe → Thời gian hiện tại
      
      b) Phân ca của nhân viên NHẬN:
         - Tìm phân ca SỚM NHẤT chưa bắt đầu (trạng thái = 0)
         - Trạng thái → 1 (Đang làm)
         - GioBatDauThucTe → Thời gian hiện tại (nếu chưa có)
   
   5. Tính chênh lệch:
      - ChenhLech = (SoTienCuoiCa - SoTienDauCa) - SoTienThuThem + SoTienChiRa
      - Nếu chênh lệch = 0 → Đúng
      - Nếu chênh lệch > 0 → Thừa tiền
      - Nếu chênh lệch < 0 → Thiếu tiền
   ```

7. **Kết quả:**
   - Giao ca chuyển sang trạng thái = 1 (Đã xác nhận)
   - Phân ca của nhân viên GIAO → Trạng thái = 2 (Đã kết thúc)
   - Phân ca của nhân viên NHẬN → Trạng thái = 1 (Đang làm)
   - Nhân viên nhận ca bắt đầu làm việc

---

### **BƯỚC 7: LẶP LẠI QUY TRÌNH**

**Người thực hiện:** Tất cả nhân viên

**Mục đích:** Tiếp tục quy trình cho các ca tiếp theo

#### **Các Bước:**

1. **Nhân viên nhận ca tiếp tục làm việc** (Lặp lại Bước 4)
2. **Cuối ca, tạo giao ca** (Lặp lại Bước 5)
3. **Nhân viên ca sau xác nhận** (Lặp lại Bước 6)
4. **Lặp lại cho đến hết ca trong ngày**

---

## 🎯 CÁC TÍNH NĂNG TỰ ĐỘNG

### **1. Tự Động Lấy Tiền Đầu Ca**

**Khi nào:** Khi tạo giao ca mới

**Cách hoạt động:**
```
1. Tìm giao ca trước đã xác nhận (trạng thái = 1)
   - Tìm theo cùng ca_id và ngay_lam_viec <= ngay_lam_viec hiện tại
   - Hoặc tìm theo phanCaId
   
2. Lấy soTienCuoiCa từ giao ca trước
   
3. Điền vào soTienDauCa của giao ca mới
   
4. Nếu người dùng nhập thủ công → Ưu tiên giá trị nhập vào
```

**Ví dụ:**
- Ca Sáng (07:00-12:00): `soTienCuoiCa = 5,000,000₫`
- Ca Chiều (12:00-17:00): Tự động lấy `soTienDauCa = 5,000,000₫`

---

### **2. Tự Động Tính Doanh Thu**

**Khi nào:** Khi xác nhận giao ca

**Cách hoạt động:**
```
1. Lấy khoảng thời gian ca:
   - BatDau = phanCa.gioBatDauThucTe (hoặc giờ bắt đầu ca)
   - KetThuc = Thời gian hiện tại
   
2. Tính từ HoaDon:
   - TongDoanhThu = SUM(HoaDon.tongTienSauGiam) 
     WHERE HoaDon.ngayTao BETWEEN BatDau AND KetThuc
     AND HoaDon.trangThai IN (1, 4) // Đã thanh toán hoặc hoàn thành
   
   - SoDonHang = COUNT(HoaDon) trong khoảng thời gian
   
   - SoDonHangThanhToanTienMat = COUNT với ChiTietThanhToan 
     có PhuongThucThanhToan.tenPhuongThuc LIKE '%Tiền mặt%'
   
   - SoDonHangThanhToanChuyenKhoan = COUNT với ChiTietThanhToan
     có PhuongThucThanhToan.tenPhuongThuc LIKE '%Chuyển khoản%' 
     hoặc '%VNPAY%' hoặc '%ZaloPay%'
   
3. Chỉ tự động tính nếu:
   - TongDoanhThu chưa có hoặc = 0
   - Hoặc các số đơn hàng chưa có hoặc = 0
   
4. Nếu người dùng đã nhập → Giữ nguyên giá trị nhập vào
```

**Ví dụ:**
- Ca Sáng có 10 đơn hàng với tổng `tongTienSauGiam = 15,000,000₫`
- Khi xác nhận giao ca, hệ thống tự động:
  - `tongDoanhThu = 15,000,000₫`
  - `soDonHang = 10`
  - `soDonHangThanhToanTienMat = 7` (7 đơn thanh toán tiền mặt)
  - `soDonHangThanhToanChuyenKhoan = 3` (3 đơn chuyển khoản)

---

### **3. Tự Động Cập Nhật Phân Ca**

**Khi nào:** Khi xác nhận giao ca

**Cách hoạt động:**
```
1. Cập nhật phân ca của nhân viên GIAO:
   - Phân ca LIÊN QUAN đến giao ca này
   - Trạng thái → 2 (Đã kết thúc)
   - GioKetThucThucTe → Thời gian hiện tại
   
2. Tìm và cập nhật phân ca của nhân viên NHẬN:
   - Tìm phân ca SỚM NHẤT chưa bắt đầu (trạng thái = 0)
   - Ưu tiên ca có giờ bắt đầu sau ca vừa giao
   - Nếu không có → Lấy ca sớm nhất chưa bắt đầu
   - Trạng thái → 1 (Đang làm)
   - GioBatDauThucTe → Thời gian hiện tại (nếu chưa có)
```

**Ví dụ:**
- Nhân viên A giao Ca Sáng cho Nhân viên B
- Hệ thống tự động:
  - Phân ca Ca Sáng của A → Trạng thái = 2 (Đã kết thúc)
  - Phân ca Ca Chiều của B (nếu có) → Trạng thái = 1 (Đang làm)

---

### **4. Tính Chênh Lệch Tiền Tự Động**

**Khi nào:** Khi cập nhật giao ca

**Công thức:**
```
ChenhLech = (SoTienCuoiCa - SoTienDauCa) - SoTienThuThem + SoTienChiRa
```

**Ví dụ:**
- `SoTienDauCa = 5,000,000₫`
- `SoTienCuoiCa = 8,500,000₫`
- `SoTienThuThem = 500,000₫`
- `SoTienChiRa = 300,000₫`
- `ChenhLech = (8,500,000 - 5,000,000) - 500,000 + 300,000 = 3,300,000₫`

**Ý nghĩa:**
- `ChenhLech = 0` → Đúng với doanh thu
- `ChenhLech > 0` → Thừa tiền (có thể do thu thêm hoặc sai sót)
- `ChenhLech < 0` → Thiếu tiền (cần kiểm tra)

---

## ⚠️ LƯU Ý QUAN TRỌNG

### **1. Tiền Đầu Ca**

- ✅ **Tự động:** Hệ thống tự động lấy từ giao ca trước
- ✅ **Có thể nhập thủ công:** Nếu cần điều chỉnh
- ⚠️ **Lưu ý:** Nếu là ca đầu tiên trong ngày, tiền đầu ca = 0 hoặc số tiền ban đầu

### **2. Doanh Thu**

- ✅ **Tự động:** Hệ thống tự động tính từ `HoaDon`
- ✅ **Có thể điều chỉnh:** Nếu cần sửa lại
- ⚠️ **Lưu ý:** Chỉ tính các `HoaDon` có `trangThai IN (1, 4)` (Đã thanh toán hoặc hoàn thành)

### **3. Phân Ca**

- ✅ **Tự động:** Chỉ cập nhật phân ca LIÊN QUAN
- ⚠️ **Lưu ý:** Nếu nhân viên có nhiều ca/ngày, chỉ phân ca tiếp theo được cập nhật

### **4. Xác Nhận Giao Ca**

- ⚠️ **Yêu cầu:** Nhân viên nhận phải đang trong ca (trạng thái = 1)
- ⚠️ **Lưu ý:** Phải đếm tiền thực tế và so sánh với `soTienCuoiCa` trước khi xác nhận

### **5. Chênh Lệch Tiền**

- ⚠️ **Kiểm tra:** Nếu chênh lệch quá lớn, cần kiểm tra lại
- ⚠️ **Lưu ý:** Chênh lệch có thể do thu thêm hoặc chi ra trong ca

---

## 📊 VÍ DỤ THỰC TẾ

### **Ví Dụ 1: Ca Sáng → Ca Chiều**

**Bước 1:** Nhân viên A bắt đầu Ca Sáng (07:00-12:00)
- Phân ca Ca Sáng của A → Trạng thái = 1 (Đang làm)

**Bước 2:** Trong ca, A bán hàng:
- Tạo 10 đơn hàng
- Tổng doanh thu: 15,000,000₫
- Tiền mặt cuối ca: 15,500,000₫ (bao gồm tiền đầu ca 5,000,000₫)

**Bước 3:** Cuối Ca Sáng, A tạo giao ca:
- `SoTienDauCa`: Tự động = 5,000,000₫ (từ giao ca trước hoặc ban đầu)
- `SoTienCuoiCa`: Nhập = 15,500,000₫
- `TongDoanhThu`: Tự động = 15,000,000₫ (từ HoaDon)
- `SoDonHang`: Tự động = 10
- Giao ca → Trạng thái = 0 (Chờ xác nhận)

**Bước 4:** Nhân viên B đăng nhập, xác nhận giao ca:
- Đếm tiền thực tế: 15,500,000₫ ✅
- Bấm "Xác nhận"
- Hệ thống:
  - Giao ca → Trạng thái = 1 (Đã xác nhận)
  - Phân ca Ca Sáng của A → Trạng thái = 2 (Đã kết thúc)
  - Phân ca Ca Chiều của B → Trạng thái = 1 (Đang làm)

**Bước 5:** B bắt đầu làm việc Ca Chiều với tiền đầu ca = 15,500,000₫

---

## 🔍 GIẢI THÍCH CÁC TRƯỜNG DỮ LIỆU

### **GiaoCa:**

| Trường | Ý nghĩa | Tự động/Thủ công |
|--------|---------|------------------|
| `soTienDauCa` | Tiền đầu ca | ✅ Tự động từ giao ca trước |
| `soTienCuoiCa` | Tiền cuối ca | ❌ Nhập thủ công (đếm tiền) |
| `soTienThuThem` | Tiền thu thêm | ❌ Nhập thủ công (nếu có) |
| `soTienChiRa` | Tiền chi ra | ❌ Nhập thủ công (nếu có) |
| `tongDoanhThu` | Tổng doanh thu | ✅ Tự động từ HoaDon |
| `soDonHang` | Số đơn hàng | ✅ Tự động từ HoaDon |
| `soDonHangThanhToanTienMat` | Số đơn tiền mặt | ✅ Tự động từ HoaDon |
| `soDonHangThanhToanChuyenKhoan` | Số đơn chuyển khoản | ✅ Tự động từ HoaDon |
| `chenhLech` | Chênh lệch tiền | ✅ Tự động tính |
| `baoCaoCongViec` | Báo cáo công việc | ❌ Nhập thủ công |
| `suCoBatThuong` | Sự cố bất thường | ❌ Nhập thủ công (nếu có) |
| `congViecTonDong` | Công việc tồn đọng | ❌ Nhập thủ công (nếu có) |

---

## ✅ CHECKLIST CHO NHÂN VIÊN

### **Khi Bắt Đầu Ca:**
- [ ] Đăng nhập vào hệ thống
- [ ] Xem danh sách ca hôm nay
- [ ] Bấm "Bắt đầu ca" khi đến giờ
- [ ] Kiểm tra trạng thái ca = "Đang làm"

### **Trong Ca:**
- [ ] Bán hàng, tạo đơn hàng
- [ ] Ghi nhận sự cố (nếu có)
- [ ] Chuẩn bị báo cáo công việc

### **Cuối Ca - Tạo Giao Ca:**
- [ ] Đếm tiền cuối ca
- [ ] Kiểm tra tiền đầu ca (tự động hoặc nhập thủ công)
- [ ] Kiểm tra doanh thu và số đơn hàng (tự động hoặc nhập thủ công)
- [ ] Điền báo cáo công việc
- [ ] Điền sự cố bất thường (nếu có)
- [ ] Điền công việc tồn đọng (nếu có)
- [ ] Bấm "Tạo Giao Ca"

### **Khi Nhận Ca (Xác Nhận Giao Ca):**
- [ ] Đăng nhập vào hệ thống
- [ ] Xem giao ca chờ xác nhận
- [ ] Đọc báo cáo giao ca
- [ ] Đếm tiền đầu ca thực tế
- [ ] So sánh với `soTienCuoiCa` trong giao ca
- [ ] Kiểm tra doanh thu và số đơn hàng (tự động hoặc điều chỉnh)
- [ ] Điều chỉnh thông tin nếu cần
- [ ] Bấm "Xác nhận giao ca"
- [ ] Bắt đầu làm việc

---

## 🎓 KẾT LUẬN

Hệ thống giao ca được thiết kế để:

1. ✅ **Tự động hóa:** Giảm thiểu nhập liệu thủ công
2. ✅ **Chính xác:** Tính toán từ dữ liệu thực tế (`HoaDon`)
3. ✅ **Minh bạch:** Lưu lịch sử mọi thao tác
4. ✅ **Linh hoạt:** Cho phép điều chỉnh thủ công khi cần

**Quy trình hoạt động mượt mà, tự động và chính xác!** 🚀






