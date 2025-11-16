# Phân tích Logic Trả Hàng

## Tình huống ban đầu:
- **Tổng đơn ban đầu**: 1,000,000 VNĐ
- **Khuyến mãi**: 10% cho đơn ≥ 500K, tối đa giảm 800K
- **Voucher**: 5% cho đơn ≥ 200K, tối đa giảm 400K
- **Tiền khách đã trả**: 600,000 VNĐ
- **Đơn còn lại**: 400,000 VNĐ
- **Tiền ban đầu cửa hàng đưa**: 900,000 VNĐ

## Logic tính toán:

### 1. Tính lại hóa đơn cũ đã giảm bao nhiêu tiền:

**Khuyến mãi ban đầu:**
- Đơn: 1,000,000 VNĐ
- 10% của 1,000,000 = 100,000 VNĐ (≤ 800,000 → áp dụng được)
- → Khuyến mãi giảm: **100,000 VNĐ**

**Voucher ban đầu:**
- Đơn sau khuyến mãi: 1,000,000 - 100,000 = 900,000 VNĐ
- 5% của 900,000 = 45,000 VNĐ (≤ 400,000 → áp dụng được)
- → Voucher giảm: **45,000 VNĐ**

**Tổng giảm giá ban đầu**: 100,000 + 45,000 = **145,000 VNĐ**
**Tổng tiền thực tế khách phải trả**: 1,000,000 - 145,000 = **855,000 VNĐ**

### 2. Khi trả hàng (đơn còn lại 400K):

**Kiểm tra điều kiện khuyến mãi 10%:**
- Đơn còn lại: 400,000 VNĐ < 500,000 VNĐ
- → **KHÔNG còn thỏa mãn điều kiện khuyến mãi 10%**

**Voucher 5% có thể áp dụng:**
- Đơn còn lại: 400,000 VNĐ ≥ 200,000 VNĐ
- 5% của 400,000 = 20,000 VNĐ (≤ 400,000 → áp dụng được)
- → Voucher có thể giảm: **20,000 VNĐ**

### 3. Tính toán tiền trả lại:

**Tình huống 1: Nếu tính theo giá gốc không giảm giá:**
- Tiền hoàn lại = 400,000 VNĐ (giá gốc của phần trả)
- Khách đã trả: 600,000 VNĐ
- Khách phải trả: 855,000 VNĐ
- Khách còn nợ: 855,000 - 600,000 = **255,000 VNĐ**

**Tình huống 2: Nếu tính theo giá đã giảm:**
- Tiền hoàn lại = 400,000 VNĐ
- Nhưng khách đã được giảm giá trên tổng đơn, nên cần tính lại

**Tình huống 3: Tính theo tỷ lệ giảm giá đã áp dụng:**
- Tỷ lệ giảm giá trên đơn ban đầu: 145,000 / 1,000,000 = 14.5%
- Tiền hoàn lại theo tỷ lệ: 400,000 × (1 - 0.145) = **342,000 VNĐ**
- Khách còn nợ: 855,000 - 600,000 - 342,000 = **-87,000 VNĐ** (cửa hàng phải trả lại)

### 4. Vấn đề trong logic người dùng mô tả:

Người dùng tính:
- 900 - 600 = 300 (sai, vì 900 là tiền sau khuyến mãi, không phải tổng đơn)
- 400 × 5% = 20
- 900 - 660 = 540
- 400 - 40 = 360 → Khách nợ 40K
- 400 - 20 = 380 → 20K khách được

**Vấn đề:**
1. Logic tính toán không rõ ràng
2. Không có cách tính nhất quán
3. Có vẻ như có lỗi trong cách tính "tiền ban đầu cửa hàng đưa" (900K)

## Logic đề xuất:

### Cách 1: Tính theo giá gốc (đơn giản nhất)
```
Tiền hoàn lại = Giá gốc sản phẩm trả × Số lượng
→ Tiền hoàn lại = 400,000 VNĐ
→ Khách còn nợ = 855,000 - 600,000 = 255,000 VNĐ
→ Sau khi hoàn lại: 255,000 - 400,000 = -145,000 VNĐ (cửa hàng trả lại)
```

### Cách 2: Tính theo tỷ lệ giảm giá đã áp dụng
```
Tỷ lệ giảm = Tổng giảm / Tổng đơn = 145,000 / 1,000,000 = 14.5%
Tiền hoàn lại = 400,000 × (1 - 0.145) = 342,000 VNĐ
Khách còn nợ = 855,000 - 600,000 = 255,000 VNĐ
Sau khi hoàn lại: 255,000 - 342,000 = -87,000 VNĐ (cửa hàng trả lại)
```

### Cách 3: Tính lại giảm giá cho đơn còn lại (phức tạp nhất)
```
Đơn còn lại: 400,000 VNĐ
- Khuyến mãi 10%: KHÔNG áp dụng (400K < 500K)
- Voucher 5%: Áp dụng được (400K ≥ 200K) → Giảm 20,000 VNĐ
→ Tiền hoàn lại = 400,000 - 20,000 = 380,000 VNĐ

Khách đã trả: 600,000 VNĐ
Khách phải trả: 855,000 VNĐ
Khách còn nợ: 255,000 VNĐ
Sau khi hoàn lại: 255,000 - 380,000 = -125,000 VNĐ (cửa hàng trả lại)
```

## Khuyến nghị:

**Nên sử dụng Cách 3** vì:
1. Tính lại giảm giá cho đơn còn lại là công bằng nhất
2. Đảm bảo khách không bị thiệt khi trả hàng
3. Cửa hàng chỉ chịu phần giảm giá thực tế trên sản phẩm đã trả

## Logic cần implement:

1. **Tính lại khuyến mãi cho đơn còn lại:**
   - Kiểm tra đơn còn lại có thỏa mãn điều kiện khuyến mãi không
   - Nếu có, áp dụng khuyến mãi cho đơn còn lại

2. **Tính lại voucher cho đơn còn lại:**
   - Kiểm tra đơn còn lại có thỏa mãn điều kiện voucher không
   - Nếu có, áp dụng voucher cho đơn còn lại

3. **Tính tiền hoàn lại:**
   - Tiền hoàn lại = Tổng giá sản phẩm trả - Giảm giá mới tính cho phần trả

4. **Tính tiền khách còn nợ hoặc cửa hàng phải trả lại:**
   - Tiền khách phải trả (sau giảm giá ban đầu) = Tổng đơn - Tổng giảm giá ban đầu
   - Tiền khách đã trả
   - Tiền hoàn lại (đã tính giảm giá mới)
   - → Chênh lệch = Tiền khách phải trả - Tiền đã trả - Tiền hoàn lại

