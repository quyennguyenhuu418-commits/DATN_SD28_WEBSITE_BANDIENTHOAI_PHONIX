# FontAwesome Usage Guide

## Cách sử dụng FontAwesome trong dự án

### 1. Khai báo toàn cục
FontAwesome đã được khai báo toàn cục trong `main.js`, bạn không cần import trong từng component.

### 2. Sử dụng trong template
```vue
<template>
  <!-- Solid icons (mặc định) -->
  <font-awesome-icon icon="user" />
  <font-awesome-icon icon="plus" />
  <font-awesome-icon icon="trash" />
  
  <!-- Regular icons -->
  <font-awesome-icon :icon="['far', 'star']" />
  
  <!-- Brand icons -->
  <font-awesome-icon :icon="['fab', 'github']" />
  
  <!-- Với size và color -->
  <font-awesome-icon icon="user" size="2x" color="blue" />
  
  <!-- Với class CSS -->
  <font-awesome-icon icon="user" class="my-icon" />
</template>
```

### 3. Sử dụng trong JavaScript
```javascript
// Trong component
const iconName = 'user'
const iconStyle = 'fas' // fas, far, fab

// Trong template
<font-awesome-icon :icon="[iconStyle, iconName]" />
```

### 4. Danh sách icon thường dùng
Xem file `src/plugins/fontawesome.js` để biết danh sách icon có sẵn.

### 5. Thêm icon mới
Nếu cần thêm icon mới, chỉ cần sử dụng trực tiếp trong template:
```vue
<font-awesome-icon icon="new-icon-name" />
```

### 6. Lưu ý
- Tất cả icon đã được load toàn cục, không cần import
- Sử dụng `fas` (solid) làm mặc định
- Sử dụng `far` cho regular icons
- Sử dụng `fab` cho brand icons





