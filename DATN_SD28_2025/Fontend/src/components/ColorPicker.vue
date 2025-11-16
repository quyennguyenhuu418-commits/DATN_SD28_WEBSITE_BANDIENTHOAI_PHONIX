<template>
  <div class="color-picker-container">
    <div class="color-picker-header">
      <h3>Chọn màu sắc</h3>
      <button @click="closePicker" class="close-btn">×</button>
    </div>
    
    <div class="color-picker-content">
      <!-- Color Grid -->
      <div class="color-grid">
        <div 
          v-for="color in colorPalette" 
          :key="color.hex"
          class="color-swatch"
          :style="{ backgroundColor: color.hex }"
          :class="{ selected: selectedColor?.hex === color.hex }"
          @click="selectColor(color)"
          :title="`${color.name} - ${color.hex}`"
        ></div>
      </div>
      
      <!-- Selected Color Info -->
      <div v-if="selectedColor" class="selected-color-info">
        <div class="color-preview" :style="{ backgroundColor: selectedColor.hex }"></div>
        <div class="color-details">
          <div class="color-name">{{ selectedColor.name }}</div>
          <div class="color-hex">{{ selectedColor.hex }}</div>
        </div>
      </div>
      
      <!-- Custom Color Input -->
      <div class="custom-color-section">
        <h4>Màu tùy chỉnh</h4>
        <div class="custom-inputs">
          <div class="input-group">
            <label>Chọn màu:</label>
            <input 
              type="color" 
              v-model="customColor"
              @change="handleCustomColor"
              class="color-input"
            />
          </div>
          <div class="input-group">
            <label>Mã Hex:</label>
            <input 
              type="text" 
              v-model="customHex"
              @input="handleHexInput"
              @blur="validateHex"
              placeholder="#000000"
              class="hex-input"
            />
          </div>
          <div class="input-group">
            <label>RGB:</label>
            <div class="rgb-inputs">
              <input 
                type="number" 
                v-model="rgbValues.r"
                @input="updateFromRGB"
                min="0" 
                max="255"
                placeholder="R"
                class="rgb-input"
              />
              <input 
                type="number" 
                v-model="rgbValues.g"
                @input="updateFromRGB"
                min="0" 
                max="255"
                placeholder="G"
                class="rgb-input"
              />
              <input 
                type="number" 
                v-model="rgbValues.b"
                @input="updateFromRGB"
                min="0" 
                max="255"
                placeholder="B"
                class="rgb-input"
              />
            </div>
          </div>
        </div>
        
        <!-- Custom Color Preview -->
        <div v-if="customHex" class="custom-preview">
          <div class="color-preview" :style="{ backgroundColor: customHex }"></div>
          <div class="color-details">
            <div class="color-name">Màu tùy chỉnh</div>
            <div class="color-hex">{{ customHex }}</div>
            <div class="color-rgb">RGB({{ rgbValues.r }}, {{ rgbValues.g }}, {{ rgbValues.b }})</div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="color-picker-actions">
      <button @click="closePicker" class="btn-cancel">Hủy</button>
      <button @click="confirmColor" class="btn-confirm" :disabled="!selectedColor">Xác nhận</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Color {
  name: string
  hex: string
  category: string
}

const emit = defineEmits<{
  select: [color: Color]
  close: []
}>()

const selectedColor = ref<Color | null>(null)
const customColor = ref('#000000')
const customHex = ref('')
const rgbValues = ref({ r: 0, g: 0, b: 0 })

// Color palette with common colors
const colorPalette = ref<Color[]>([
  // Basic Colors
  { name: 'Đen', hex: '#000000', category: 'Cơ bản' },
  { name: 'Trắng', hex: '#FFFFFF', category: 'Cơ bản' },
  { name: 'Xám', hex: '#808080', category: 'Cơ bản' },
  { name: 'Bạc', hex: '#C0C0C0', category: 'Cơ bản' },
  
  // Red Colors
  { name: 'Đỏ', hex: '#FF0000', category: 'Đỏ' },
  { name: 'Đỏ đậm', hex: '#8B0000', category: 'Đỏ' },
  { name: 'Đỏ nhạt', hex: '#FFB6C1', category: 'Đỏ' },
  { name: 'Hồng', hex: '#FF69B4', category: 'Đỏ' },
  { name: 'Hồng phấn', hex: '#FFC0CB', category: 'Đỏ' },
  
  // Orange Colors
  { name: 'Cam', hex: '#FF8C00', category: 'Cam' },
  { name: 'Cam đậm', hex: '#FF4500', category: 'Cam' },
  { name: 'Cam nhạt', hex: '#FFE4B5', category: 'Cam' },
  { name: 'Vàng', hex: '#FFD700', category: 'Cam' },
  { name: 'Vàng nhạt', hex: '#FFFFE0', category: 'Cam' },
  
  // Green Colors
  { name: 'Xanh lá', hex: '#00FF00', category: 'Xanh lá' },
  { name: 'Xanh lá đậm', hex: '#006400', category: 'Xanh lá' },
  { name: 'Xanh lá nhạt', hex: '#90EE90', category: 'Xanh lá' },
  { name: 'Xanh mint', hex: '#98FB98', category: 'Xanh lá' },
  { name: 'Xanh olive', hex: '#808000', category: 'Xanh lá' },
  
  // Blue Colors
  { name: 'Xanh dương', hex: '#0000FF', category: 'Xanh dương' },
  { name: 'Xanh dương đậm', hex: '#00008B', category: 'Xanh dương' },
  { name: 'Xanh dương nhạt', hex: '#ADD8E6', category: 'Xanh dương' },
  { name: 'Xanh navy', hex: '#000080', category: 'Xanh dương' },
  { name: 'Xanh cyan', hex: '#00FFFF', category: 'Xanh dương' },
  { name: 'Xanh teal', hex: '#008080', category: 'Xanh dương' },
  
  // Purple Colors
  { name: 'Tím', hex: '#800080', category: 'Tím' },
  { name: 'Tím đậm', hex: '#4B0082', category: 'Tím' },
  { name: 'Tím nhạt', hex: '#DDA0DD', category: 'Tím' },
  { name: 'Tím lavender', hex: '#E6E6FA', category: 'Tím' },
  
  // Brown Colors
  { name: 'Nâu', hex: '#8B4513', category: 'Nâu' },
  { name: 'Nâu đậm', hex: '#654321', category: 'Nâu' },
  { name: 'Nâu nhạt', hex: '#D2B48C', category: 'Nâu' },
  { name: 'Be', hex: '#F5F5DC', category: 'Nâu' },
  { name: 'Vàng đồng', hex: '#CD7F32', category: 'Nâu' },
  { name: 'Đồng', hex: '#B87333', category: 'Nâu' },
  
  // Special Colors
  { name: 'Vàng kim', hex: '#FFD700', category: 'Đặc biệt' },
  { name: 'Bạc kim', hex: '#C0C0C0', category: 'Đặc biệt' },
  { name: 'Xanh ngọc', hex: '#00CED1', category: 'Đặc biệt' },
  { name: 'Đỏ ruby', hex: '#E0115F', category: 'Đặc biệt' },
  { name: 'Vàng chanh', hex: '#CCFF00', category: 'Đặc biệt' }
])

function selectColor(color: Color) {
  selectedColor.value = color
}

// Convert hex to RGB
function hexToRgb(hex: string) {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : null
}

// Convert RGB to hex
function rgbToHex(r: number, g: number, b: number) {
  const toHex = (n: number) => {
    const hex = Math.max(0, Math.min(255, Math.round(n))).toString(16)
    return hex.length === 1 ? '0' + hex : hex
  }
  return `#${toHex(r)}${toHex(g)}${toHex(b)}`
}

function handleCustomColor() {
  const hex = customColor.value
  customHex.value = hex
  
  const rgb = hexToRgb(hex)
  if (rgb) {
    rgbValues.value = rgb
  }
  
  selectedColor.value = {
    name: `Màu tùy chỉnh`,
    hex: hex,
    category: 'Tùy chỉnh'
  }
}

function handleHexInput() {
  let hex = customHex.value.trim()
  
  // Add # if missing
  if (!hex.startsWith('#')) {
    hex = '#' + hex
  }
  
  if (hex.match(/^#[0-9A-Fa-f]{6}$/)) {
    customColor.value = hex
    customHex.value = hex
    
    const rgb = hexToRgb(hex)
    if (rgb) {
      rgbValues.value = rgb
    }
    
    selectedColor.value = {
      name: `Màu tùy chỉnh`,
      hex: hex,
      category: 'Tùy chỉnh'
    }
  }
}

function validateHex() {
  let hex = customHex.value.trim()
  
  if (!hex.startsWith('#')) {
    hex = '#' + hex
  }
  
  if (!hex.match(/^#[0-9A-Fa-f]{6}$/)) {
    // Reset to previous valid value or default
    customHex.value = customColor.value
  }
}

function updateFromRGB() {
  const hex = rgbToHex(rgbValues.value.r, rgbValues.value.g, rgbValues.value.b)
  customColor.value = hex
  customHex.value = hex
  
  selectedColor.value = {
    name: `Màu tùy chỉnh`,
    hex: hex,
    category: 'Tùy chỉnh'
  }
}

function confirmColor() {
  if (selectedColor.value) {
    emit('select', selectedColor.value)
  }
}

function closePicker() {
  emit('close')
}
</script>

<style scoped>
.color-picker-container {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  z-index: 1000;
  width: 600px;
  max-width: 90vw;
  max-height: 80vh;
  overflow: hidden;
}

.color-picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-bottom: 1px solid #e9ecef;
}

.color-picker-header h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #e9ecef;
}

.color-picker-content {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(40px, 1fr));
  gap: 8px;
  margin-bottom: 20px;
}

.color-swatch {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
  position: relative;
}

.color-swatch:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 10;
}

.color-swatch.selected {
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
}

.selected-color-info {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 20px;
}

.color-preview {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  border: 2px solid #ddd;
}

.color-details {
  flex: 1;
}

.color-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.color-hex {
  font-family: 'Courier New', monospace;
  color: #666;
  font-size: 14px;
}

.custom-color-section {
  margin-top: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  border: 2px solid #e9ecef;
}

.custom-color-section h4 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 16px;
  font-weight: 600;
}

.custom-inputs {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-group label {
  font-weight: 500;
  color: #555;
  font-size: 14px;
}

.color-input {
  width: 60px;
  height: 40px;
  border: 2px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.color-input:hover {
  border-color: #007bff;
}

.hex-input {
  padding: 10px 12px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  transition: border-color 0.2s;
}

.hex-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.rgb-inputs {
  display: flex;
  gap: 8px;
}

.rgb-input {
  flex: 1;
  padding: 10px 8px;
  border: 2px solid #ddd;
  border-radius: 8px;
  text-align: center;
  font-size: 14px;
  transition: border-color 0.2s;
}

.rgb-input:focus {
  outline: none;
  border-color: #007bff;
  box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.1);
}

.custom-preview {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-top: 15px;
  padding: 15px;
  background: white;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.custom-preview .color-preview {
  width: 50px;
  height: 50px;
  border-radius: 8px;
  border: 2px solid #ddd;
}

.custom-preview .color-details {
  flex: 1;
}

.custom-preview .color-name {
  font-weight: 600;
  color: #333;
  margin-bottom: 5px;
}

.custom-preview .color-hex {
  font-family: 'Courier New', monospace;
  color: #666;
  font-size: 14px;
  margin-bottom: 3px;
}

.custom-preview .color-rgb {
  font-family: 'Courier New', monospace;
  color: #888;
  font-size: 12px;
}

.color-picker-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  background: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.btn-cancel,
.btn-confirm {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
}

.btn-cancel {
  background: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background: #5a6268;
}

.btn-confirm {
  background: #007bff;
  color: white;
}

.btn-confirm:hover:not(:disabled) {
  background: #0056b3;
}

.btn-confirm:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 768px) {
  .color-picker-container {
    width: 95vw;
    max-height: 90vh;
  }
  
  .color-grid {
    grid-template-columns: repeat(auto-fill, minmax(35px, 1fr));
  }
  
  .color-swatch {
    width: 35px;
    height: 35px;
  }
  
  .custom-inputs {
    gap: 12px;
  }
  
  .rgb-inputs {
    gap: 6px;
  }
  
  .rgb-input {
    padding: 8px 6px;
    font-size: 12px;
  }
  
  .custom-preview {
    flex-direction: column;
    text-align: center;
    gap: 10px;
  }
}
</style>
