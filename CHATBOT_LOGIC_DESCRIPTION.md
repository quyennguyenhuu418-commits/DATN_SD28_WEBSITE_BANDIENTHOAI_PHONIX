# Hệ Thống Logic Chatbot Tư Vấn Điện Thoại

## Tổng Quan
Hệ thống chatbot được xây dựng với logic thông minh, có khả năng phân tích ý định người dùng và tư vấn chính xác về sản phẩm điện thoại.

## Kiến Trúc Logic

### 1. Phân Tích Ý Định (Intent Analysis)
Hệ thống sử dụng `IntentAnalysis` class để phân tích và hiểu ý định người dùng:

#### Các Loại Ý Định Được Hỗ Trợ:
- **product_search**: Tìm kiếm sản phẩm
- **comparison**: So sánh sản phẩm  
- **order_tracking**: Tra cứu đơn hàng
- **price_inquiry**: Hỏi về giá cả
- **spec_inquiry**: Hỏi về thông số kỹ thuật
- **brand_preference**: Sở thích thương hiệu
- **greeting**: Chào hỏi
- **help**: Yêu cầu trợ giúp

#### Trích Xuất Thực Thể (Entity Extraction):
- **RAM**: Tự động nhận diện "8GB RAM", "12GB RAM"
- **ROM**: Nhận diện "128GB ROM", "256GB storage"
- **Giá cả**: "dưới 15 triệu", "từ 10 đến 20 triệu"
- **Thương hiệu**: Apple, Samsung, Xiaomi, OPPO, Vivo
- **Số đơn hàng**: PX1234, mã đơn
- **Số điện thoại**: 09xxxxxxxx

### 2. Tìm Kiếm Nâng Cao (Advanced Search)
Hệ thống `SearchCriteria` hỗ trợ tìm kiếm đa tiêu chí:

#### Các Tiêu Chí Tìm Kiếm:
- **Giá cả**: minPrice, maxPrice
- **RAM/ROM**: ramId, romId
- **Hệ điều hành**: osName (iOS, Android, One UI, MIUI)
- **Thương hiệu**: brandId, brandName
- **Chip**: chip, cpuName, gpu
- **Pin**: minBattery, maxBattery
- **Màn hình**: minScreen, maxScreen
- **Camera**: rearCam, frontCam
- **Sim**: simType

#### Ví Dụ Truy Vấn Thông Minh:
```
"iPhone 15 Pro 256GB" → Tìm theo tên cụ thể
"dưới 15 triệu RAM 8GB" → Tìm theo giá và cấu hình
"Samsung camera tốt" → Tìm theo thương hiệu và tính năng
"Android dưới 10 triệu" → Tìm theo OS và giá
```

### 3. So Sánh Thông Minh (Smart Comparison)
Hệ thống `buildEnhancedComparisonInsights` cung cấp:

#### Tính Năng So Sánh:
- **Điểm hiệu năng**: Tính toán dựa trên RAM, ROM, Pin, Chip
- **Đánh giá giá trị**: So sánh hiệu năng/giá tiền
- **Gợi ý thông minh**: Sản phẩm tốt nhất theo từng tiêu chí
- **Highlighting**: Làm nổi bật thông số quan trọng

#### Công Thức Tính Điểm:
```
Performance Score = RAM(25%) + ROM(20%) + Battery(20%) + Screen(15%) + Chip(20%)
Overall Score = Performance(40%) + Value(30%) + Brand(15%) + Features(15%)
```

### 4. Tư Vấn Theo Ngữ Cảnh (Context-Aware Consultation)
Hệ thống `generateSmartFallbackReply` cung cấp:

#### Tư Vấn Thông Minh:
- **Phân tích ngữ cảnh**: Hiểu rõ nhu cầu người dùng
- **Gợi ý cụ thể**: Dựa trên thông tin đã trích xuất
- **Hướng dẫn chi tiết**: Cách sử dụng từng tính năng
- **Gợi ý tiếp theo**: Các hành động có thể thực hiện

#### Ví Dụ Tư Vấn:
```
Người dùng: "Tôi cần điện thoại chụp ảnh tốt"
Bot: "📸 Tôi hiểu bạn quan tâm đến camera! 
     💡 Gợi ý tìm kiếm:
     • 'iPhone 15 Pro camera' - camera xuất sắc
     • 'Samsung Galaxy S24 Ultra' - camera đa năng
     • 'Google Pixel 8' - xử lý ảnh AI"
```

### 5. Giao Diện Thông Minh (Smart UI)
Frontend được cập nhật để hỗ trợ:

#### Hiển Thị Nâng Cao:
- **Thông số chi tiết**: RAM, ROM, OS, Chip, Pin, Camera
- **Highlighting**: Làm nổi bật thông số quan trọng
- **Điểm hiệu năng**: Hiển thị điểm đánh giá
- **Gợi ý động**: Quick replies thông minh

#### Ví Dụ Giao Diện:
```
⭐ 8GB RAM    💾 256GB ROM    🔋 4000mAh    📱 120Hz
🍎 iOS        🏆 Điểm: 85/100    💰 25 triệu
```

## Luồng Xử Lý Logic

### 1. Nhận Tin Nhắn
```
User Input → Intent Analysis → Entity Extraction → Context Building
```

### 2. Phân Tích & Định Tuyến
```
Intent Type → Route to Handler → Generate Response → Smart Fallback
```

### 3. Tạo Phản Hồi
```
Context + Entities → AI Processing → Enhanced Response → UI Rendering
```

## Lợi Ích Của Hệ Thống

### 1. Thông Minh
- Hiểu được ý định phức tạp của người dùng
- Tư vấn dựa trên ngữ cảnh cụ thể
- Học hỏi từ các tương tác

### 2. Chính Xác
- Trích xuất thông tin chính xác từ câu nói
- So sánh sản phẩm khách quan
- Đánh giá dựa trên tiêu chí rõ ràng

### 3. Thân Thiện
- Giao diện trực quan, dễ sử dụng
- Phản hồi tự nhiên, gần gũi
- Hướng dẫn chi tiết, dễ hiểu

### 4. Linh Hoạt
- Hỗ trợ nhiều cách diễn đạt
- Mở rộng dễ dàng cho tính năng mới
- Tích hợp với các hệ thống khác

## Kết Luận
Hệ thống logic chatbot được xây dựng theo mô tả chi tiết, đảm bảo khả năng tư vấn thông minh, chính xác và thân thiện với người dùng. Hệ thống có thể mở rộng và cải tiến liên tục để phục vụ tốt hơn nhu cầu của khách hàng.
