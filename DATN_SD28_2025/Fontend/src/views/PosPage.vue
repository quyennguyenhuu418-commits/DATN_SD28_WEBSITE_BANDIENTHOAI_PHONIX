<template>
  <div class="h-screen flex flex-col bg-slate-100 overflow-hidden">
    <!-- Header -->
    <header class="bg-white shadow-sm border-b border-slate-200">
      <!-- Top Section -->
      <div class="px-6 py-4">
        <div class="flex items-center justify-between">
          <!-- Logo -->
          <div class="flex items-center space-x-3">
            <div class="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
              <span class="text-white font-bold text-lg">P</span>
            </div>
            <div>
              <h1 class="text-xl font-bold text-slate-800">POS System</h1>
              <p class="text-sm text-slate-500">Point of Sale</p>
            </div>
          </div>

          <!-- User Info and Time -->
          <div class="flex items-center space-x-4">
            <div class="text-right">
              <div class="text-sm font-medium text-slate-800">{{ currentTime }}</div>
              <div class="text-xs text-slate-500">{{ currentDate }}</div>
            </div>
            <!-- Simple User Menu Component -->
            <SimpleUserMenu />
          </div>
        </div>
      </div>

      <!-- Invoice Tabs -->
      <div class="px-6 py-3 border-t border-slate-200">
        <div class="flex items-center justify-between">
          <!-- Left Section: Product Search + Add Invoice -->
          <div class="flex items-center space-x-4">
            <!-- Product Search -->
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <svg class="h-4 w-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                </svg>
              </div>
              <input
                type="text"
                v-model="searchQuery"
                placeholder="Tìm sản phẩm... (F3)"
                @input="searchProducts"
                @focus="showProductSuggestions = true"
                @blur="hideProductSuggestions"
                class="w-[488px] pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
              />

              <!-- Product Suggestions Dropdown -->
              <div v-if="showProductSuggestions" class="absolute top-full left-0 right-0 mt-1 bg-white border border-slate-300 rounded-lg shadow-lg max-h-80 overflow-y-auto z-50">
                <div
                  v-for="product in (searchQuery ? filteredProducts : products.filter(p => p.trangThai === 1 && p.soLuongTon > 0)).slice(0, 10)"
                  :key="product.chiTietSanPhamId"
                  @click="addToCartFromSearch(product)"
                  class="flex items-center justify-between p-4 hover:bg-slate-50 cursor-pointer border-b border-slate-100 last:border-b-0"
                >
                  <!-- Product Image -->
                  <div class="w-12 h-12 bg-slate-200 rounded-lg flex items-center justify-center flex-shrink-0">
                    <img
                      v-if="product.hinhAnh"
                      :src="product.hinhAnh"
                      :alt="product.tenSanPham"
                      class="w-full h-full object-cover rounded-lg"
                    />
                    <svg v-else class="w-6 h-6 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"></path>
                    </svg>
                  </div>

                  <!-- Product Info -->
                  <div class="flex-1 ml-3">
                    <h4 class="font-medium text-slate-800 text-sm">{{ product.tenSanPham }}</h4>
                    <p class="text-xs text-slate-500">{{ product.maCtsp || 'N/A' }}</p>
                    <p class="text-xs text-slate-500">Tồn: {{ product.soLuongTon }} | KH đặt: 0</p>
                  </div>

                  <!-- Price -->
                  <div class="text-right">
                    <div v-if="product.giaGoc && product.giaGoc > product.gia" class="text-xs line-through text-slate-400">
                      {{ formatCurrency(product.giaGoc) }}
                    </div>
                    <div class="text-lg font-bold text-black">{{ formatCurrency(product.gia) }}</div>
                    <div v-if="product.giamPhanTram" class="text-xs text-green-600">-{{ Math.round(product.giamPhanTram) }}%</div>
                  </div>
                </div>

                <!-- No Results State -->
                <div v-if="searchQuery && filteredProducts.length === 0" class="p-4 text-center text-slate-500">
                  <svg class="w-12 h-12 mx-auto mb-2 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                  </svg>
                  <p class="text-sm font-medium mb-1">Không tìm thấy sản phẩm</p>
                  <p class="text-xs">Thử thay đổi từ khóa tìm kiếm</p>
                </div>

                <!-- Add New Product Button -->
                <div class="p-3 border-t border-slate-200 bg-slate-50">
                  <button
                    @click="showProductModal = true; showProductSuggestions = false"
                    class="w-full py-2 text-sm text-orange-600 hover:text-orange-700 flex items-center justify-center space-x-2"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                    </svg>
                    <span>Thêm mới hàng hóa</span>
                  </button>
                </div>
              </div>
            </div>

            <button
              @click="addNewInvoice"
              class="flex items-center justify-center w-10 h-8 bg-orange-400 hover:bg-orange-600 rounded-lg text-white transition-all"
              title="Thêm hóa đơn mới"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
              </svg>
            </button>

            <!-- Current Invoice Display -->
            <div class="flex items-center space-x-2 text-sm">
              <span class="text-slate-600">Đây là hóa đơn:</span>
              <span class="font-bold text-orange-600 bg-orange-50 px-2 py-1 rounded">{{ activeInvoiceId }}</span>
              <span class="text-slate-500">/ {{ invoices.length }}</span>
            </div>
          </div>

          <!-- Center Section: Invoice Navigation -->
          <div class="flex items-center space-x-2 -ml-8">
            <!-- Previous Button -->
            <button
              @click="previousInvoice"
              :disabled="currentInvoiceIndex <= 0"
              class="flex items-center justify-center w-8 h-8 bg-slate-200 hover:bg-slate-300 disabled:bg-slate-100 disabled:text-slate-400 rounded-lg text-slate-600 transition-all"
              title="Hóa đơn trước"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
              </svg>
            </button>

            <!-- Invoice Tabs Container (5 tabs max) -->
            <div class="flex space-x-2">
              <div
                v-for="(invoice, index) in visibleInvoices"
                :key="invoice.id"
                @click="setActiveInvoice(invoice.id)"
                @dragstart="onDragStart($event, getActualIndex(invoice.id))"
                @dragover="onDragOver($event)"
                @drop="onDrop($event, getActualIndex(invoice.id))"
                draggable="true"
                :class="[
                  'relative group flex items-center justify-between min-w-[110px] px-3 py-2 rounded-lg border transition-all cursor-move',
                  invoice.id === activeInvoiceId
                    ? 'bg-orange-500 text-white border-orange-500'
                    : 'bg-white text-slate-700 border-slate-300 hover:border-orange-300'
                ]"
              >
                <div class="flex items-center space-x-2">
                  <span class="text-sm">Hóa đơn {{ invoice.id }}</span>
                  <div class="text-xs opacity-75" v-if="invoice.cartItems.length > 0">
                    ({{ invoice.cartItems.length }})
                  </div>
                </div>
                <button
                  @click.stop="closeInvoice(invoice.id)"
                  :title="`Đóng hóa đơn ${invoice.id}`"
                  :class="[
                    'w-4 h-4 rounded-full bg-red-500 hover:bg-red-600 text-white flex items-center justify-center text-xs font-bold transition-all',
                    'absolute top-1/2 right-2 transform -translate-y-1/2',
                    invoice.id === activeInvoiceId
                      ? 'opacity-100'
                      : 'opacity-0 group-hover:opacity-100'
                  ]"
                  v-if="invoices.length > 1"
                >
                  ×
                </button>
              </div>
            </div>

            <!-- Next Button -->
            <button
              @click="nextInvoice"
              :disabled="currentInvoiceIndex >= invoices.length - 1"
              class="flex items-center justify-center w-8 h-8 bg-slate-200 hover:bg-slate-300 disabled:bg-slate-100 disabled:text-slate-400 rounded-lg text-slate-600 transition-all"
              title="Hóa đơn tiếp theo"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
              </svg>
            </button>
          </div>

          <!-- Right Section: Invoice Count -->
          <div class="text-sm text-slate-500">
            Tổng: {{ invoices.length }} hóa đơn
          </div>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <div class="flex-1 flex overflow-hidden">
      <!-- Products Section (Left) -->
      <main class="flex-1 bg-white flex flex-col min-h-0">
        <!-- Product List -->
        <div class="flex-1 p-4 overflow-y-auto">
          <!-- Product Items -->
          <div class="space-y-3">
            <div
              v-for="(item, index) in currentInvoice.cartItems"
              :key="item.cartItemId || `${item.chiTietSanPhamId}_${index}`"
              :class="[
                'flex items-center justify-between p-4 rounded-lg border',
                item.priceChanged ? 'bg-yellow-50 border-yellow-300 border-2' : 'bg-slate-50 border-slate-200'
              ]"
            >
              <!-- Product Image -->
              <div class="w-12 h-12 bg-slate-200 rounded-lg flex items-center justify-center flex-shrink-0">
                <img
                  v-if="item.hinhAnh"
                  :src="item.hinhAnh"
                  :alt="item.tenSanPham"
                  class="w-full h-full object-cover rounded-lg"
                />
                <svg v-else class="w-6 h-6 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"></path>
                </svg>
              </div>

              <!-- Product Info -->
              <div class="flex-1 ml-3">
                <h4 class="font-medium text-slate-800 text-sm">
                  {{ item.tenSanPham }}
                  <span v-if="item.selectedImeis && item.selectedImeis.length > 0" class="text-xs text-blue-600 font-normal">
                    ({{ item.selectedImeis.length }}/{{ item.quantity }} IMEI: {{ item.selectedImeis.join(', ') }})
                  </span>
                  <!-- Warning badge nếu giá đã thay đổi -->
                  <span v-if="item.priceChanged" class="ml-2 inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-yellow-100 text-yellow-800">
                    ⚠️ Giá đã thay đổi
                  </span>
                </h4>
                <p class="text-xs text-slate-500">{{ item.tenHang || 'N/A' }} • {{ item.tenRam || 'N/A' }} • {{ item.tenRom || 'N/A' }} • {{ item.tenMauSac || 'N/A' }}</p>
                <p class="text-xs text-slate-500">Tồn: {{ item.soLuongTon }} | KH đặt: 0</p>
                <!-- Hiển thị giá cũ nếu giá đã thay đổi -->
                <p v-if="item.priceChanged && item.oldPrice" class="text-xs text-yellow-600 font-medium mt-1">
                  Giá cũ: <span class="line-through">{{ formatCurrency(item.oldPrice) }}</span> → Giá mới: {{ formatCurrency(item.gia) }}
                </p>
              </div>

              <!-- Price per item -->
              <div class="text-right mr-4">
                <div class="text-lg font-bold text-black">{{ formatCurrency(item.gia * (item.quantity || 1)) }}</div>
              </div>

              <!-- Quantity Controls -->
              <div class="flex items-center space-x-2">
                <button
                  @click="decreaseQuantity(item.cartItemId || item.chiTietSanPhamId)"
                  class="w-8 h-8 flex items-center justify-center bg-slate-200 hover:bg-slate-300 rounded-full text-slate-600 transition-colors"
                >
                  -
                </button>
                <div class="w-12 text-center">
                  <input
                    type="number"
                    :value="item.quantity || 1"
                    @input="updateQuantity(item.cartItemId || item.chiTietSanPhamId, $event.target.value)"
                    class="w-full text-center border border-slate-300 rounded px-1 py-1 text-sm"
                    min="1"
                    :max="item.soLuongTon"
                  />
                </div>
                <button
                  @click="increaseQuantity(item.cartItemId || item.chiTietSanPhamId)"
                  class="w-8 h-8 flex items-center justify-center bg-slate-200 hover:bg-slate-300 rounded-full text-slate-600 transition-colors"
                >
                  +
                </button>
              </div>

              <!-- Remove Button -->
              <button
                @click="removeFromCart(item.cartItemId || item.chiTietSanPhamId)"
                class="ml-2 w-8 h-8 flex items-center justify-center bg-red-100 hover:bg-red-200 rounded-full text-red-600 transition-colors"
              >
                ×
              </button>
            </div>
          </div>

          <!-- Empty State -->
          <div v-if="currentInvoice.cartItems.length === 0" class="text-center py-12 text-slate-500">
            <svg class="w-16 h-16 mx-auto mb-4 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l-1 7a2 2 0 01-2 2H8a2 2 0 01-2-2L5 9z"></path>
            </svg>
            <p class="text-lg font-medium mb-2">Chưa có sản phẩm nào</p>
            <p class="text-sm">Tìm kiếm và thêm sản phẩm vào đơn hàng</p>
          </div>
        </div>

        <!-- Note Input - Fixed at bottom -->
        <div class="flex-shrink-0 p-4 border-t border-slate-200 bg-slate-50">
          <div class="flex items-center space-x-2 mb-3">
            <svg class="w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
            </svg>
            <input
              type="text"
              v-model="currentInvoice.orderNote"
              placeholder="Ghi chú đơn hàng"
              class="flex-1 px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <!-- Sale Mode Navigation Bar (Horizontal) -->
          <div class="flex space-x-2">
            <button
              @click="saleMode = 'quick'"
              :disabled="currentInvoice.cartItems.length === 0"
              :class="[
                'flex-1 flex items-center justify-center space-x-2 px-4 py-2 rounded-lg transition-all disabled:cursor-not-allowed',
                saleMode === 'quick'
                  ? 'bg-green-600 text-white shadow-md'
                  : 'bg-slate-200 text-slate-600 hover:bg-slate-300 disabled:bg-slate-100 disabled:text-slate-400'
              ]"
              title="Bán nhanh"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
              </svg>
              <span class="text-sm font-medium">Bán nhanh</span>
            </button>

            <button
              @click="switchToDeliveryMode"
              :disabled="currentInvoice.cartItems.length === 0"
              :class="[
                'flex-1 flex items-center justify-center space-x-2 px-4 py-2 rounded-lg transition-all disabled:cursor-not-allowed',
                saleMode === 'delivery'
                  ? 'bg-orange-400 text-white shadow-md'
                  : 'bg-slate-200 text-slate-600 hover:bg-slate-300 disabled:bg-slate-100 disabled:text-slate-400'
              ]"
              title="Bán giao hàng"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"></path>
              </svg>
              <span class="text-sm font-medium">Bán giao</span>
            </button>
          </div>
        </div>
      </main>

      <!-- Payment Section (Right) - Quick Sale -->
      <aside v-if="saleMode === 'quick'" class="w-96 bg-white shadow-lg flex flex-col border-l border-slate-200 h-full overflow-hidden">
        <!-- Payment Header -->
        <div class="p-4 border-b border-slate-200">
          <div class="text-center mb-4">
            <h2 class="text-xl font-bold text-slate-800">Thanh toán</h2>
            <p class="text-sm text-slate-500 mt-1">Tìm khách hàng từ danh sách khách hàng</p>
          </div>

          <!-- Customer Search -->
          <div class="flex space-x-2">
            <div class="relative flex-1">
              <input
                type="text"
                v-model="customerSearch"
                placeholder="Tìm khách hàng (F4)"
                @input="searchCustomers"
                @focus="showCustomerSuggestions = true"
                @blur="hideCustomerSuggestions"
                class="w-full pl-10 pr-4 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
              <svg class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>

              <!-- Customer Suggestions Dropdown -->
              <div v-if="showCustomerSuggestions && filteredCustomers.length > 0" class="absolute top-full left-0 right-0 mt-1 bg-white border border-slate-300 rounded-lg shadow-lg max-h-60 overflow-y-auto z-50">
                <div
                  v-for="customer in filteredCustomers"
                  :key="customer.id"
                  @click="selectCustomer(customer)"
                  class="px-4 py-3 hover:bg-slate-50 cursor-pointer border-b border-slate-100 last:border-b-0"
                >
                  <div class="font-medium text-slate-800">{{ customer.hoTen }}</div>
                  <div class="text-sm text-slate-500">{{ customer.soDienThoai }}</div>
                  <div class="text-xs text-slate-400">{{ customer.email || 'Không có email' }}</div>
                </div>
              </div>
            </div>

            <!-- Quick Customer Button -->
            <button
              @click="openQuickCustomerModal"
              class="w-10 h-10 bg-orange-400 hover:bg-orange-600 text-white rounded-lg transition-colors flex items-center justify-center self-center"
              title="Thêm khách hàng nhanh"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
              </svg>
            </button>
          </div>

          <!-- Selected Customer Display -->
          <div v-if="currentInvoice.selectedCustomer" class="mt-3 p-3 bg-orange-50 border border-orange-200 rounded-lg">
            <div class="flex items-center justify-between">
              <div>
                <div class="font-medium text-orange-800">{{ currentInvoice.selectedCustomer.hoTen }}</div>
                <div class="text-sm text-orange-600">{{ currentInvoice.selectedCustomer.soDienThoai }}</div>
              </div>
              <button
                @click="clearSelectedCustomer"
                class="w-6 h-6 bg-red-500 hover:bg-red-600 rounded-full text-white text-xs flex items-center justify-center"
              >
                ×
              </button>
            </div>
          </div>
        </div>

        <!-- Order Summary -->
        <div v-if="saleMode === 'quick'" class="flex-1 p-4 space-y-4 overflow-y-auto hide-scroll">
          <!-- Summary Items -->
          <div class="space-y-3">
            <div class="flex justify-between items-center">
              <span class="text-slate-600">Tổng tiền hàng</span>
              <span class="font-medium text-black">{{ currentInvoice.cartItems.length }}</span>
              <span class="font-bold text-black">{{ formatCurrency(currentInvoice.subtotal) }}</span>
            </div>

            <div class="flex justify-between items-center">
              <span class="text-slate-600">Giảm giá</span>
              <span class="font-bold text-black">{{ formatCurrency(currentInvoice.discount) }}</span>
            </div>

            <div class="border-t border-slate-200 pt-3">
              <div class="flex justify-between items-center text-lg">
                <span class="font-semibold text-slate-800">Khách cần trả</span>
                <span class="font-bold text-black">{{ formatCurrency(currentInvoice.total) }}</span>
              </div>
            </div>

            <!-- Cash Payment Fields -->
            <div v-if="currentInvoice.paymentMethod === 'cash'">
              <div class="flex justify-between items-center text-lg">
                <span class="font-semibold text-slate-800">Khách thanh toán</span>
                <input
                  type="text"
                  v-model="formattedCustomerPaid"
                  @input="handleCustomerPaidInput"
                  class="w-32 text-right font-bold text-lg px-2 py-1 border border-slate-300 rounded focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                  placeholder="0"
                />
              </div>

              <!-- Change Amount -->
              <div class="flex justify-between items-center text-lg" v-if="currentInvoice.customerPaid > currentInvoice.total">
                <span class="font-semibold text-green-600">Tiền thừa</span>
                <span class="font-bold text-black">{{ formatCurrency(currentInvoice.customerPaid - currentInvoice.total) }}</span>
              </div>
            </div>

            <!-- VNPay Payment Info -->
            <div v-if="currentInvoice.paymentMethod === 'vnpay'" class="p-3 bg-blue-50 border border-blue-200 rounded-lg">
              <div class="flex items-center space-x-2">
                <svg class="w-5 h-5 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <span class="text-sm font-medium text-blue-800">Thanh toán qua VNPay</span>
              </div>
              <p class="text-xs text-blue-600 mt-1">Khách hàng sẽ được chuyển hướng đến trang thanh toán VNPay</p>
            </div>
          </div>

          <!-- Payment Method Selection -->
          <div class="space-y-3">
            <div class="flex items-center space-x-4">
              <label class="flex items-center space-x-2">
                <input type="radio" name="paymentMethod" value="cash" v-model="currentInvoice.paymentMethod" @change="resetCombinedPaymentFields" class="text-orange-600">
                <span class="text-sm">Tiền mặt</span>
              </label>
              <label class="flex items-center space-x-2">
                <input type="radio" name="paymentMethod" value="vnpay" v-model="currentInvoice.paymentMethod" @change="resetCombinedPaymentFields" class="text-orange-600">
                <span class="text-sm">VNPay</span>
              </label>
              <label class="flex items-center space-x-2">
                <input type="radio" name="paymentMethod" value="combined" v-model="currentInvoice.paymentMethod" @change="resetCombinedPaymentFields" class="text-orange-600">
                <span class="text-sm">Kết hợp</span>
              </label>
            </div>
          </div>

          <!-- Combined Payment Fields -->
          <div v-if="currentInvoice.paymentMethod === 'combined'" class="space-y-3">
            <div class="p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
              <div class="flex items-center space-x-2 mb-2">
                <svg class="w-5 h-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                <span class="text-sm font-medium text-yellow-800">Thanh toán kết hợp</span>
              </div>
              <p class="text-xs text-yellow-600">Chia thanh toán giữa tiền mặt và VNPay</p>
            </div>

            <!-- Cash Amount -->
            <div class="flex justify-between items-center">
              <span class="font-semibold text-slate-800">Tiền mặt</span>
              <input
                type="text"
                v-model="formattedCashAmount"
                @input="handleCashAmountInput"
                class="w-32 text-right font-bold text-lg px-2 py-1 border border-slate-300 rounded focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                placeholder="0"
              />
            </div>

            <!-- VNPay Amount -->
            <div class="flex justify-between items-center">
              <span class="font-semibold text-slate-800">VNPay</span>
              <input
                type="text"
                v-model="formattedVnpayAmount"
                @input="handleVnpayAmountInput"
                class="w-32 text-right font-bold text-lg px-2 py-1 border border-slate-300 rounded focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                placeholder="0"
              />
            </div>

            <!-- Total Check -->
            <div class="flex justify-between items-center text-sm" :class="isPaymentAmountValid ? 'text-green-600' : 'text-red-600'">
              <span>Tổng thanh toán</span>
              <span>{{ formatCurrency((currentInvoice.cashAmount || 0) + (currentInvoice.vnpayAmount || 0)) }}</span>
            </div>
          </div>

          <!-- Voucher Section -->
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-slate-700">Voucher giảm giá</span>
              <button
                @click="openVoucherModal"
                class="text-xs text-orange-600 hover:text-orange-700"
              >
                Chọn voucher
              </button>
            </div>

            <!-- Selected Voucher -->
            <div v-if="currentInvoice.selectedVoucher" class="p-3 bg-green-50 border border-green-200 rounded-lg">
              <div class="flex items-center justify-between">
                <div>
                  <div class="text-sm font-medium text-green-800">{{ currentInvoice.selectedVoucher.tenPhieuGiamGia }}</div>
                  <div class="text-xs text-green-600">{{ currentInvoice.selectedVoucher.maPhieuGiamGia }}</div>
                  <div class="text-xs text-green-600">
                    Giảm {{ (currentInvoice.selectedVoucher.loaiPhieuGiamGia === 'PERCENT' || currentInvoice.selectedVoucher.loaiPhieuGiamGia === 1) ? (currentInvoice.selectedVoucher.giaTriGiamGia || 0) + '%' : formatCurrency(currentInvoice.selectedVoucher.giaTriGiamGia || 0) }}
                  </div>
                </div>
                <button
                  @click="removeVoucher"
                  class="w-6 h-6 bg-red-500 hover:bg-red-600 rounded-full text-white text-xs flex items-center justify-center"
                >
                  ×
                </button>
              </div>
            </div>

            <!-- Best Voucher Suggestion -->
            <div v-if="!currentInvoice.selectedVoucher && bestVoucher" class="p-3 bg-orange-50 border border-orange-200 rounded-lg">
              <div class="flex items-center justify-between">
                <div>
                  <div class="text-sm font-medium text-orange-800">💡 Voucher tốt nhất</div>
                  <div class="text-xs text-orange-600">{{ bestVoucher.tenPhieuGiamGia }}</div>
                  <div class="text-xs text-orange-600">
                    Tiết kiệm {{ (bestVoucher.loaiPhieuGiamGia === 'PERCENT' || bestVoucher.loaiPhieuGiamGia === 1) ?
                    formatCurrency(Math.min(currentInvoice.subtotal * (bestVoucher.giaTriGiamGia || 0) / 100, bestVoucher.soTienGiamToiDa || Infinity)) :
                    formatCurrency(bestVoucher.giaTriGiamGia || 0)
                    }}
                  </div>
                </div>
                <button
                  @click="applyBestVoucher"
                  class="px-3 py-1 bg-orange-400 hover:bg-orange-600 text-white text-xs rounded"
                >
                  Áp dụng
                </button>
              </div>
            </div>
          </div>

          <!-- Quick Payment Amounts (Only for Cash) -->
          <div v-if="currentInvoice.paymentMethod === 'cash'" class="grid grid-cols-3 gap-2">
            <button
              v-for="amount in getSuggestedAmounts()"
              :key="amount.value"
              @click="setQuickAmount(amount.value)"
              :class="[
                'px-3 py-2 text-sm font-medium rounded-lg border transition-all',
                currentInvoice.customerPaid === amount.value
                  ? 'bg-orange-400 text-white border-orange-400'
                  : 'bg-white text-slate-700 border-slate-300 hover:bg-orange-50 hover:border-orange-300'
              ]"
            >
              {{ amount.label }}
            </button>
          </div>
        </div>

        <!-- Payment Footer (hidden when delivery mode is active) -->
        <div v-if="saleMode === 'quick'" class="p-4 border-t border-slate-200 bg-slate-50 flex-shrink-0">
          <!-- Main Payment Button -->
          <button
            @click="normalSale"
            :disabled="currentInvoice.cartItems.length === 0"
            :class="[
              'w-full py-4 text-white rounded-lg font-bold text-lg transition-colors disabled:cursor-not-allowed',
              currentInvoice.paymentMethod === 'vnpay'
                ? 'bg-blue-500 hover:bg-blue-600 disabled:bg-slate-300'
                : 'bg-orange-400 hover:bg-orange-600 disabled:bg-slate-300'
            ]"
          >
            {{ currentInvoice.paymentMethod === 'vnpay' ? 'THANH TOÁN VNPAY' : 'THANH TOÁN TIỀN MẶT' }}
          </button>


          <!-- Contact Info -->
          <div class="text-center text-xs text-slate-500 mt-3 pt-3 border-t border-slate-200">
            <div class="flex items-center justify-center space-x-4">
              <span>📞 1900 6522</span>
              <span>❓</span>
              <span>💬</span>
            </div>
          </div>
        </div>
      </aside>

      <!-- Delivery Section (Right) - Hiển thị khi chọn "Bán giao hàng" -->
      <aside v-if="saleMode === 'delivery'" class="w-[750px] bg-white shadow-lg flex flex-col border-l border-slate-200 h-full overflow-hidden">
        <!-- Header -->
        <div class="p-4 border-b border-slate-200 bg-gradient-to-r from-orange-50 to-orange-100">
          <h2 class="text-xl font-bold text-slate-800">Thông tin giao hàng</h2>
        </div>

        <!-- Content -->
        <div class="flex-1 overflow-y-auto p-6 hide-scroll">
          <!-- Layout 2 cột: Bên trái - Thông tin khách hàng, Bên phải - Thông tin thanh toán -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <!-- Bên trái: Thông tin khách hàng -->
            <div class="space-y-4">
              <h4 class="text-lg font-semibold text-slate-800 mb-4 border-b-2 border-orange-400 pb-2">Thông tin khách hàng</h4>

              <!-- Phần chọn khách hàng -->
              <div class="bg-slate-50 p-4 rounded-lg border border-slate-200">
                <label class="block text-sm font-medium text-slate-700 mb-2">Chọn khách hàng</label>
                <div class="flex space-x-2">
                  <div class="relative flex-1">
                    <input
                      type="text"
                      v-model="customerSearch"
                      placeholder="Tìm khách hàng (F4)"
                      @input="searchCustomers"
                      @focus="showCustomerSuggestions = true"
                      @blur="hideCustomerSuggestions"
                      class="w-full pl-10 pr-4 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
                    />
                    <svg class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                    </svg>
                    <!-- Customer Suggestions Dropdown -->
                    <div v-if="showCustomerSuggestions && filteredCustomers.length > 0" class="absolute top-full left-0 right-0 mt-1 bg-white border border-slate-300 rounded-lg shadow-lg max-h-60 overflow-y-auto z-50">
                      <div
                        v-for="customer in filteredCustomers"
                        :key="customer.id"
                        @click="selectCustomer(customer)"
                        class="px-4 py-3 hover:bg-slate-50 cursor-pointer border-b border-slate-100 last:border-b-0"
                      >
                        <div class="font-medium text-slate-800">{{ customer.hoTen }}</div>
                        <div class="text-sm text-slate-500">{{ customer.soDienThoai }}</div>
                        <div class="text-xs text-slate-400">{{ customer.email || 'Không có email' }}</div>
                      </div>
                    </div>
                  </div>
                  <button
                    @click="openQuickCustomerModal"
                    class="w-10 h-10 bg-orange-400 hover:bg-orange-600 text-white rounded-lg transition-colors flex items-center justify-center"
                    title="Thêm khách hàng nhanh"
                  >
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
                    </svg>
                  </button>
                </div>

                <!-- Selected Customer Display -->
                <div v-if="currentInvoice.selectedCustomer" class="mt-3 p-3 bg-orange-50 border border-orange-200 rounded-lg">
                  <div class="flex items-center justify-between">
                    <div class="flex-1">
                      <div class="font-medium text-slate-800">{{ currentInvoice.selectedCustomer.hoTen }}</div>
                      <div class="text-sm text-slate-600">{{ currentInvoice.selectedCustomer.soDienThoai }}</div>
                      <div class="text-xs text-slate-500">{{ currentInvoice.selectedCustomer.email || 'Không có email' }}</div>
                    </div>
                    <button @click="removeCustomer" class="text-red-500 hover:text-red-700">
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>

              <!-- Địa chỉ trước đó -->
              <div v-if="currentInvoice.selectedCustomer">
                <label class="block text-sm font-medium text-slate-700 mb-1">Địa chỉ trước đó</label>
                <select v-model="selectedPreviousAddressId" @change="applyPreviousAddress" class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent">
                  <option value="">-- Chọn địa chỉ đã lưu --</option>
                  <option v-for="addr in previousAddresses" :key="addr.id" :value="addr.id">
                    {{ (addr.diaChi?.diaChiChiTiet || '') + (addr.diaChi?.phuongXa ? ', ' + addr.diaChi?.phuongXa : '') + (addr.diaChi?.tinhThanhPho ? ', ' + addr.diaChi?.tinhThanhPho : '') }}
                  </option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">Tên người nhận <span class="text-red-500">*</span></label>
                <input type="text" v-model="deliveryInfo.tenNguoiNhan" placeholder="Nhập tên người nhận" class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent" />
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">Số điện thoại <span class="text-red-500">*</span></label>
                <input type="tel" v-model="deliveryInfo.soDienThoai" placeholder="Nhập số điện thoại người nhận" class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent" />
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">Địa chỉ chi tiết (Số nhà, ngõ, đường) <span class="text-red-500">*</span></label>
                <textarea v-model="deliveryInfo.soNhaDuong" placeholder="Ví dụ: 123/45, ngõ 12, đường ABC" class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent" rows="2"></textarea>
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">Tỉnh/Thành phố <span class="text-red-500">*</span></label>
                <select v-model="deliveryInfo.idTinhThanhPho" @change="onProvinceChange" class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent" :disabled="loadingProvinces">
                  <option value="">Chọn tỉnh/thành phố</option>
                  <option v-for="province in tinhThanhPhoList" :key="province.code" :value="province.code">
                    {{ province.name }}
                  </option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">Phường/Xã <span class="text-red-500">*</span></label>
                <select v-model="deliveryInfo.idPhuongXa" @change="onWardChange" class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent" :disabled="!deliveryInfo.idTinhThanhPho || loadingWards">
                  <option value="">Chọn phường/xã</option>
                  <option v-for="ward in phuongXaList" :key="ward.code" :value="ward.code">
                    {{ ward.name }}
                  </option>
                </select>
              </div>

              <div>
                <label class="block text-sm font-medium text-slate-700 mb-1">Ghi chú (tùy chọn)</label>
                <textarea v-model="deliveryInfo.ghiChu" placeholder="Ghi chú thêm cho bưu tá (tùy chọn)" class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent" rows="2"></textarea>
              </div>
            </div>

            <!-- Bên phải: Thông tin thanh toán (COD, Voucher, Phương thức thanh toán) -->
            <div class="space-y-4">
              <h4 class="text-lg font-semibold text-slate-800 mb-4 border-b-2 border-orange-400 pb-2">Thông tin thanh toán</h4>

              <!-- Tổng tiền -->
              <div class="bg-slate-50 p-4 rounded-xl border border-slate-200">
                <h4 class="text-sm font-semibold text-slate-900 mb-3">Tổng tiền</h4>
                <div class="space-y-2">
                  <div class="flex justify-between items-center text-sm">
                    <span class="text-slate-600">Tổng tiền hàng:</span>
                    <span class="font-medium text-slate-800">{{ formatCurrency(currentInvoice.subtotal) }}</span>
                  </div>
                  <div v-if="currentInvoice.discount > 0" class="flex justify-between items-center text-sm">
                    <span class="text-slate-600">Giảm giá:</span>
                    <span class="font-medium text-red-600">-{{ formatCurrency(currentInvoice.discount) }}</span>
                  </div>
                  <div class="flex justify-between items-center pt-2 border-t border-slate-200">
                    <span class="text-base font-semibold text-slate-900">Tổng cộng:</span>
                    <span class="text-lg font-bold text-orange-600">{{ formatCurrency(currentInvoice.total) }}</span>
                  </div>
                  <div v-if="deliveryInfo.codEnabled && deliveryInfo.codFee > 0" class="flex justify-between items-center text-sm pt-2 border-t border-slate-200">
                    <span class="text-slate-600">Phí COD:</span>
                    <span class="font-medium text-slate-800">{{ formatCurrency(deliveryInfo.codFee) }}</span>
                  </div>
                  <div v-if="deliveryInfo.codEnabled && deliveryInfo.codFee > 0" class="flex justify-between items-center pt-2 border-t-2 border-orange-400">
                    <span class="text-base font-semibold text-slate-900">Tổng thanh toán:</span>
                    <span class="text-xl font-bold text-orange-600">{{ formatCurrency(currentInvoice.total + deliveryInfo.codFee) }}</span>
                  </div>
                </div>
              </div>

              <!-- COD Section -->
              <div class="bg-slate-50 p-4 rounded-xl border border-slate-200">
                <div class="flex items-center justify-between">
                  <div>
                    <h4 class="text-sm font-semibold text-slate-900 mb-1">Thu hộ tiền (COD)</h4>
                    <p class="text-xs text-slate-700">Tính phí thu hộ dựa trên địa chỉ giao hàng</p>
                  </div>
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input type="checkbox" v-model="deliveryInfo.codEnabled" class="sr-only peer" />
                    <div class="w-14 h-7 bg-gray-300/80 rounded-full peer peer-checked:after:translate-x-full after:content-[''] after:absolute after:top-0.5 after:left-[4px] after:bg-white after:rounded-full after:h-6 after:w-6 after:transition-all peer-checked:bg-orange-500"></div>
                  </label>
                </div>
              </div>

              <!-- Payment Method Section -->
              <div class="bg-slate-50 p-4 rounded-xl border border-slate-200">
                <h4 class="text-sm font-semibold text-slate-900 mb-3">Phương thức thanh toán</h4>
                <div class="space-y-3">
                  <label class="flex items-center space-x-2 cursor-pointer">
                    <input type="radio" name="paymentMethodDelivery" value="cash" v-model="currentInvoice.paymentMethod" @change="resetCombinedPaymentFields" class="text-orange-600">
                    <span class="text-sm">Tiền mặt</span>
                  </label>
                  <label class="flex items-center space-x-2 cursor-pointer">
                    <input type="radio" name="paymentMethodDelivery" value="vnpay" v-model="currentInvoice.paymentMethod" @change="resetCombinedPaymentFields" class="text-orange-600">
                    <span class="text-sm">VNPay</span>
                  </label>
                  <label class="flex items-center space-x-2 cursor-pointer">
                    <input type="radio" name="paymentMethodDelivery" value="combined" v-model="currentInvoice.paymentMethod" @change="resetCombinedPaymentFields" class="text-orange-600">
                    <span class="text-sm">Kết hợp</span>
                  </label>

                  <div v-if="currentInvoice.paymentMethod === 'combined'" class="mt-3 pt-3 border-t border-slate-200 space-y-3">
                    <div class="flex justify-between items-center">
                      <span class="font-semibold text-slate-800">Tiền mặt</span>
                      <input type="text" v-model="formattedCashAmount" @input="handleCashAmountInput" class="w-32 text-right font-bold text-lg px-2 py-1 border border-slate-300 rounded focus:ring-2 focus:ring-orange-500 focus:border-transparent" placeholder="0" />
                    </div>
                    <div class="flex justify-between items-center">
                      <span class="font-semibold text-slate-800">VNPay</span>
                      <input type="text" v-model="formattedVnpayAmount" @input="handleVnpayAmountInput" class="w-32 text-right font-bold text-lg px-2 py-1 border border-slate-300 rounded focus:ring-2 focus:ring-orange-500 focus:border-transparent" placeholder="0" />
                    </div>
                    <div class="flex justify-between items-center text-sm" :class="isPaymentAmountValid ? 'text-green-600' : 'text-red-600'">
                      <span>Tổng thanh toán</span>
                      <span>{{ formatCurrency((currentInvoice.cashAmount || 0) + (currentInvoice.vnpayAmount || 0)) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Voucher Section -->
              <div class="bg-slate-50 p-4 rounded-xl border border-slate-200">
                <div class="flex items-center justify-between mb-3">
                  <h4 class="text-sm font-semibold text-slate-900">Voucher giảm giá</h4>
                  <button @click="openVoucherModal" class="text-xs text-orange-600 hover:text-orange-700 font-medium">Chọn voucher</button>
                </div>
                <div v-if="currentInvoice.selectedVoucher" class="p-3 bg-green-50 border border-green-200 rounded-lg">
                  <div class="flex items-center justify-between">
                    <div>
                      <div class="text-sm font-medium text-green-800">{{ currentInvoice.selectedVoucher.tenPhieuGiamGia }}</div>
                      <div class="text-xs text-green-600">{{ currentInvoice.selectedVoucher.maPhieuGiamGia }}</div>
                    </div>
                    <button @click="removeVoucher" class="w-6 h-6 bg-red-500 hover:bg-red-600 rounded-full text-white text-xs flex items-center justify-center">×</button>
                  </div>
                </div>
                <div v-else-if="bestVoucher" class="p-3 bg-orange-50 border border-orange-200 rounded-lg">
                  <div class="flex items-center justify-between">
                    <div>
                      <div class="text-sm font-medium text-orange-800">💡 Voucher tốt nhất</div>
                      <div class="text-xs text-orange-600">{{ bestVoucher.tenPhieuGiamGia }}</div>
                    </div>
                    <button @click="applyBestVoucher" class="px-3 py-1 bg-orange-400 hover:bg-orange-600 text-white text-xs rounded">Áp dụng</button>
                  </div>
                </div>
                <div v-else class="text-xs text-slate-500 text-center py-2">
                  Chưa có voucher
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer với nút thanh toán -->
        <div class="p-6 border-t border-slate-200 bg-slate-50 flex-shrink-0">
          <div class="flex items-center justify-between">
            <button @click="saleMode = 'quick'" class="px-6 py-2 border border-slate-300 text-slate-700 rounded-lg hover:bg-slate-100 transition-colors font-medium">
              Hủy
            </button>
            <button @click="confirmDelivery" class="px-8 py-3 bg-orange-400 text-white rounded-lg hover:bg-orange-600 transition-colors font-semibold text-lg shadow-lg">
              THANH TOÁN
            </button>
          </div>
        </div>
      </aside>
    </div>

    <!-- Delivery Modal (Old - for backward compatibility) -->
    <div v-if="showDeliveryModal" class="fixed inset-0 bg-white/20 backdrop-blur-md flex items-center justify-center z-50 p-4">
      <div class="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <h3 class="text-lg font-semibold mb-4">Thông tin giao hàng</h3>
        <div class="space-y-4">
          <!-- Tên người nhận -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Tên người nhận <span class="text-red-500">*</span></label>
            <input
              type="text"
              v-model="deliveryInfo.tenNguoiNhan"
              placeholder="Nhập tên người nhận"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            />
          </div>

          <!-- Số điện thoại -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Số điện thoại <span class="text-red-500">*</span></label>
            <input
              type="tel"
              v-model="deliveryInfo.soDienThoai"
              placeholder="Nhập số điện thoại người nhận"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            />
          </div>

          <!-- Địa chỉ chi tiết -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Địa chỉ chi tiết (Số nhà, ngõ, đường) <span class="text-red-500">*</span></label>
            <textarea
              v-model="deliveryInfo.soNhaDuong"
              placeholder="Ví dụ: 123/45, ngõ 12, đường ABC"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
              rows="2"
            ></textarea>
          </div>

          <!-- Tỉnh/TP -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Tỉnh/Thành phố <span class="text-red-500">*</span></label>
            <select
              v-model="deliveryInfo.idTinhThanhPho"
              @change="onProvinceChange"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
              :disabled="loadingProvinces"
            >
              <option value="">Chọn tỉnh/thành phố</option>
              <option
                v-for="tinh in tinhThanhPhoList"
                :key="tinh.code"
                :value="tinh.code"
              >
                {{ tinh.name }}
              </option>
            </select>
            <div v-if="loadingProvinces" class="text-xs text-slate-500 mt-1">Đang tải...</div>
          </div>


          <!-- Phường/Xã -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Phường/Xã <span class="text-red-500">*</span></label>
            <select
              v-model="deliveryInfo.idPhuongXa"
              @change="onWardChange"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
              :disabled="!deliveryInfo.idTinhThanhPho || loadingWards"
            >
              <option value="">Chọn phường/xã</option>
              <option
                v-for="phuong in phuongXaList"
                :key="phuong.code"
                :value="phuong.code"
              >
                {{ phuong.name }}
              </option>
            </select>
            <div v-if="loadingWards" class="text-xs text-slate-500 mt-1">Đang tải...</div>
          </div>

          <!-- Ghi chú -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Ghi chú <span class="text-slate-500 text-xs">(tùy chọn)</span></label>
            <textarea
              v-model="deliveryInfo.ghiChu"
              placeholder="Ghi chú thêm cho bưu tá (tùy chọn)"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
              rows="2"
            ></textarea>
          </div>

          <!-- COD Toggle Card -->
          <div class="bg-gradient-to-br from-slate-50 to-slate-100 p-4 rounded-xl border-2 border-slate-200 shadow-lg">
            <div class="flex items-center justify-between">
              <div class="flex-1">
                <h4 class="text-sm font-semibold text-slate-900 mb-1">Thu hộ tiền (COD)</h4>
                <p class="text-xs text-slate-700">Tính phí thu hộ dựa trên địa chỉ giao hàng</p>
                <div v-if="deliveryInfo.codEnabled && deliveryInfo.codFee > 0" class="mt-2">
                  <span class="text-sm text-slate-700">Phí COD: </span>
                  <span class="text-lg font-bold text-orange-600">{{ formatCurrency(deliveryInfo.codFee) }}</span>
                </div>
              </div>

              <div class="flex items-center space-x-3">
                <!-- COD Fee Display -->
                <div v-if="deliveryInfo.codEnabled && deliveryInfo.codFee > 0"
                     class="text-right">
                  <div class="text-2xl font-bold text-orange-600">{{ formatCurrency(deliveryInfo.codFee) }}</div>
                  <div class="text-xs text-slate-700">Phí COD</div>
                </div>

                <!-- Toggle Switch -->
                <div class="relative">
                  <label class="relative inline-flex items-center cursor-pointer">
                    <input
                      type="checkbox"
                      v-model="deliveryInfo.codEnabled"
                      class="sr-only peer"
                    />
                    <div class="w-14 h-7 bg-gray-300/80 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-orange-300/50 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-0.5 after:left-[4px] after:bg-white/90 after:border-gray-300/60 after:border after:rounded-full after:h-6 after:w-6 after:transition-all peer-checked:bg-orange-500/90 shadow-lg backdrop-blur-sm"></div>
                  </label>
                </div>
              </div>
            </div>

            <!-- COD Info -->
            <div v-if="deliveryInfo.codEnabled" class="mt-3 pt-3 border-t border-white/30">
              <div class="flex items-center justify-between text-sm">
                <span class="text-slate-700">Tổng phí giao hàng:</span>
                <span class="font-semibold text-orange-600">{{ formatCurrency(deliveryInfo.codFee) }}</span>
              </div>
              <div class="text-xs text-slate-600 mt-1">
                Phí được tính tự động dựa trên tỉnh/thành phố đã chọn
              </div>
            </div>
          </div>
        </div>
        <div class="flex space-x-3 mt-6">
          <button
            @click="closeDeliveryModal"
            class="flex-1 px-4 py-2 border border-slate-300 text-slate-700 rounded-lg hover:bg-slate-50 transition-colors"
          >
            Hủy
          </button>
          <button
            @click="confirmDelivery"
            class="flex-1 px-4 py-2 bg-orange-400 text-white rounded-lg hover:bg-orange-600 transition-colors"
          >
            Xác nhận
          </button>
        </div>
      </div>
    </div>

    <!-- Product Selection Modal -->
    <div v-if="showProductModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-50">
      <div class="bg-gradient-to-br from-blue-50 to-indigo-50 border-2 border-blue-200 rounded-xl w-4/5 max-w-6xl max-h-[80vh] flex flex-col shadow-2xl">
        <!-- Modal Header -->
        <div class="p-6 border-b border-blue-200 bg-gradient-to-r from-blue-100 to-indigo-100">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-semibold text-blue-800">Chọn sản phẩm</h3>
            <button
              @click="showProductModal = false"
              class="w-8 h-8 flex items-center justify-center bg-blue-200 hover:bg-blue-300 rounded-full text-blue-700 transition-colors"
            >
              ×
            </button>
          </div>

          <!-- Search Bar -->
          <div class="mt-4">
            <div class="relative">
              <input
                type="text"
                v-model="productSearchQuery"
                placeholder="Tìm kiếm sản phẩm..."
                class="w-full pl-10 pr-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
              />
              <svg class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
            </div>
          </div>
        </div>

        <!-- Modal Content -->
        <div class="flex-1 overflow-y-auto p-6">
          <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
            <div
              v-for="product in modalFilteredProducts"
              :key="product.chiTietSanPhamId"
              @click="addToCartFromModal(product)"
              class="bg-white rounded-xl border border-slate-200 hover:border-orange-300 hover:shadow-lg transition-all duration-200 cursor-pointer group overflow-hidden"
            >
              <!-- Product Image -->
              <div class="aspect-square bg-gradient-to-br from-slate-100 to-slate-200 relative overflow-hidden">
                <img
                  v-if="product.hinhAnh"
                  :src="product.hinhAnh"
                  :alt="product.tenSanPham"
                  class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
                />
                <div v-else class="w-full h-full flex items-center justify-center">
                  <svg class="w-12 h-12 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"></path>
                  </svg>
                </div>

                <!-- Stock Badge -->
                <div v-if="product.soLuongTon <= 5" class="absolute top-2 right-2 px-2 py-1 bg-red-500 text-white text-xs font-medium rounded-full">
                  Sắp hết
                </div>
              </div>

              <!-- Product Info -->
              <div class="p-3">
                <h4 class="font-medium text-slate-800 text-sm mb-1 line-clamp-2 group-hover:text-orange-600 transition-colors">
                  {{ product.tenSanPham }}
                </h4>

                <!-- Specs -->
                <div class="flex flex-wrap gap-1 mb-2">
                  <span class="px-2 py-0.5 bg-blue-100 text-blue-700 text-xs rounded-full">{{ product.tenRam }}</span>
                  <span class="px-2 py-0.5 bg-green-100 text-green-700 text-xs rounded-full">{{ product.tenRom }}</span>
                  <span class="px-2 py-0.5 bg-purple-100 text-purple-700 text-xs rounded-full">{{ product.tenMauSac }}</span>
                </div>

                <!-- Price -->
                <div class="text-lg font-bold text-black mb-1">
                  {{ formatCurrency(product.gia) }}
                </div>

                <div class="text-xs text-slate-500">
                  Tồn: {{ product.soLuongTon }}
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-if="modalFilteredProducts.length === 0" class="flex flex-col items-center justify-center h-64 text-slate-500">
            <svg class="w-16 h-16 mb-4 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
            <p class="text-lg font-medium">Không tìm thấy sản phẩm</p>
            <p class="text-sm">Thử thay đổi từ khóa tìm kiếm</p>
          </div>
        </div>
      </div>
    </div>

    <!-- IMEI Selection Modal -->
    <div v-if="showImeiModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-50">
      <div class="bg-gradient-to-br from-purple-50 to-pink-50 border-2 border-purple-200 rounded-xl w-4/5 max-w-2xl max-h-[80vh] flex flex-col shadow-2xl">
        <!-- Modal Header -->
        <div class="p-6 border-b border-purple-200 bg-gradient-to-r from-purple-100 to-pink-100">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-semibold text-purple-800">Chọn IMEI - {{ selectedProduct?.tenSanPham }}</h3>
            <button
              @click="closeImeiModal"
              class="w-8 h-8 flex items-center justify-center bg-purple-200 hover:bg-purple-300 rounded-full text-purple-700 transition-colors"
            >
              ×
            </button>
          </div>

          <!-- Quantity and Selection Info -->
          <div class="mt-4 flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <label class="text-sm font-medium text-purple-700">Số lượng:</label>
              <input
                type="number"
                v-model="quantityToAdd"
                min="1"
                :max="selectedProduct?.soLuongTon || 1"
                class="w-20 px-3 py-1 border border-purple-300 rounded-lg text-center"
              />
            </div>
            <div class="text-sm text-purple-600">
              Đã chọn: {{ selectedImeis.length }}/{{ quantityToAdd }} IMEI
            </div>
          </div>

          <!-- IMEI Search Bar -->
          <div class="mt-4">
            <div class="relative">
              <input
                type="text"
                v-model="imeiSearchQuery"
                placeholder="Tìm kiếm IMEI..."
                class="w-full pl-10 pr-4 py-3 border border-orange-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
              />
              <svg class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
              </svg>
            </div>
          </div>
        </div>

        <!-- Modal Content -->
        <div class="flex-1 overflow-y-auto p-6">
          <div class="grid grid-cols-1 gap-3">
            <div
              v-for="imei in filteredImeis"
              :key="imei.id"
              @click="selectImei(imei)"
              :class="[
                'p-4 rounded-lg border-2 cursor-pointer transition-all duration-200',
                selectedImeis.find(selected => selected.id === imei.id)
                  ? 'border-orange-500 bg-orange-100'
                  : 'border-orange-200 bg-white hover:border-orange-300 hover:bg-orange-50'
              ]"
            >
              <div class="flex items-center justify-between">
                <div>
                  <div class="font-medium text-orange-800">{{ imei.imei }}</div>
                  <div class="text-sm text-orange-600">Khả dụng</div>
                </div>
                <div v-if="selectedImeis.find(selected => selected.id === imei.id)" class="w-6 h-6 bg-orange-500 rounded-full flex items-center justify-center">
                  <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-if="filteredImeis.length === 0" class="flex flex-col items-center justify-center h-32 text-purple-500">
            <svg class="w-12 h-12 mb-2 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
            <p class="text-sm">Không tìm thấy IMEI</p>
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 border-t border-purple-200 bg-gradient-to-r from-purple-50 to-pink-50">
          <div class="flex space-x-3">
            <button
              @click="closeImeiModal"
              class="flex-1 px-4 py-2 border border-orange-300 text-orange-700 rounded-lg hover:bg-orange-50 transition-colors"
            >
              Hủy
            </button>
            <button
              @click="confirmImeiSelection"
              :disabled="selectedImeis.length !== quantityToAdd"
              :class="[
                'flex-1 px-4 py-2 rounded-lg transition-colors',
                selectedImeis.length === quantityToAdd
                  ? 'bg-orange-400 text-white hover:bg-orange-600'
                  : 'bg-orange-200 text-orange-400 cursor-not-allowed'
              ]"
            >
              Xác nhận ({{ selectedImeis.length }}/{{ quantityToAdd }})
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Quick Customer Modal -->
    <div v-if="showQuickCustomerModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-50">
      <div class="bg-gradient-to-br from-blue-50 to-indigo-50 border-2 border-blue-200 rounded-xl w-4/5 max-w-md flex flex-col shadow-2xl">
        <!-- Modal Header -->
        <div class="p-6 border-b border-blue-200 bg-gradient-to-r from-blue-100 to-indigo-100">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-semibold text-blue-800">Thêm khách hàng nhanh</h3>
            <button
              @click="closeQuickCustomerModal"
              class="w-8 h-8 flex items-center justify-center bg-blue-200 hover:bg-blue-300 rounded-full text-blue-700 transition-colors"
            >
              ×
            </button>
          </div>
        </div>

        <!-- Modal Content -->
        <div class="p-6 space-y-4">
          <div>
            <label class="block text-sm font-medium text-blue-700 mb-2">Họ tên *</label>
            <input
              type="text"
              v-model="quickCustomerForm.hoTen"
              placeholder="Nhập họ tên khách hàng"
              class="w-full px-3 py-2 border border-orange-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-blue-700 mb-2">Số điện thoại *</label>
            <input
              type="tel"
              v-model="quickCustomerForm.soDienThoai"
              placeholder="Nhập số điện thoại"
              class="w-full px-3 py-2 border border-orange-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-blue-700 mb-2">Email</label>
            <input
              type="email"
              v-model="quickCustomerForm.email"
              placeholder="Nhập email (tùy chọn)"
              class="w-full px-3 py-2 border border-orange-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-blue-700 mb-2">Địa chỉ</label>
            <input
              type="text"
              v-model="quickCustomerForm.diaChi"
              placeholder="Nhập địa chỉ (tùy chọn)"
              class="w-full px-3 py-2 border border-orange-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
            />
          </div>
        </div>

        <!-- Modal Footer -->
        <div class="p-6 border-t border-blue-200 bg-gradient-to-r from-blue-50 to-indigo-50">
          <div class="flex space-x-3">
            <button
              @click="closeQuickCustomerModal"
              class="flex-1 px-4 py-2 border border-orange-300 text-orange-700 rounded-lg hover:bg-orange-50 transition-colors"
            >
              Hủy
            </button>
            <button
              @click="createQuickCustomer"
              class="flex-1 px-4 py-2 bg-orange-400 text-white rounded-lg hover:bg-orange-600 transition-colors"
            >
              Tạo khách hàng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Voucher Selection Modal -->
    <div v-if="showVoucherModal" class="fixed inset-0 bg-white/20 flex items-center justify-center z-50">
      <div class="bg-gradient-to-br from-green-50 to-emerald-50 border-2 border-green-200 rounded-xl w-4/5 max-w-4xl max-h-[80vh] flex flex-col shadow-2xl">
        <!-- Modal Header -->
        <div class="p-6 border-b border-green-200 bg-gradient-to-r from-green-100 to-emerald-100">
          <div class="flex items-center justify-between">
            <h3 class="text-xl font-semibold text-green-800">🎫 Chọn voucher giảm giá</h3>
            <button
              @click="showVoucherModal = false"
              class="w-8 h-8 flex items-center justify-center bg-green-200 hover:bg-green-300 rounded-full text-green-700 transition-colors"
            >
              ×
            </button>
          </div>

          <!-- Tab Navigation -->
          <div class="flex space-x-1 mt-4">
            <button
              @click="voucherTab = 'select'"
              :class="[
                'px-4 py-2 text-sm font-medium rounded-lg transition-colors',
                voucherTab === 'select'
                  ? 'bg-green-200 text-green-800'
                  : 'text-green-600 hover:bg-green-100'
              ]"
            >
              Chọn từ danh sách
            </button>
            <button
              @click="voucherTab = 'manual'"
              :class="[
                'px-4 py-2 text-sm font-medium rounded-lg transition-colors',
                voucherTab === 'manual'
                  ? 'bg-green-200 text-green-800'
                  : 'text-green-600 hover:bg-green-100'
              ]"
            >
              Nhập mã voucher
            </button>
          </div>
        </div>

        <!-- Modal Content -->
        <div class="flex-1 overflow-y-auto p-6">
          <!-- Select from List Tab -->
          <div v-if="voucherTab === 'select'" class="space-y-4">
            <div
              v-for="voucher in vouchers"
              :key="voucher.id"
              @click="isVoucherApplicableLocal(voucher) ? selectVoucher(voucher) : null"
              :class="[
                'p-4 border rounded-lg transition-all',
                isVoucherApplicableLocal(voucher)
                  ? 'border-slate-200 hover:border-blue-300 hover:shadow-md cursor-pointer'
                  : 'border-slate-200 opacity-50 cursor-not-allowed'
              ]"
            >
              <div class="flex items-center justify-between">
                <div class="flex-1">
                  <div class="flex items-center space-x-3">
                    <div class="w-12 h-12 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
                      <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path>
                      </svg>
                    </div>
                    <div>
                      <h4 class="font-semibold text-slate-800">{{ voucher.tenPhieuGiamGia }}</h4>
                      <p class="text-sm text-slate-500">{{ voucher.maPhieuGiamGia }}</p>
                      <div class="flex items-center space-x-4 mt-1">
                        <span class="text-sm font-medium text-blue-600">
                          Giảm {{ (voucher.loaiPhieuGiamGia === 'PERCENT' || voucher.loaiPhieuGiamGia === 1) ? (voucher.giaTriGiamGia || 0) + '%' : formatCurrency(voucher.giaTriGiamGia || 0) }}
                        </span>
                        <span class="text-xs text-slate-500">
                          Đơn tối thiểu: {{ formatCurrency(voucher.hoaDonToiThieu || 0) }}
                        </span>
                        <span v-if="voucher.soTienGiamToiDa" class="text-xs text-slate-500">
                          Giảm tối đa: {{ formatCurrency(voucher.soTienGiamToiDa) }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="text-right">
                  <div class="text-lg font-bold" :class="isVoucherApplicableLocal(voucher) ? 'text-green-600' : 'text-slate-400'">
                    -{{ calculateVoucherDiscount(voucher) }}
                  </div>
                  <div class="text-xs text-slate-500">
                    Còn {{ voucher.soLuongDung }} lượt
                  </div>
                  <div v-if="!isVoucherApplicableLocal(voucher)" class="text-xs text-red-500 mt-1">
                    Không đủ điều kiện áp dụng
                  </div>
                </div>
              </div>
            </div>

            <!-- Empty State for Select Tab -->
            <div v-if="vouchers.length === 0" class="flex flex-col items-center justify-center h-64 text-slate-500">
              <svg class="w-16 h-16 mb-4 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path>
              </svg>
              <p class="text-lg font-medium">Không có voucher khả dụng</p>
              <p class="text-sm">Không có voucher nào phù hợp với đơn hàng này</p>
            </div>
          </div>

          <!-- Manual Input Tab -->
          <div v-if="voucherTab === 'manual'" class="space-y-4">
            <div class="bg-white p-6 rounded-lg border border-slate-200">
              <h4 class="text-lg font-semibold text-slate-800 mb-4">Nhập mã voucher</h4>

              <div class="space-y-4">
                <div>
                  <label class="block text-sm font-medium text-slate-700 mb-2">
                    Mã voucher
                  </label>
                  <input
                    type="text"
                    v-model="manualVoucherCode"
                    @keyup.enter="applyManualVoucher"
                    placeholder="Nhập mã voucher..."
                    class="w-full px-4 py-3 border border-slate-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  />
                </div>

                <div class="flex space-x-3">
                  <button
                    @click="applyManualVoucher"
                    :disabled="!manualVoucherCode.trim() || manualVoucherLoading"
                    class="flex-1 bg-green-600 text-white px-4 py-3 rounded-lg font-medium hover:bg-green-700 disabled:bg-slate-300 disabled:cursor-not-allowed transition-colors"
                  >
                    <span v-if="manualVoucherLoading">Đang kiểm tra...</span>
                    <span v-else>Áp dụng voucher</span>
                  </button>

                  <button
                    @click="clearManualVoucher"
                    class="px-4 py-3 border border-slate-300 text-slate-700 rounded-lg font-medium hover:bg-slate-50 transition-colors"
                  >
                    Xóa
                  </button>
                </div>
              </div>
            </div>

            <!-- Manual Voucher Result -->
            <div v-if="manualVoucherResult" class="bg-white p-6 rounded-lg border border-slate-200">
              <div v-if="manualVoucherResult.success" class="space-y-3">
                <div class="flex items-center space-x-2 text-green-600">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                  </svg>
                  <span class="font-medium">Voucher hợp lệ!</span>
                </div>

                <div class="bg-green-50 p-4 rounded-lg">
                  <div class="flex items-center justify-between">
                    <div>
                      <h5 class="font-semibold text-green-800">{{ manualVoucherResult.voucher.tenPhieuGiamGia }}</h5>
                      <p class="text-sm text-green-600">{{ manualVoucherResult.voucher.maPhieuGiamGia }}</p>
                      <p class="text-sm text-green-600">
                        Giảm {{ (manualVoucherResult.voucher.loaiPhieuGiamGia === 'PERCENT' || manualVoucherResult.voucher.loaiPhieuGiamGia === 1) ?
                        (manualVoucherResult.voucher.giaTriGiamGia || 0) + '%' :
                        formatCurrency(manualVoucherResult.voucher.giaTriGiamGia || 0) }}
                      </p>
                    </div>
                    <div class="text-right">
                      <div class="text-lg font-bold text-green-600">
                        -{{ formatCurrency(manualVoucherResult.discountAmount) }}
                      </div>
                    </div>
                  </div>
                </div>

                <button
                  @click="selectManualVoucher"
                  class="w-full bg-green-600 text-white px-4 py-3 rounded-lg font-medium hover:bg-green-700 transition-colors"
                >
                  Sử dụng voucher này
                </button>
              </div>

              <div v-else class="flex items-center space-x-2 text-red-600">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
                <span class="font-medium">{{ manualVoucherResult.message }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast Component -->
    <Toast ref="toastRef" />
  </div>
</template>

<script>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import Toast from '../components/Toast.vue'
import SimpleUserMenu from '../components/SimpleUserMenu.vue'
import api, { paymentApi } from '../services/api'

export default {
  name: 'PosPage',
  components: {
    Toast,
    SimpleUserMenu
  },
  setup() {
    // Router
    const router = useRouter()
    
    // Reactive data
    const searchQuery = ref('')
    const customerSearch = ref('')
    const currentTime = ref('')
    const currentDate = ref('')
    const loading = ref(false)
    const toastRef = ref(null)

    // Data from API
    const products = ref([])
    const customers = ref([])

    // Administrative divisions for delivery
    const tinhThanhPhoList = ref([])
    const phuongXaList = ref([])
    const loadingProvinces = ref(false)
    const loadingWards = ref(false)

    // Invoice management
    const invoices = ref([
      {
        id: 1,
        name: 'Hóa đơn 1',
        cartItems: [],
        selectedCustomerId: '',
        selectedCustomer: null,
        selectedVoucher: null,
        subtotal: 0,
        discount: 0,
        total: 0,
        customerPaid: 0,
        change: 0,
        orderNote: '',
        paymentMethod: 'cash', // Default to cash
        // Combined payment fields
        cashAmount: 0,
        vnpayAmount: 0
      }
    ])
    const activeInvoiceId = ref(1)

    // Sale mode: 'quick' (bán nhanh) or 'delivery' (bán giao hàng)
    const saleMode = ref('quick')

    // Delivery modal
    const showDeliveryModal = ref(false)
    const deliveryInfo = ref({
      tenNguoiNhan: '',
      soDienThoai: '',
      soNhaDuong: '',
      idTinhThanhPho: '',
      idPhuongXa: '',
      phuongXa: '',
      quanHuyen: '',
      tinhThanh: '',
      ghiChu: ''
    })

    // Previous addresses for selected customer
    const previousAddresses = ref([])
    const selectedPreviousAddressId = ref('')

    const loadPreviousAddresses = async () => {
      try {
        previousAddresses.value = []
        selectedPreviousAddressId.value = ''
        const customer = currentInvoice.value?.selectedCustomer
        if (!customer || !customer.id) return
        const { data } = await api.get(`/api/user-dia-chi/khach-hang/${customer.id}`)
        previousAddresses.value = data || []
      } catch (e) {
        console.warn('Load previous addresses failed', e)
      }
    }

    const applyPreviousAddress = async () => {
      const addr = previousAddresses.value.find((a) => String(a.id) === String(selectedPreviousAddressId.value))
      if (!addr || !addr.diaChi) return
      deliveryInfo.value.soNhaDuong = addr.diaChi.diaChiChiTiet || ''
      deliveryInfo.value.ghiChu = addr.diaChi.ghiChu || ''

      // Map province/ward names to codes used by the selects
      if (!tinhThanhPhoList.value || tinhThanhPhoList.value.length === 0) {
        await loadProvinces()
      }
      const normalize = (s) => (s || '')
        .toString()
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/^(tinh|thanh pho|tp\.?|quan|huyen|thi tran|thi xa|phuong|xa)\s+/g, '')
        .replace(/\s+/g, ' ')
        .trim()

      const addrProvinceName = normalize(addr.diaChi.tinhThanhPho)
      const province = tinhThanhPhoList.value.find(p => {
        const n = normalize(p.name)
        return n === addrProvinceName || n.includes(addrProvinceName) || addrProvinceName.includes(n)
      })
      if (province) {
        deliveryInfo.value.idTinhThanhPho = province.code
        await onProvinceChange()
        const addrWardName = normalize(addr.diaChi.phuongXa)
        const ward = phuongXaList.value.find(w => {
          const n = normalize(w.name)
          return n === addrWardName || n.includes(addrWardName) || addrWardName.includes(n)
        })
        if (ward) {
          deliveryInfo.value.idPhuongXa = ward.code
          await onWardChange()
        }
      }

      // Prefill name/phone from selected customer
      if (currentInvoice.value?.selectedCustomer) {
        deliveryInfo.value.tenNguoiNhan = currentInvoice.value.selectedCustomer.hoTen || deliveryInfo.value.tenNguoiNhan
        deliveryInfo.value.soDienThoai = currentInvoice.value.selectedCustomer.soDienThoai || deliveryInfo.value.soDienThoai
      }
    }

    // Product modal
    const showProductModal = ref(false)
    const productSearchQuery = ref('')

    // Customer suggestions
    const showCustomerSuggestions = ref(false)
    const filteredCustomers = ref([])

    // Product suggestions
    const showProductSuggestions = ref(false)

    // Voucher modal and data
    const showVoucherModal = ref(false)
    const vouchers = ref([])
    const availableVouchers = ref([])

    // Manual voucher input
    const voucherTab = ref('select')
    const manualVoucherCode = ref('')
    const manualVoucherLoading = ref(false)
    const manualVoucherResult = ref(null)

    // IMEI Modal
    const showImeiModal = ref(false)
    const selectedProduct = ref(null)
    const availableImeis = ref([])
    const imeiSearchQuery = ref('')
    const selectedImeis = ref([])
    const quantityToAdd = ref(1)

    // Reserved IMEIs shared across all invoices to avoid double-pick
    const reservedImeis = ref(new Set())

    const addReservedImeis = (imeis) => {
      const set = reservedImeis.value
      imeis.forEach((i) => set.add(i))
      // force reactivity by recreating set
      reservedImeis.value = new Set(set)
    }

    const releaseReservedImeis = (imeis) => {
      const set = reservedImeis.value
      imeis.forEach((i) => set.delete(i))
      reservedImeis.value = new Set(set)
    }

    // Quick Customer Modal
    const showQuickCustomerModal = ref(false)
    const quickCustomerForm = ref({
      hoTen: '',
      soDienThoai: '',
      email: '',
      diaChi: ''
    })

    // Computed properties
    const currentInvoice = computed(() => {
      return invoices.value.find(inv => inv.id === activeInvoiceId.value) || invoices.value[0]
    })

    const formattedCustomerPaid = computed({
      get() {
        return currentInvoice.value.customerPaid ? currentInvoice.value.customerPaid.toLocaleString('vi-VN') : ''
      },
      set(value) {
        // This will be handled by handleCustomerPaidInput
      }
    })

    const filteredProducts = computed(() => {
      let filtered = products.value.filter(product =>
        product.trangThai === 1 && product.soLuongTon > 0
      )

      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(product =>
          product.tenSanPham.toLowerCase().includes(query) ||
          product.maCtsp.toLowerCase().includes(query)
        )
      }

      return filtered
    })

    const modalFilteredProducts = computed(() => {
      let filtered = products.value.filter(product =>
        product.trangThai === 1 && product.soLuongTon > 0
      )

      if (productSearchQuery.value) {
        const query = productSearchQuery.value.toLowerCase()
        filtered = filtered.filter(product =>
          product.tenSanPham.toLowerCase().includes(query) ||
          product.maCtsp.toLowerCase().includes(query)
        )
      }

      return filtered
    })

    // Invoice navigation computed properties
    const currentInvoiceIndex = computed(() => {
      return invoices.value.findIndex(inv => inv.id === activeInvoiceId.value)
    })

    const visibleInvoices = computed(() => {
      const maxVisible = 5
      const currentIndex = currentInvoiceIndex.value
      const totalInvoices = invoices.value.length

      if (totalInvoices <= maxVisible) {
        return invoices.value
      }

      let startIndex = Math.max(0, currentIndex - Math.floor(maxVisible / 2))
      let endIndex = Math.min(totalInvoices, startIndex + maxVisible)

      if (endIndex - startIndex < maxVisible) {
        startIndex = Math.max(0, endIndex - maxVisible)
      }

      return invoices.value.slice(startIndex, endIndex)
    })

    // Best voucher suggestion
    const bestVoucher = computed(() => {
      if (currentInvoice.value.selectedVoucher || availableVouchers.value.length === 0) {
        return null
      }

      const subtotal = currentInvoice.value.subtotal
      const eligibleVouchers = availableVouchers.value.filter(voucher =>
        !voucher.hoaDonToiThieu || subtotal >= voucher.hoaDonToiThieu
      )

      if (eligibleVouchers.length === 0) return null

      // Find voucher with highest discount amount
      return eligibleVouchers.reduce((best, current) => {
        const bestDiscount = calculateVoucherDiscountAmount(best)
        const currentDiscount = calculateVoucherDiscountAmount(current)
        return currentDiscount > bestDiscount ? current : best
      })
    })

    // Combined payment computed properties
    const formattedCashAmount = computed({
      get() {
        return currentInvoice.value.cashAmount ? currentInvoice.value.cashAmount.toLocaleString('vi-VN') : ''
      },
      set(value) {
        // This will be handled by handleCashAmountInput
      }
    })

    const formattedVnpayAmount = computed({
      get() {
        return currentInvoice.value.vnpayAmount ? currentInvoice.value.vnpayAmount.toLocaleString('vi-VN') : ''
      },
      set(value) {
        // This will be handled by handleVnpayAmountInput
      }
    })

    const isPaymentAmountValid = computed(() => {
      const totalPayment = (currentInvoice.value.cashAmount || 0) + (currentInvoice.value.vnpayAmount || 0)
      return Math.abs(totalPayment - currentInvoice.value.total) < 1000 // Allow 1k difference for rounding
    })

    // Filtered IMEIs for search
    const filteredImeis = computed(() => {
      if (!imeiSearchQuery.value) return availableImeis.value
      return availableImeis.value.filter(imei =>
        imei.imei.includes(imeiSearchQuery.value)
      )
    })

    // Methods
    const updateTime = () => {
      const now = new Date()
      currentTime.value = now.toLocaleTimeString('vi-VN')
      currentDate.value = now.toLocaleDateString('vi-VN', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }

    // Load previous addresses and provinces when delivery mode is active or customer changes
    watch([() => saleMode.value, () => currentInvoice.value?.selectedCustomer?.id], async ([mode]) => {
      if (mode === 'delivery') {
        await Promise.all([
          loadProvinces(),
          loadPreviousAddresses()
        ])
      }
    })

    const loadProducts = async () => {
      try {
        console.log('Loading products from API...')

        // Lưu lại tồn kho đã điều chỉnh từ cart items trước khi reload (cho tất cả invoices)
        const stockAdjustments = new Map()
        invoices.value.forEach(invoice => {
          if (invoice?.cartItems) {
            invoice.cartItems.forEach(item => {
              const ctspId = item.chiTietSanPhamId
              if (ctspId) {
                // Tính tổng số lượng đã thêm vào cart cho sản phẩm này
                const currentQty = stockAdjustments.get(ctspId) || 0
                stockAdjustments.set(ctspId, currentQty + (item.quantity || 0))
              }
            })
          }
        })

        // Test API connection first
        try {
          const testResponse = await api.get('/api/san-pham-pos/test')
          console.log('API Test Response:', testResponse.data)

          // Debug database
          const debugResponse = await api.get('/api/san-pham-pos/debug')
          console.log('Database Debug Info:', debugResponse.data)

          // Check raw data
          const rawDataResponse = await api.get('/api/san-pham-pos/raw-data')
          console.log('Raw Database Data:', rawDataResponse.data)
        } catch (testError) {
          console.error('API Test failed:', testError)
        }

        // Thử endpoint chính trước
        let response = await api.get('/api/san-pham-pos')
        console.log('Main API Response:', response)
        console.log('Main Products data:', response.data)
        console.log('Main Number of products:', response.data?.length || 0)

        // Nếu không có sản phẩm, thử endpoint force
        if (!response.data || response.data.length === 0) {
          console.log('No products from main API, trying force endpoint...')
          response = await api.get('/api/san-pham-pos/force-products')
          console.log('Force API Response:', response)
          console.log('Force Products data:', response.data)
          console.log('Force Number of products:', response.data?.length || 0)
        }

        products.value = response.data || []

        // Kiểm tra và cập nhật giá cho các cart items trong tất cả invoices
        // Đánh dấu priceChanged nếu giá đã thay đổi (để thông báo ở phiên tiếp theo)
        const priceChangedItems = []
        invoices.value.forEach(invoice => {
          if (invoice.cartItems) {
            invoice.cartItems.forEach(item => {
              const product = products.value.find(p => p.chiTietSanPhamId === item.chiTietSanPhamId)
              if (product) {
                const newPrice = Number(product.gia || product.giaSauGiam || product.giaGoc || 0)
                const oldPrice = Number(item.giaKhiThem || item.gia || 0)

                // Nếu giá mới khác với giá khi thêm vào cart
                if (newPrice > 0 && newPrice !== oldPrice) {
                  // Đánh dấu giá đã thay đổi nhưng KHÔNG cập nhật giá trong cart
                  // Giữ nguyên giá cũ (giaKhiThem) để bán theo giá niêm yết tại thời điểm thêm vào
                  item.priceChanged = true
                  item.oldPrice = oldPrice // Lưu giá cũ để hiển thị
                  // KHÔNG cập nhật item.gia - vẫn giữ giá cũ để bán theo giá niêm yết

                  if (!priceChangedItems.find(p => p.chiTietSanPhamId === item.chiTietSanPhamId)) {
                    priceChangedItems.push({
                      chiTietSanPhamId: item.chiTietSanPhamId,
                      tenSanPham: item.tenSanPham,
                      oldPrice: oldPrice,
                      newPrice: newPrice
                    })
                  }
                } else {
                  // Giá không thay đổi, xóa đánh dấu nếu có
                  item.priceChanged = false
                  item.oldPrice = null
                }
              }
            })
          }
        })

        // Thông báo nếu có giá thay đổi (chỉ thông báo một lần khi load products)
        if (priceChangedItems.length > 0) {
          const itemNames = priceChangedItems.slice(0, 3).map(p => p.tenSanPham).join(', ')
          toastRef.value?.info('Thông báo',
            `Giá của ${priceChangedItems.length} sản phẩm đã thay đổi. Các sản phẩm trong hóa đơn chờ vẫn bán theo giá cũ. ${itemNames}${priceChangedItems.length > 3 ? '...' : ''}`)
        }

        // Áp dụng lại các điều chỉnh tồn kho từ cart items
        if (stockAdjustments.size > 0) {
          stockAdjustments.forEach((qty, ctspId) => {
            adjustStock(ctspId, -qty)
          })
        }

        if (products.value.length === 0) {
          console.warn('No products found in response')
          toastRef.value?.warning('Cảnh báo', 'Không có sản phẩm nào trong hệ thống')
        }
      } catch (error) {
        console.error('Error loading products:', error)
        console.error('Error details:', error.response?.data || error.message)
        console.error('Error status:', error.response?.status)
        console.error('Error config:', error.config)
        toastRef.value?.error('Lỗi', 'Không thể tải sản phẩm: ' + (error.response?.data?.message || error.message))
      }
    }

    const loadCustomers = async () => {
      try {
        const response = await api.get('/api/khach-hang')
        customers.value = response.data
      } catch (error) {
        console.error('Error loading customers:', error)
        toastRef.value?.error('Lỗi', 'Không thể tải khách hàng')
      }
    }

    const searchProducts = () => {
      // Products are filtered via computed property
      console.log('Search query:', searchQuery.value)
      console.log('All products:', products.value.length)
      console.log('Filtered products:', filteredProducts.value.length)
      console.log('Show suggestions:', showProductSuggestions.value)
    }

    const searchCustomers = async () => {
      if (!customerSearch.value || customerSearch.value.length < 2) {
        filteredCustomers.value = []
        showCustomerSuggestions.value = false
        return
      }

      try {
        const response = await api.get(`/api/khach-hang/search?query=${encodeURIComponent(customerSearch.value)}`)
        filteredCustomers.value = response.data
        showCustomerSuggestions.value = true
      } catch (error) {
        console.error('Error searching customers:', error)
        filteredCustomers.value = []
        showCustomerSuggestions.value = false
      }
    }

    const addToCart = (product) => {
      // Validate product status before opening IMEI modal - prevent inactive products
      if (product.trangThai !== undefined && product.trangThai !== 1) {
        toastRef.value?.warning('Cảnh báo', `"${product.tenSanPham}" đã ngừng hoạt động, không thể thêm vào giỏ hàng`)
        return
      }

      if (product.trangThaiSanPham !== undefined && product.trangThaiSanPham !== 1) {
        toastRef.value?.warning('Cảnh báo', `"${product.tenSanPham}" đã ngừng hoạt động, không thể thêm vào giỏ hàng`)
        return
      }

      // Mỗi sản phẩm là một item riêng, không cần check existing item
      // Luôn mở IMEI modal để chọn IMEI mới
      selectedProduct.value = product
      quantityToAdd.value = 1
      selectedImeis.value = []
      loadAvailableImeis(product.chiTietSanPhamId)
      showImeiModal.value = true
    }

    const addToCartWithQuantity = (product, quantity) => {
      // Open IMEI selection modal with specific quantity
      selectedProduct.value = product
      quantityToAdd.value = quantity
      selectedImeis.value = []
      loadAvailableImeis(product.chiTietSanPhamId)
      showImeiModal.value = true
    }

    const loadAvailableImeis = async (chiTietSanPhamId) => {
      try {
        // Load available IMEIs for the specific product variant
        const response = await api.get(`/api/imei/chi-tiet/${chiTietSanPhamId}`)
        // Filter only available IMEIs (trangThai = 1)
        const raw = (response.data || []).filter(imei => imei.trangThai === 1)
        // Exclude IMEIs already reserved in other invoices
        const reserved = reservedImeis.value
        availableImeis.value = raw.filter(imei => !reserved.has(imei.imei))
      } catch (error) {
        console.error('Error loading IMEIs:', error)
        availableImeis.value = []
        toastRef.value?.error('Lỗi', 'Không thể tải danh sách IMEI')
      }
    }

    const loadAvailableImeisForExistingProduct = async (chiTietSanPhamId, excludeImeis = []) => {
      try {
        // Load available IMEIs for the specific product variant
        const response = await api.get(`/api/imei/chi-tiet/${chiTietSanPhamId}`)
        // Filter only available IMEIs (trangThai = 1)
        let allImeis = (response.data || []).filter(imei => imei.trangThai === 1)

        // Get already used IMEIs - từ excludeImeis (thường là IMEIs của item hiện tại)
        const usedImeis = excludeImeis || []

        // Filter out already used in this item and reserved globally by other invoices
        const reserved = reservedImeis.value
        availableImeis.value = allImeis.filter(imei => !usedImeis.includes(imei.imei) && !reserved.has(imei.imei))
      } catch (error) {
        console.error('Error loading IMEIs for existing product:', error)
        availableImeis.value = []
        toastRef.value?.error('Lỗi', 'Không thể tải danh sách IMEI')
      }
    }

    const selectImei = (imei) => {
      const index = selectedImeis.value.findIndex(selected => selected.id === imei.id)
      if (index > -1) {
        // Remove if already selected
        selectedImeis.value.splice(index, 1)
      } else {
        // Add if not selected and within quantity limit
        if (selectedImeis.value.length < quantityToAdd.value) {
          selectedImeis.value.push(imei)
        } else {
          toastRef.value?.warning('Cảnh báo', `Chỉ có thể chọn tối đa ${quantityToAdd.value} IMEI`)
        }
      }
    }

    const confirmImeiSelection = async () => {
      if (selectedImeis.value.length !== quantityToAdd.value || !selectedProduct.value) {
        toastRef.value?.error('Lỗi', `Vui lòng chọn đúng ${quantityToAdd.value} IMEI`)
        return
      }

      // Validate product status before adding to cart - prevent inactive products
      try {
        const res = await api.get(`/api/san-pham-pos/${selectedProduct.value.chiTietSanPhamId}`)
        const fresh = res.data || {}

        // Check if product detail (CTSP) is inactive
        if (fresh.trangThai !== undefined && fresh.trangThai !== 1) {
          toastRef.value?.warning('Cảnh báo', `"${selectedProduct.value.tenSanPham}" đã ngừng hoạt động, không thể thêm vào giỏ hàng`)
          closeImeiModal()
          return
        }

        // Check if parent product (SP) is inactive
        if (fresh.trangThaiSanPham !== undefined && fresh.trangThaiSanPham !== 1) {
          toastRef.value?.warning('Cảnh báo', `"${selectedProduct.value.tenSanPham}" đã ngừng hoạt động, không thể thêm vào giỏ hàng`)
          closeImeiModal()
          return
        }
      } catch (error) {
        console.warn('Warning validating product status:', error)
        toastRef.value?.warning('Cảnh báo', 'Không thể kiểm tra trạng thái sản phẩm, vui lòng thử lại')
        return
      }

      // Check if enough IMEIs are available
      if (selectedImeis.value.length > availableImeis.value.length) {
        toastRef.value?.warning('Cảnh báo', 'Không đủ IMEI khả dụng')
        return
      }

      // Kiểm tra xem có phải đang thêm vào item hiện tại (từ nút +) hay thêm SPCT mới
      // Nếu có item đang được chỉnh sửa (từ increaseQuantity), thêm IMEI vào item đó
      const currentEditingItem = currentInvoice.value.cartItems.find(item =>
        item.chiTietSanPhamId === selectedProduct.value.chiTietSanPhamId &&
        item.quantity > (item.selectedImeis?.length || 0)
      )

      if (currentEditingItem) {
        // Đang thêm IMEI vào item hiện tại (từ nút +)
        const newImeis = selectedImeis.value.map(imei => imei.imei)

        // Add new IMEIs to existing ones
        currentEditingItem.selectedImeis = [...(currentEditingItem.selectedImeis || []), ...newImeis]
        // Quantity đã được tăng trước đó trong increaseQuantity, không cần tăng lại

        // Decrease stock immediately
        adjustStock(selectedProduct.value.chiTietSanPhamId, -quantityToAdd.value)
        // Reserve IMEIs globally
        addReservedImeis(newImeis)

        calculateTotals()
        toastRef.value?.success('Thành công', `Đã thêm ${quantityToAdd.value} IMEI cho ${selectedProduct.value.tenSanPham}`)
      } else {
        // Thêm SPCT mới - luôn tạo cart item mới (không gộp vào item hiện có)
        // Decrease stock immediately TRƯỚC KHI tạo cart item để có giá trị tồn kho chính xác
        adjustStock(selectedProduct.value.chiTietSanPhamId, -quantityToAdd.value)

        const cartItemId = `${selectedProduct.value.chiTietSanPhamId}_${Date.now()}_${Math.random()}`
        currentInvoice.value.cartItems.push({
          cartItemId: cartItemId, // Unique ID cho mỗi item
          chiTietSanPhamId: selectedProduct.value.chiTietSanPhamId,
          tenSanPham: selectedProduct.value.tenSanPham,
          gia: selectedProduct.value.gia, // Giá hiện tại
          giaKhiThem: selectedProduct.value.gia, // Lưu giá tại thời điểm thêm vào cart
          quantity: quantityToAdd.value,
          soLuongTon: selectedProduct.value.soLuongTon, // Đã được cập nhật bởi adjustStock
          tenHang: selectedProduct.value.tenHang,
          tenRam: selectedProduct.value.tenRam,
          tenRom: selectedProduct.value.tenRom,
          tenMauSac: selectedProduct.value.tenMauSac,
          hinhAnh: selectedProduct.value.hinhAnh,
          selectedImeis: selectedImeis.value.map(imei => imei.imei),
          priceChanged: false, // Đánh dấu giá đã thay đổi
          addedAt: Date.now() // Thời điểm thêm vào cart
        })

        // Reserve IMEIs globally
        addReservedImeis(selectedImeis.value.map(imei => imei.imei))

        calculateTotals()
        toastRef.value?.success('Thành công', `Đã thêm ${selectedProduct.value.tenSanPham} với ${selectedImeis.value.length} IMEI`)
      }

      closeImeiModal()
    }

    const closeImeiModal = () => {
      showImeiModal.value = false
      selectedProduct.value = null
      availableImeis.value = []
      imeiSearchQuery.value = ''
      selectedImeis.value = []
      quantityToAdd.value = 1
    }

    // Quick Customer functions
    const openQuickCustomerModal = () => {
      quickCustomerForm.value = {
        hoTen: '',
        soDienThoai: '',
        email: '',
        diaChi: ''
      }
      showQuickCustomerModal.value = true
    }

    const createQuickCustomer = async () => {
      try {
        // Validate required fields
        if (!quickCustomerForm.value.hoTen.trim() || !quickCustomerForm.value.soDienThoai.trim()) {
          toastRef.value?.error('Lỗi', 'Vui lòng nhập họ tên và số điện thoại')
          return
        }

        // Create customer
        const response = await api.post('/api/khach-hang', quickCustomerForm.value)
        const newCustomer = response.data

        // Add to customers list
        customers.value.unshift(newCustomer)

        // Auto-select the new customer
        selectCustomer(newCustomer)

        // Close modal
        showQuickCustomerModal.value = false

        toastRef.value?.success('Thành công', `Đã tạo khách hàng ${newCustomer.hoTen}`)
      } catch (error) {
        console.error('Error creating customer:', error)
        toastRef.value?.error('Lỗi', 'Không thể tạo khách hàng mới')
      }
    }

    const closeQuickCustomerModal = () => {
      showQuickCustomerModal.value = false
      quickCustomerForm.value = {
        hoTen: '',
        soDienThoai: '',
        email: '',
        diaChi: ''
      }
    }

    const increaseQuantity = (cartItemId) => {
      const item = currentInvoice.value.cartItems.find(item => (item.cartItemId || item.chiTietSanPhamId) === cartItemId)
      if (item && item.quantity < item.soLuongTon) {
        // Tăng quantity cho item hiện tại
        item.quantity = (item.quantity || 1) + 1

        // Cần thêm IMEI mới cho item này - mở modal chọn IMEI
        const product = {
          chiTietSanPhamId: item.chiTietSanPhamId,
          tenSanPham: item.tenSanPham,
          gia: item.gia,
          soLuongTon: item.soLuongTon,
          tenHang: item.tenHang,
          tenRam: item.tenRam,
          tenRom: item.tenRom,
          tenMauSac: item.tenMauSac,
          hinhAnh: item.hinhAnh
        }

        selectedProduct.value = product
        quantityToAdd.value = 1
        selectedImeis.value = []
        // Load IMEIs nhưng không bao gồm IMEI đã chọn trong item hiện tại
        loadAvailableImeisForExistingProduct(item.chiTietSanPhamId, item.selectedImeis || [])
        showImeiModal.value = true
      } else {
        toastRef.value?.warning('Cảnh báo', 'Không đủ hàng trong kho')
      }
    }

    const decreaseQuantity = (cartItemId) => {
      const item = currentInvoice.value.cartItems.find(item => (item.cartItemId || item.chiTietSanPhamId) === cartItemId)
      if (item && item.quantity > 1) {
        item.quantity -= 1

        // Remove one IMEI from the list (the last one)
        if (item.selectedImeis && item.selectedImeis.length > 0) {
          const released = item.selectedImeis.pop()
          if (released) releaseReservedImeis([released])
        }

        // Return stock by 1
        adjustStock(item.chiTietSanPhamId, +1)

        calculateTotals()
      } else if (item && item.quantity === 1) {
        // Nếu quantity = 1, xóa luôn item
        removeFromCart(cartItemId)
      }
    }

    const removeFromCart = (cartItemId) => {
      const index = currentInvoice.value.cartItems.findIndex(item => (item.cartItemId || item.chiTietSanPhamId) === cartItemId)
      if (index > -1) {
        const item = currentInvoice.value.cartItems[index]
        // Return reserved stock for this item
        const qty = item.quantity || 1
        const imeis = item.selectedImeis || []
        if (qty > 0) adjustStock(item.chiTietSanPhamId, +qty)
        if (imeis.length > 0) releaseReservedImeis(imeis)
        currentInvoice.value.cartItems.splice(index, 1)
        calculateTotals()
      }
    }

    const updateQuantity = (cartItemId, newQuantity) => {
      const quantity = parseInt(newQuantity) || 1
      const item = currentInvoice.value.cartItems.find(item => (item.cartItemId || item.chiTietSanPhamId) === cartItemId)
      if (item) {
        if (quantity > item.soLuongTon) {
          toastRef.value?.warning('Cảnh báo', 'Không đủ hàng trong kho')
          return
        }
        if (quantity <= 0) {
          removeFromCart(cartItemId)
          return
        }

        const oldQuantity = item.quantity || 1
        const diff = quantity - oldQuantity

        if (diff > 0) {
          // Increase quantity - need to select more IMEIs
          // Tăng quantity trước
          item.quantity = quantity
          // Mở modal để chọn IMEI bổ sung
          const product = {
            chiTietSanPhamId: item.chiTietSanPhamId,
            tenSanPham: item.tenSanPham,
            gia: item.gia,
            soLuongTon: item.soLuongTon,
            tenHang: item.tenHang,
            tenRam: item.tenRam,
            tenRom: item.tenRom,
            tenMauSac: item.tenMauSac,
            hinhAnh: item.hinhAnh
          }

          selectedProduct.value = product
          quantityToAdd.value = diff
          selectedImeis.value = []
          loadAvailableImeisForExistingProduct(item.chiTietSanPhamId, item.selectedImeis || [])
          showImeiModal.value = true
        } else if (diff < 0) {
          // Decrease quantity - remove IMEIs
          const removeCount = Math.abs(diff)
          if (item.selectedImeis && item.selectedImeis.length > 0) {
            for (let i = 0; i < removeCount && item.selectedImeis.length > 0; i++) {
              const released = item.selectedImeis.pop()
              if (released) releaseReservedImeis([released])
            }
          }
          item.quantity = quantity
          adjustStock(item.chiTietSanPhamId, +Math.abs(diff))
          calculateTotals()
        }
      }
    }

    const calculateTotals = () => {
      // Sử dụng giaKhiThem (giá tại thời điểm thêm vào) nếu có, nếu không thì dùng gia
      currentInvoice.value.subtotal = currentInvoice.value.cartItems.reduce((sum, item) => {
        const price = item.giaKhiThem || item.gia || 0
        return sum + (price * item.quantity)
      }, 0)

      // Calculate voucher discount
      let voucherDiscount = 0
      if (currentInvoice.value.selectedVoucher) {
        voucherDiscount = calculateVoucherDiscountAmount(currentInvoice.value.selectedVoucher)
      }

      currentInvoice.value.discount = voucherDiscount
      currentInvoice.value.total = currentInvoice.value.subtotal - currentInvoice.value.discount

      // Update available vouchers when subtotal changes
      loadAvailableVouchers()

      // Preview totals with server for consistency
      previewTotalsWithServer()
    }

    const previewTotalsWithServer = async () => {
      try {
        const voucherCode = currentInvoice.value.selectedVoucher?.maPhieuGiamGia || null
        const { data } = await api.post('/api/orders/preview', {
          subtotal: currentInvoice.value.subtotal,
          voucherCode,
          customerId: currentInvoice.value.selectedCustomerId || null,
          paymentMethod: currentInvoice.value.paymentMethod
        })
        if (data) {
          currentInvoice.value.discount = Number(data.discount || 0)
          currentInvoice.value.total = Number(data.total || currentInvoice.value.subtotal)
        }
      } catch (e) {
        // fallback giữ logic hiện tại nếu preview lỗi
        // Revalidate current voucher: if no longer available, remove and suggest
        revalidateCurrentVoucher()
      }
    }

    const setQuickAmount = (amount) => {
      currentInvoice.value.customerPaid = amount
      calculateChange()
    }

    const addToCartFromModal = (product) => {
      addToCart(product)
      showProductModal.value = false
    }

    // Invoice navigation methods
    const previousInvoice = () => {
      const currentIndex = currentInvoiceIndex.value
      if (currentIndex > 0) {
        setActiveInvoice(invoices.value[currentIndex - 1].id)
      }
    }

    const nextInvoice = () => {
      const currentIndex = currentInvoiceIndex.value
      if (currentIndex < invoices.value.length - 1) {
        setActiveInvoice(invoices.value[currentIndex + 1].id)
      }
    }

    const getActualIndex = (invoiceId) => {
      return invoices.value.findIndex(inv => inv.id === invoiceId)
    }

    // Drag and drop methods
    const draggedIndex = ref(null)

    const onDragStart = (event, index) => {
      draggedIndex.value = index
      event.dataTransfer.effectAllowed = 'move'
    }

    const onDragOver = (event) => {
      event.preventDefault()
      event.dataTransfer.dropEffect = 'move'
    }

    const onDrop = (event, dropIndex) => {
      event.preventDefault()

      if (draggedIndex.value === null || draggedIndex.value === dropIndex) {
        return
      }

      // Reorder invoices array
      const draggedInvoice = invoices.value[draggedIndex.value]
      invoices.value.splice(draggedIndex.value, 1)
      invoices.value.splice(dropIndex, 0, draggedInvoice)

      draggedIndex.value = null
    }

    // Adjust stock in product list by ctsp id
    const adjustStock = (chiTietSanPhamId, delta) => {
      const p = products.value.find(pr => pr.chiTietSanPhamId === chiTietSanPhamId || pr.id === chiTietSanPhamId)
      if (p) {
        const next = Math.max(0, (p.soLuongTon || 0) + delta)
        p.soLuongTon = next

        // Cập nhật selectedProduct nếu đang chỉnh sửa sản phẩm này
        if (selectedProduct.value && (selectedProduct.value.chiTietSanPhamId === chiTietSanPhamId || selectedProduct.value.id === chiTietSanPhamId)) {
          selectedProduct.value.soLuongTon = next
        }

        // Cập nhật soLuongTon trong tất cả cart items có cùng chiTietSanPhamId (cho tất cả invoices)
        invoices.value.forEach(invoice => {
          if (invoice?.cartItems) {
            invoice.cartItems.forEach(item => {
              if (item.chiTietSanPhamId === chiTietSanPhamId) {
                item.soLuongTon = next
              }
            })
          }
        })
      }
    }

    // Customer selection methods
    const selectCustomer = (customer) => {
      currentInvoice.value.selectedCustomer = customer
      currentInvoice.value.selectedCustomerId = customer.id
      customerSearch.value = customer.hoTen
      showCustomerSuggestions.value = false
      // Load customer-specific vouchers
      loadCustomerVouchers(customer.id)
    }

    const clearSelectedCustomer = () => {
      currentInvoice.value.selectedCustomer = null
      currentInvoice.value.selectedCustomerId = ''
      customerSearch.value = ''
      showCustomerSuggestions.value = false
      // Reload only public vouchers
      loadVouchers()
      loadAvailableVouchers()
    }

    const removeCustomer = () => {
      clearSelectedCustomer()
    }

    const hideCustomerSuggestions = () => {
      // Delay hiding to allow click on suggestions
      setTimeout(() => {
        showCustomerSuggestions.value = false
      }, 200)
    }

    const hideProductSuggestions = () => {
      // Delay hiding to allow click on suggestions
      setTimeout(() => {
        showProductSuggestions.value = false
      }, 200)
    }

    const addToCartFromSearch = async (product) => {
      // Validate product status before adding to cart - prevent inactive products
      if (product.trangThai !== undefined && product.trangThai !== 1) {
        toastRef.value?.warning('Cảnh báo', `"${product.tenSanPham}" đã ngừng hoạt động, không thể thêm vào giỏ hàng`)
        return
      }

      if (product.trangThaiSanPham !== undefined && product.trangThaiSanPham !== 1) {
        toastRef.value?.warning('Cảnh báo', `"${product.tenSanPham}" đã ngừng hoạt động, không thể thêm vào giỏ hàng`)
        return
      }

      addToCart(product)
      showProductSuggestions.value = false
      searchQuery.value = '' // Clear search after adding
    }

    // Voucher methods
    const loadVouchers = async () => {
      try {
        // Load all public vouchers (non-private)
        const response = await api.get('/api/phieu-giam-gia')
        const allVouchers = response.data
        // Filter only public vouchers
        vouchers.value = allVouchers.filter(voucher => !voucher.riengTu)
      } catch (error) {
        console.error('Error loading vouchers:', error)
        vouchers.value = []
      }
    }

    const loadCustomerVouchers = async (customerId) => {
      if (!customerId) {
        loadAvailableVouchers()
        return
      }

      try {
        // Load all vouchers (public + private for this customer)
        const [publicResponse, privateResponsePrimary] = await Promise.all([
          api.get('/api/phieu-giam-gia'), // Public vouchers
          api.get(`/api/phieu-giam-gia/customer/${customerId}`).catch(() => ({ data: [] })) // Private vouchers (primary path)
        ])

        // Fallback alternate path if backend maps khác
        let privateData = privateResponsePrimary.data || []
        if (!privateData || privateData.length === 0) {
          try {
            const alt = await api.get(`/api/phieu-giam-gia/khach-hang/${customerId}`)
            privateData = alt.data || []
          } catch {}
        }

        const publicVouchers = (publicResponse.data || []).filter(voucher => !voucher.riengTu)
        // Normalize private vouchers: some APIs may return relation objects { phieuGiamGia: {...} }
        let privateVouchers = privateData || []
        privateVouchers = privateVouchers.map((it) => (it && it.phieuGiamGia ? it.phieuGiamGia : it))

        // Combine all vouchers
        const allVouchers = [...publicVouchers, ...privateVouchers]

        // Remove duplicates based on ID
        const uniqueVouchers = allVouchers.filter((voucher, index, self) =>
          index === self.findIndex(v => v.id === voucher.id)
        )

        console.log('[Voucher] public:', publicVouchers.length, 'private:', privateVouchers.length, 'combined:', uniqueVouchers.length)
        console.log('[Voucher] example:', uniqueVouchers[0])
        // Sort: applicable first, private first, higher discount first, nearest expiry
        vouchers.value = uniqueVouchers.sort((a, b) => {
          const apA = isVoucherApplicableLocal(a) ? 1 : 0
          const apB = isVoucherApplicableLocal(b) ? 1 : 0
          if (apA !== apB) return apB - apA
          const prA = a.riengTu ? 1 : 0
          const prB = b.riengTu ? 1 : 0
          if (prA !== prB) return prB - prA
          const dA = Number(calculateVoucherDiscount(a).replace(/[^0-9]/g,''))
          const dB = Number(calculateVoucherDiscount(b).replace(/[^0-9]/g,''))
          if (dA !== dB) return dB - dA
          const eA = a.ngayKetThuc ? new Date(a.ngayKetThuc).getTime() : Infinity
          const eB = b.ngayKetThuc ? new Date(b.ngayKetThuc).getTime() : Infinity
          return eA - eB
        })
        if (typeof window !== 'undefined') {
          window.__POS_VOUCHERS__ = vouchers
        }
        loadAvailableVouchers(customerId)
      } catch (error) {
        console.error('Error loading customer vouchers:', error)
        loadAvailableVouchers()
      }
    }

    const loadAvailableVouchers = (customerId = null) => {
      const subtotal = currentInvoice.value.subtotal
      availableVouchers.value = vouchers.value.filter(voucher => {
        // Check if voucher is active and has quantity
        if (voucher.trangThai !== 1 || voucher.soLuongDung <= 0) return false

        // Check if order meets minimum requirement
        if (voucher.hoaDonToiThieu && subtotal < voucher.hoaDonToiThieu) return false

        // Check date validity
        const now = new Date()
        const startDate = new Date(voucher.ngayBatDau)
        const endDate = new Date(voucher.ngayKetThuc)
        if (now < startDate || now > endDate) return false

        // For private vouchers, only show if customer is selected
        if (voucher.riengTu && !customerId) return false

        return true
      })
    }

    const isVoucherApplicableLocal = (voucher) => {
      try {
        const subtotal = currentInvoice.value.subtotal
        if (voucher.trangThai !== 1 || voucher.soLuongDung <= 0) return false
        if (voucher.hoaDonToiThieu && subtotal < voucher.hoaDonToiThieu) return false
        const now = new Date()
        const startDate = voucher.ngayBatDau ? new Date(voucher.ngayBatDau) : null
        const endDate = voucher.ngayKetThuc ? new Date(voucher.ngayKetThuc) : null
        if (startDate && now < startDate) return false
        if (endDate && now > endDate) return false
        if (voucher.riengTu && !currentInvoice.value.selectedCustomerId) return false
        return true
      } catch { return false }
    }

    // Ensure items in cart are still sellable and prices up to date

    const revalidateCurrentVoucher = async (openChooserOnInvalid = false) => {
      const v = currentInvoice.value.selectedVoucher
      if (!v) return true

      // If voucher not in available list anymore, remove and auto-apply best voucher
      const stillAvailable = availableVouchers.value.some(av => av.id === v.id)
      if (!stillAvailable) {
        const code = v.maPhieuGiamGia || v.ma || 'voucher'
        currentInvoice.value.selectedVoucher = null
        calculateTotals()

        // Tự động áp dụng voucher tốt nhất mới (nếu có)
        await loadAvailableVouchers()
        if (bestVoucher.value) {
          selectVoucher(bestVoucher.value)
          toastRef.value?.info('Đã áp dụng voucher mới', `Voucher "${code}" không còn hợp lệ. Đã tự động áp dụng voucher tốt nhất: ${bestVoucher.value.tenPhieuGiamGia}`)
        } else {
          toastRef.value?.warning('Voucher không còn hợp lệ', `Không thể sử dụng ${code} nữa. Vui lòng chọn voucher khác.`)
        }

        if (openChooserOnInvalid && !bestVoucher.value) {
          // mở modal chọn voucher và gợi ý cái tốt nhất
          showVoucherModal.value = true
        }
        return false
      }

      // Validate usage for customer via API if customer selected
      try {
        // Hard validation from server: status, dates, quantity
        if (v.maPhieuGiamGia) {
          const resp = await api.get(`/api/phieu-giam-gia/by-code/${v.maPhieuGiamGia}`)
          const fresh = resp.data || {}
          const now = new Date()
          const startOk = !fresh.ngayBatDau || new Date(fresh.ngayBatDau) <= now
          const endOk = !fresh.ngayKetThuc || new Date(fresh.ngayKetThuc) >= now
          const statusOk = fresh.trangThai === 1
          const qtyOk = (fresh.soLuongDung ?? 0) > 0
          if (!(startOk && endOk && statusOk && qtyOk)) throw new Error('Voucher invalid by status/time/quantity')
        }
        if (currentInvoice.value.selectedCustomerId && v.maPhieuGiamGia) {
          await api.post('/api/phieu-giam-gia/validate-usage', {
            customerId: currentInvoice.value.selectedCustomerId,
            voucherCode: v.maPhieuGiamGia
          })
        }
      } catch (e) {
        const code = v.maPhieuGiamGia || v.ma || 'voucher'
        currentInvoice.value.selectedVoucher = null
        calculateTotals()

        // Tự động áp dụng voucher tốt nhất mới (nếu có)
        await loadAvailableVouchers()
        if (bestVoucher.value) {
          selectVoucher(bestVoucher.value)
          toastRef.value?.info('Đã áp dụng voucher mới', `Voucher "${code}" không còn hợp lệ. Đã tự động áp dụng voucher tốt nhất: ${bestVoucher.value.tenPhieuGiamGia}`)
        } else {
          toastRef.value?.warning('Voucher không hợp lệ', 'Voucher hiện không còn đáp ứng điều kiện. Đã gỡ ra khỏi đơn.')
        }

        if (openChooserOnInvalid && !bestVoucher.value) {
          showVoucherModal.value = true
        }
        return false
      }
      return true
    }

    const selectVoucher = (voucher) => {
      currentInvoice.value.selectedVoucher = voucher
      calculateTotals()
      showVoucherModal.value = false
    }

    const markVoucherAsUsed = async (voucher, customerId) => {
      if (!voucher || !customerId) return

      try {
        // Mark voucher as used for this customer
        await api.post('/api/khach-hang-giam-gia/mark-used', {
          customerId: customerId,
          voucherId: voucher.id,
          usedAt: new Date().toISOString()
        })

        // Remove voucher from available list
        const index = vouchers.value.findIndex(v => v.id === voucher.id)
        if (index > -1) {
          vouchers.value.splice(index, 1)
        }

        // Update available vouchers
        loadAvailableVouchers(customerId)

        console.log('Voucher marked as used:', voucher.maPhieuGiamGia)
      } catch (error) {
        console.error('Error marking voucher as used:', error)
        // Don't show error to user, just log it
      }
    }

    const removeVoucher = () => {
      currentInvoice.value.selectedVoucher = null
      calculateTotals()
    }

    const applyBestVoucher = () => {
      if (bestVoucher.value) {
        selectVoucher(bestVoucher.value)
      }
    }

    // Manual voucher methods
    const applyManualVoucher = async () => {
      if (!manualVoucherCode.value.trim()) {
        toastRef.value?.error('Lỗi', 'Vui lòng nhập mã voucher')
        return
      }

      manualVoucherLoading.value = true
      manualVoucherResult.value = null

      try {
        // Call API to validate voucher by code
        const response = await api.get(`/api/phieu-giam-gia/validate/${manualVoucherCode.value.trim()}`)

        if (response.data && response.data.thanhCong) {
          const voucher = response.data.duLieu

          // Check if voucher is valid for current order
          const subtotal = currentInvoice.value.subtotal
          if (voucher.hoaDonToiThieu && subtotal < voucher.hoaDonToiThieu) {
            manualVoucherResult.value = {
              success: false,
              message: `Đơn hàng tối thiểu ${formatCurrency(voucher.hoaDonToiThieu)} để sử dụng voucher này`
            }
            return
          }

          // Check if voucher is active
          if (voucher.trangThai !== 1) {
            manualVoucherResult.value = {
              success: false,
              message: 'Voucher này đã hết hạn hoặc không còn hiệu lực'
            }
            return
          }

          // Check if voucher has remaining uses
          if (voucher.soLuongDung <= 0) {
            manualVoucherResult.value = {
              success: false,
              message: 'Voucher này đã hết lượt sử dụng'
            }
            return
          }

          // Check date validity
          const now = new Date()
          const startDate = new Date(voucher.ngayBatDau)
          const endDate = new Date(voucher.ngayKetThuc)

          if (now < startDate || now > endDate) {
            manualVoucherResult.value = {
              success: false,
              message: 'Voucher này chưa có hiệu lực hoặc đã hết hạn'
            }
            return
          }

          // Calculate discount amount
          let discountAmount = 0
          if (voucher.loaiPhieuGiamGia === 'PERCENT' || voucher.loaiPhieuGiamGia === 1) {
            discountAmount = subtotal * (voucher.giaTriGiamGia || 0) / 100
            const maxDiscount = voucher.soTienGiamToiDa || Infinity
            discountAmount = Math.min(discountAmount, maxDiscount)
          } else {
            discountAmount = Math.min(voucher.giaTriGiamGia || 0, subtotal)
          }

          manualVoucherResult.value = {
            success: true,
            voucher: voucher,
            discountAmount: discountAmount
          }

        } else {
          manualVoucherResult.value = {
            success: false,
            message: 'Không tìm thấy voucher với mã này'
          }
        }
      } catch (error) {
        console.error('Error validating manual voucher:', error)
        manualVoucherResult.value = {
          success: false,
          message: 'Có lỗi xảy ra khi kiểm tra voucher. Vui lòng thử lại.'
        }
      } finally {
        manualVoucherLoading.value = false
      }
    }

    const selectManualVoucher = () => {
      if (manualVoucherResult.value && manualVoucherResult.value.success) {
        selectVoucher(manualVoucherResult.value.voucher)
        clearManualVoucher()
      }
    }

    const clearManualVoucher = () => {
      manualVoucherCode.value = ''
      manualVoucherResult.value = null
    }

    const openVoucherModal = async () => {
      voucherTab.value = 'select'
      clearManualVoucher()
      // Refresh vouchers right before opening
      if (currentInvoice.value.selectedCustomerId) {
        await loadCustomerVouchers(currentInvoice.value.selectedCustomerId)
      } else {
        await loadVouchers()
        loadAvailableVouchers()
      }
      // Expose for debugging
      if (typeof window !== 'undefined') {
        window.__POS_VOUCHERS__ = vouchers
      }
      showVoucherModal.value = true
    }

    // Combined payment methods
    const handleCashAmountInput = (event) => {
      const value = event.target.value
      const numericValue = value.replace(/[^\d]/g, '')

      if (numericValue === '') {
        currentInvoice.value.cashAmount = 0
      } else {
        currentInvoice.value.cashAmount = parseInt(numericValue)
      }

      // Auto-calculate VNPay amount
      const remaining = currentInvoice.value.total - currentInvoice.value.cashAmount
      currentInvoice.value.vnpayAmount = Math.max(0, remaining)
    }

    const handleVnpayAmountInput = (event) => {
      const value = event.target.value
      const numericValue = value.replace(/[^\d]/g, '')

      if (numericValue === '') {
        currentInvoice.value.vnpayAmount = 0
      } else {
        currentInvoice.value.vnpayAmount = parseInt(numericValue)
      }

      // Auto-calculate cash amount
      const remaining = currentInvoice.value.total - currentInvoice.value.vnpayAmount
      currentInvoice.value.cashAmount = Math.max(0, remaining)
    }

    // Watch for payment method changes to reset combined payment fields
    const resetCombinedPaymentFields = () => {
      if (currentInvoice.value.paymentMethod !== 'combined') {
        currentInvoice.value.cashAmount = 0
        currentInvoice.value.vnpayAmount = 0
      } else {
        // Initialize combined payment with equal split
        const halfTotal = Math.floor(currentInvoice.value.total / 2)
        currentInvoice.value.cashAmount = halfTotal
        currentInvoice.value.vnpayAmount = currentInvoice.value.total - halfTotal
      }
    }

    const calculateVoucherDiscount = (voucher) => {
      const subtotal = currentInvoice.value.subtotal
      if (voucher.loaiPhieuGiamGia === 'PERCENT' || voucher.loaiPhieuGiamGia === 1) {
        // Percentage discount
        const discount = subtotal * (voucher.giaTriGiamGia || 0) / 100
        const maxDiscount = voucher.soTienGiamToiDa || Infinity
        return formatCurrency(Math.min(discount, maxDiscount))
      } else {
        // Fixed amount discount
        return formatCurrency(Math.min(voucher.giaTriGiamGia || 0, subtotal))
      }
    }

    const calculateVoucherDiscountAmount = (voucher) => {
      const subtotal = currentInvoice.value.subtotal
      if (voucher.loaiPhieuGiamGia === 'PERCENT' || voucher.loaiPhieuGiamGia === 1) {
        // Percentage discount
        const discount = subtotal * (voucher.giaTriGiamGia || 0) / 100
        const maxDiscount = voucher.soTienGiamToiDa || Infinity
        return Math.min(discount, maxDiscount)
      } else {
        // Fixed amount discount
        return Math.min(voucher.giaTriGiamGia || 0, subtotal)
      }
    }

    const calculateChange = () => {
      // This method is called when customer payment amount changes
      // The change calculation is handled in the template
    }

    const handleCustomerPaidInput = (event) => {
      const value = event.target.value
      // Remove all non-numeric characters except dots
      const numericValue = value.replace(/[^\d]/g, '')

      if (numericValue === '') {
        currentInvoice.value.customerPaid = 0
      } else {
        currentInvoice.value.customerPaid = parseInt(numericValue)
      }

      calculateChange()
    }

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }

    const getSuggestedAmounts = () => {
      const total = currentInvoice.value.total
      const suggestions = []

      // Base suggestions
      const baseAmounts = [50000, 100000, 200000, 500000, 1000000, 2000000]

      // Add amounts based on total
      if (total > 0) {
        // Round up to nearest 50k
        const roundedUp = Math.ceil(total / 50000) * 50000
        suggestions.push({
          value: roundedUp,
          label: new Intl.NumberFormat('vi-VN').format(roundedUp)
        })

        // Add 50k more
        suggestions.push({
          value: roundedUp + 50000,
          label: new Intl.NumberFormat('vi-VN').format(roundedUp + 50000)
        })

        // Add 100k more
        suggestions.push({
          value: roundedUp + 100000,
          label: new Intl.NumberFormat('vi-VN').format(roundedUp + 100000)
        })
      }

      // Add base amounts that are reasonable
      baseAmounts.forEach(amount => {
        if (amount >= total * 0.8 && amount <= total * 2) {
          suggestions.push({
            value: amount,
            label: new Intl.NumberFormat('vi-VN').format(amount)
          })
        }
      })

      // Remove duplicates and sort
      const uniqueSuggestions = suggestions.filter((item, index, self) =>
        index === self.findIndex(t => t.value === item.value)
      ).sort((a, b) => a.value - b.value)

      // Return top 6 suggestions
      return uniqueSuggestions.slice(0, 6)
    }

    const formatCurrencyVietnamese = (amount) => {
      if (amount >= 1000000) {
        const millions = amount / 1000000
        if (millions === Math.floor(millions)) {
          return `${millions} triệu`
        } else {
          return `${millions.toFixed(1)} triệu`
        }
      } else if (amount >= 1000) {
        const thousands = amount / 1000
        if (thousands === Math.floor(thousands)) {
          return `${thousands} nghìn`
        } else {
          return `${thousands.toFixed(1)} nghìn`
        }
      } else {
        return formatCurrency(amount)
      }
    }

    // Invoice management
    const addNewInvoice = () => {
      if (invoices.value.length >= 20) {
        toastRef.value?.warning('Giới hạn', 'Chỉ tạo tối đa 20 hóa đơn mở cùng lúc')
        return
      }
      // Find the next available ID
      const existingIds = invoices.value.map(inv => inv.id)
      let newId = 1
      while (existingIds.includes(newId)) {
        newId++
      }

      const newInvoice = {
        id: newId,
        name: `Hóa đơn ${newId}`,
        cartItems: [],
        selectedCustomerId: '',
        selectedCustomer: null,
        selectedVoucher: null,
        subtotal: 0,
        discount: 0,
        total: 0,
        customerPaid: 0,
        change: 0,
        orderNote: '',
        paymentMethod: 'cash' // Default to cash
      }

      invoices.value.push(newInvoice)
      setActiveInvoice(newInvoice.id)
      saveInvoicesToLocalStorage()
    }

    const closeInvoice = (invoiceId) => {
      if (invoices.value.length <= 1) return

      const index = invoices.value.findIndex(inv => inv.id === invoiceId)
      if (index === -1) return

      // Release reserved IMEIs and stock for this invoice
      const invoice = invoices.value[index]
      if (invoice?.cartItems?.length) {
        invoice.cartItems.forEach(item => {
          if (item?.selectedImeis?.length) releaseReservedImeis(item.selectedImeis)
          if (item?.quantity) adjustStock(item.chiTietSanPhamId, +item.quantity)
        })
      }

      invoices.value.splice(index, 1)

      if (activeInvoiceId.value === invoiceId) {
        const newActiveIndex = Math.min(index, invoices.value.length - 1)
        activeInvoiceId.value = invoices.value[newActiveIndex].id
      }
      saveInvoicesToLocalStorage()
    }

    const setActiveInvoice = (invoiceId) => {
      activeInvoiceId.value = invoiceId
      saveInvoicesToLocalStorage()
    }

    // Sales methods
    const normalSale = async () => {
      if (currentInvoice.value.cartItems.length === 0) {
        toastRef.value?.error('Lỗi', 'Vui lòng thêm sản phẩm vào giỏ hàng')
        return
      }

      // Revalidate voucher trước khi thanh toán
      const ok = await revalidateCurrentVoucher(true)
      if (!ok) return

      // KHÔNG chặn thanh toán khi giá thay đổi - vẫn bán theo giá cũ (giaKhiThem)
      // Chỉ thông báo để người dùng biết
      const itemsWithPriceChanged = currentInvoice.value.cartItems.filter(item => item.priceChanged === true)
      if (itemsWithPriceChanged.length > 0) {
        const itemNames = itemsWithPriceChanged.slice(0, 3).map(item => item.tenSanPham).join(', ')
        toastRef.value?.info('Thông báo',
          `${itemsWithPriceChanged.length} sản phẩm có giá đã thay đổi. Đơn hàng sẽ bán theo giá cũ (giá tại thời điểm thêm vào). ${itemNames}${itemsWithPriceChanged.length > 3 ? '...' : ''}`)
      }

      // Check if private voucher is used but no customer selected
      if (currentInvoice.value.selectedVoucher &&
        currentInvoice.value.selectedVoucher.riengTu &&
        !currentInvoice.value.selectedCustomerId) {
        toastRef.value?.error('Lỗi', 'Vui lòng chọn khách hàng để sử dụng voucher riêng tư')
        return
      }

      loading.value = true

      try {
        // Ensure we have customer data for voucher usage
        let customerId = currentInvoice.value.selectedCustomerId
        if (!customerId && currentInvoice.value.selectedVoucher) {
          // For public vouchers, customerId can be null (walk-in customer)
          // For private vouchers, customerId is required (already validated above)
          console.log('ℹ️ Using voucher for walk-in customer (no customer ID required for public vouchers)')
        }

        const orderData = {
          hoaDon: {
            khachHangId: customerId || null,
            tongTien: currentInvoice.value.subtotal, // Sử dụng subtotal (tổng tiền hàng trước giảm giá)
            tongTienSauGiam: currentInvoice.value.total, // Sử dụng total (tổng tiền sau giảm giá)
            loaiHoaDon: 'NORMAL',
            ghiChu: currentInvoice.value.orderNote,
            phieuGiamGiaId: currentInvoice.value.selectedVoucher ? currentInvoice.value.selectedVoucher.id : null,
            tenKhachHang: currentInvoice.value.selectedCustomer ? currentInvoice.value.selectedCustomer.hoTen : 'Khách lẻ',
            soDienThoai: currentInvoice.value.selectedCustomer ? currentInvoice.value.selectedCustomer.soDienThoai : null,
            phuongThucThanhToan: currentInvoice.value.paymentMethod,
            // Combined payment details
            ...(currentInvoice.value.paymentMethod === 'combined' && {
              tienMat: currentInvoice.value.cashAmount,
              vnpay: currentInvoice.value.vnpayAmount
            })
          },
          chiTietHoaDon: currentInvoice.value.cartItems.map(item => {
            // Sử dụng giaKhiThem (giá tại thời điểm thêm vào) nếu có, nếu không thì dùng gia
            const price = item.giaKhiThem || item.gia || 0
            return {
              sanPhamId: item.chiTietSanPhamId,
              soLuong: item.quantity || 1,
              donGia: price,
              thanhTien: price * (item.quantity || 1),
              selectedImeis: item.selectedImeis || []
            }
          })
        }

        // If VNPay payment, create payment URL first
        if (currentInvoice.value.paymentMethod === 'vnpay') {
          try {
            const paymentData = {
              amount: currentInvoice.value.total,
              orderInfo: `Thanh toan don hang POS - ${currentInvoice.value.selectedCustomer?.hoTen || 'Khach le'}`,
              order: orderData
            }

            const paymentResponse = await paymentApi.createVnPayPaymentWithOrder(paymentData)

            if (paymentResponse.data && paymentResponse.data.paymentUrl) {
              // Xóa invoice ngay trước khi redirect (đã thanh toán) - không cần lưu lại
              const invoiceIdToRemove = currentInvoice.value.id

              // Release IMEIs và stock
              if (currentInvoice.value?.cartItems?.length) {
                currentInvoice.value.cartItems.forEach(item => {
                  if (item?.selectedImeis?.length) releaseReservedImeis(item.selectedImeis)
                  if (item?.quantity) adjustStock(item.chiTietSanPhamId, +item.quantity)
                })
              }

              // Xóa invoice khỏi danh sách
              const index = invoices.value.findIndex(inv => inv.id === invoiceIdToRemove)
              if (index > -1) {
                invoices.value.splice(index, 1)
              }

              // Chuyển sang invoice mới hoặc tạo invoice mới nếu không còn invoice nào
              if (invoices.value.length === 0) {
                addNewInvoice()
              } else {
                const newActiveIndex = Math.min(index, invoices.value.length - 1)
                activeInvoiceId.value = invoices.value[newActiveIndex].id
              }

              // Lưu vào localStorage (đã xóa invoice)
              saveInvoicesToLocalStorage()

              // Redirect to VNPay
              window.location.href = paymentResponse.data.paymentUrl
              return
            } else {
              throw new Error('Không thể tạo URL thanh toán VNPay')
            }
          } catch (paymentError) {
            console.error('VNPay payment error:', paymentError)
            toastRef.value?.error('Lỗi', 'Không thể tạo thanh toán VNPay: ' + (paymentError.response?.data?.message || paymentError.message))
            return
          }
        } else if (currentInvoice.value.paymentMethod === 'combined') {
          // Handle combined payment - use same logic as VNPay but with partial amount
          try {
            console.log('=== COMBINED PAYMENT DEBUG ===');
            console.log('Selected voucher:', currentInvoice.value.selectedVoucher);
            console.log('Selected customer ID:', currentInvoice.value.selectedCustomerId);
            console.log('Order data phieuGiamGiaId:', orderData.hoaDon.phieuGiamGiaId);

            // Create VNPay payment for the total amount (VNPay expects total invoice amount)
            const amount = currentInvoice.value.totalAfterDiscount || currentInvoice.value.total || 0

            const paymentData = {
              amount: amount,
              orderInfo: `Thanh toan don hang POS (Kết hợp) - ${currentInvoice.value.selectedCustomer?.hoTen || 'Khach le'}`,
              order: orderData
            }

            const paymentResponse = await paymentApi.createVnPayPaymentWithOrder(paymentData)

            if (paymentResponse.data && paymentResponse.data.paymentUrl) {
              // Xóa invoice ngay trước khi redirect (đã thanh toán) - không cần lưu lại
              const invoiceIdToRemove = currentInvoice.value.id

              // Release IMEIs và stock
              if (currentInvoice.value?.cartItems?.length) {
                currentInvoice.value.cartItems.forEach(item => {
                  if (item?.selectedImeis?.length) releaseReservedImeis(item.selectedImeis)
                  if (item?.quantity) adjustStock(item.chiTietSanPhamId, +item.quantity)
                })
              }

              // Xóa invoice khỏi danh sách
              const index = invoices.value.findIndex(inv => inv.id === invoiceIdToRemove)
              if (index > -1) {
                invoices.value.splice(index, 1)
              }

              // Chuyển sang invoice mới hoặc tạo invoice mới nếu không còn invoice nào
              if (invoices.value.length === 0) {
                addNewInvoice()
              } else {
                const newActiveIndex = Math.min(index, invoices.value.length - 1)
                activeInvoiceId.value = invoices.value[newActiveIndex].id
              }

              // Lưu vào localStorage (đã xóa invoice)
              saveInvoicesToLocalStorage()

              // Redirect to VNPay
              window.location.href = paymentResponse.data.paymentUrl
              return
            } else {
              throw new Error('Không thể tạo URL thanh toán VNPay')
            }
          } catch (paymentError) {
            console.error('Combined payment error:', paymentError)
            toastRef.value?.error('Lỗi', 'Không thể tạo thanh toán VNPay: ' + (paymentError.response?.data?.message || paymentError.message))
            return
          }
        }

        // For cash payment, proceed normally
        const response = await api.post('/api/hoa-don/pos-order', orderData)

        // Mark voucher as used if applicable
        if (currentInvoice.value.selectedVoucher && currentInvoice.value.selectedCustomerId) {
          await markVoucherAsUsed(currentInvoice.value.selectedVoucher, currentInvoice.value.selectedCustomerId)
        }

        // Tự động chuyển hướng sang trang hóa đơn chi tiết
        if (response.data && response.data.maHoaDon) {
          toastRef.value?.success('Thành công', 'Bán hàng thành công!')

          // Xóa invoice đã thanh toán ngay lập tức (không cần lưu lại)
          const invoiceIdToRemove = currentInvoice.value.id

          // Release IMEIs và stock
          if (currentInvoice.value?.cartItems?.length) {
            currentInvoice.value.cartItems.forEach(item => {
              if (item?.selectedImeis?.length) releaseReservedImeis(item.selectedImeis)
              if (item?.quantity) adjustStock(item.chiTietSanPhamId, +item.quantity)
            })
          }

          // Xóa invoice khỏi danh sách
          const index = invoices.value.findIndex(inv => inv.id === invoiceIdToRemove)
          if (index > -1) {
            invoices.value.splice(index, 1)
          }

          // Chuyển sang invoice mới hoặc tạo invoice mới nếu không còn invoice nào
          if (invoices.value.length === 0) {
            addNewInvoice()
          } else {
            const newActiveIndex = Math.min(index, invoices.value.length - 1)
            activeInvoiceId.value = invoices.value[newActiveIndex].id
          }

          // Lưu vào localStorage (đã xóa invoice)
          saveInvoicesToLocalStorage()

          // Chuyển hướng sang trang hóa đơn với mã hóa đơn vừa tạo
          setTimeout(() => {
            router.push({ path: '/hoa-don', query: { code: response.data.maHoaDon } })
          }, 1500)
        } else {
          toastRef.value?.success('Thành công', 'Bán hàng thành công!')

          // Xóa invoice đã thanh toán ngay lập tức
          const invoiceIdToRemove = currentInvoice.value.id

          // Release IMEIs và stock
          if (currentInvoice.value?.cartItems?.length) {
            currentInvoice.value.cartItems.forEach(item => {
              if (item?.selectedImeis?.length) releaseReservedImeis(item.selectedImeis)
              if (item?.quantity) adjustStock(item.chiTietSanPhamId, +item.quantity)
            })
          }

          // Xóa invoice khỏi danh sách
          const index = invoices.value.findIndex(inv => inv.id === invoiceIdToRemove)
          if (index > -1) {
            invoices.value.splice(index, 1)
          }

          // Chuyển sang invoice mới hoặc tạo invoice mới nếu không còn invoice nào
          if (invoices.value.length === 0) {
            addNewInvoice()
          } else {
            const newActiveIndex = Math.min(index, invoices.value.length - 1)
            activeInvoiceId.value = invoices.value[newActiveIndex].id
          }

          // Lưu vào localStorage (đã xóa invoice)
          saveInvoicesToLocalStorage()
        }

      } catch (error) {
        console.error('Error creating order:', error)
        toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi tạo hóa đơn')
      } finally {
        loading.value = false
      }
    }

    const resetDeliveryInfo = () => {
      deliveryInfo.value = {
        tenNguoiNhan: '',
        soDienThoai: '',
        soNhaDuong: '',
        idTinhThanhPho: '',
        tinhThanhPho: '',
        idPhuongXa: '',
        phuongXa: '',
        ghiChu: '',
        codEnabled: false,
        codFee: 0
      }
      phuongXaList.value = [] // Clear wards list
    }

    // Administrative division functions
    async function loadProvinces() {
      try {
        loadingProvinces.value = true
        console.log('Loading provinces...')
        const response = await api.get('/api/vietnam-administrative/provinces')
        console.log('Provinces response:', response.data)
        tinhThanhPhoList.value = response.data || []
        console.log('Provinces loaded:', tinhThanhPhoList.value.length, 'items')
      } catch (err) {
        console.error('Lỗi khi tải danh sách tỉnh/thành phố:', err)
        // Fallback data
        tinhThanhPhoList.value = [
          { code: '01', name: 'Hà Nội', type: 'Thành phố Trung ương' },
          { code: '79', name: 'TP. Hồ Chí Minh', type: 'Thành phố Trung ương' },
          { code: '31', name: 'Hải Phòng', type: 'Thành phố Trung ương' },
          { code: '48', name: 'Đà Nẵng', type: 'Thành phố Trung ương' },
          { code: '92', name: 'Cần Thơ', type: 'Thành phố Trung ương' }
        ]
        toastRef.value?.warning('Cảnh báo', 'Sử dụng dữ liệu dự phòng cho tỉnh/thành phố')
      } finally {
        loadingProvinces.value = false
      }
    }


    async function loadWards(provinceCode) {
      try {
        loadingWards.value = true
        console.log('Loading wards for province:', provinceCode)
        const response = await api.get(`/api/vietnam-administrative/wards/${provinceCode}`)
        console.log('Wards response:', response.data)
        phuongXaList.value = response.data || []
        console.log('Wards loaded:', phuongXaList.value.length, 'items')
      } catch (err) {
        console.error('Lỗi khi tải danh sách phường/xã:', err)
        phuongXaList.value = []
        toastRef.value?.error('Lỗi', 'Không thể tải danh sách phường/xã')
      } finally {
        loadingWards.value = false
      }
    }

    function calculateCodFee(provinceCode) {
      // COD fee calculation based on province
      const codFees = {
        '01': 30000, // Hà Nội
        '79': 30000, // TP.HCM
        '31': 25000, // Hải Phòng
        '48': 25000, // Đà Nẵng
        '92': 25000  // Cần Thơ
      }
      return codFees[provinceCode] || 35000 // Default for other provinces
    }

    async function onProvinceChange() {
      const provinceCode = deliveryInfo.value.idTinhThanhPho
      if (!provinceCode) {
        deliveryInfo.value.tinhThanhPho = ''
        deliveryInfo.value.idPhuongXa = ''
        deliveryInfo.value.phuongXa = ''
        phuongXaList.value = []
        deliveryInfo.value.codFee = 0
        return
      }

      // Find province name
      const province = tinhThanhPhoList.value.find(p => p.code === provinceCode)
      if (province) {
        deliveryInfo.value.tinhThanhPho = province.name
      }

      // Load wards
      await loadWards(provinceCode)

      // Calculate COD fee
      deliveryInfo.value.codFee = calculateCodFee(provinceCode)
    }


    function onWardChange() {
      const wardCode = deliveryInfo.value.idPhuongXa
      if (!wardCode) {
        deliveryInfo.value.phuongXa = ''
        return
      }

      // Find ward name
      const ward = phuongXaList.value.find(w => w.code === wardCode)
      if (ward) {
        deliveryInfo.value.phuongXa = ward.name
      }
    }

    const openDeliveryModal = async () => {
      resetDeliveryInfo()
      await loadProvinces() // Load provinces when opening modal
      showDeliveryModal.value = true
    }

    const switchToDeliveryMode = async () => {
      saleMode.value = 'delivery'
      resetDeliveryInfo()
      // Load provinces when switching to delivery mode
      if (tinhThanhPhoList.value.length === 0) {
        await loadProvinces()
      }
      // Auto-fill customer info if customer is selected
      if (currentInvoice.value.selectedCustomer) {
        deliveryInfo.value.tenNguoiNhan = currentInvoice.value.selectedCustomer.hoTen || deliveryInfo.value.tenNguoiNhan
        deliveryInfo.value.soDienThoai = currentInvoice.value.selectedCustomer.soDienThoai || deliveryInfo.value.soDienThoai
        // Load previous addresses for this customer
        await loadPreviousAddresses()
      }
    }

    const closeDeliveryModal = () => {
      showDeliveryModal.value = false
      resetDeliveryInfo()
    }

    const confirmDelivery = async () => {
      if (currentInvoice.value.cartItems.length === 0) {
        toastRef.value?.error('Lỗi', 'Vui lòng thêm sản phẩm vào giỏ hàng')
        return
      }

      // Revalidate voucher trước khi thanh toán
      const ok2 = await revalidateCurrentVoucher(true)
      if (!ok2) return

      // KHÔNG chặn thanh toán khi giá thay đổi - vẫn bán theo giá cũ (giaKhiThem)
      // Chỉ thông báo để người dùng biết
      const itemsWithPriceChanged = currentInvoice.value.cartItems.filter(item => item.priceChanged === true)
      if (itemsWithPriceChanged.length > 0) {
        const itemNames = itemsWithPriceChanged.slice(0, 3).map(item => item.tenSanPham).join(', ')
        toastRef.value?.info('Thông báo',
          `${itemsWithPriceChanged.length} sản phẩm có giá đã thay đổi. Đơn hàng sẽ bán theo giá cũ (giá tại thời điểm thêm vào). ${itemNames}${itemsWithPriceChanged.length > 3 ? '...' : ''}`)
      }

      // Validate các trường bắt buộc
      const requiredFields = {
        'Tên người nhận': deliveryInfo.value.tenNguoiNhan.trim(),
        'Số điện thoại': deliveryInfo.value.soDienThoai.trim(),
        'Địa chỉ chi tiết': deliveryInfo.value.soNhaDuong.trim(),
        'Tỉnh/Thành phố': deliveryInfo.value.idTinhThanhPho.trim(),
        'Phường/Xã': deliveryInfo.value.idPhuongXa.trim()
      }

      // Kiểm tra các trường bắt buộc
      for (const [field, value] of Object.entries(requiredFields)) {
        if (!value) {
          toastRef.value?.error('Lỗi', `Vui lòng nhập ${field.toLowerCase()}`)
          return
        }
      }

      // Tạo địa chỉ đầy đủ
      const fullAddress = `${deliveryInfo.value.soNhaDuong}, ${deliveryInfo.value.phuongXa}, ${deliveryInfo.value.quanHuyen}, ${deliveryInfo.value.tinhThanh}`.trim()

      loading.value = true

      try {
        const orderData = {
          hoaDon: {
            khachHangId: currentInvoice.value.selectedCustomerId || null,
            tongTien: currentInvoice.value.subtotal, // Sử dụng subtotal (tổng tiền hàng trước giảm giá)
            tongTienSauGiam: currentInvoice.value.total, // Sử dụng total (tổng tiền sau giảm giá)
            loaiHoaDon: 'DELIVERY',
            diaChi: fullAddress,
            tenKhachHang: deliveryInfo.value.tenNguoiNhan,
            soDienThoai: deliveryInfo.value.soDienThoai,
            ghiChu: deliveryInfo.value.ghiChu || currentInvoice.value.orderNote,
            phieuGiamGiaId: currentInvoice.value.selectedVoucher ? currentInvoice.value.selectedVoucher.id : null,
            phuongThucThanhToan: currentInvoice.value.paymentMethod,
            // Combined payment details
            ...(currentInvoice.value.paymentMethod === 'combined' && {
              tienMat: currentInvoice.value.cashAmount,
              vnpay: currentInvoice.value.vnpayAmount
            })
          },
          chiTietHoaDon: currentInvoice.value.cartItems.map(item => {
            // Sử dụng giaKhiThem (giá tại thời điểm thêm vào) nếu có, nếu không thì dùng gia
            const price = item.giaKhiThem || item.gia || 0
            return {
              sanPhamId: item.chiTietSanPhamId,
              soLuong: item.quantity || 1,
              donGia: price,
              thanhTien: price * (item.quantity || 1),
              selectedImeis: item.selectedImeis || []
            }
          })
        }

        // If VNPay payment, create payment URL first
        if (currentInvoice.value.paymentMethod === 'vnpay') {
          try {
            const paymentData = {
              amount: currentInvoice.value.total,
              orderInfo: `Thanh toan don hang giao hang - ${currentInvoice.value.selectedCustomer?.hoTen || 'Khach le'}`,
              order: orderData
            }

            const paymentResponse = await paymentApi.createVnPayPaymentWithOrder(paymentData)

            if (paymentResponse.data && paymentResponse.data.paymentUrl) {
              // Xóa invoice ngay trước khi redirect (đã thanh toán) - không cần lưu lại
              const invoiceIdToRemove = currentInvoice.value.id

              // Release IMEIs và stock
              if (currentInvoice.value?.cartItems?.length) {
                currentInvoice.value.cartItems.forEach(item => {
                  if (item?.selectedImeis?.length) releaseReservedImeis(item.selectedImeis)
                  if (item?.quantity) adjustStock(item.chiTietSanPhamId, +item.quantity)
                })
              }

              // Xóa invoice khỏi danh sách
              const index = invoices.value.findIndex(inv => inv.id === invoiceIdToRemove)
              if (index > -1) {
                invoices.value.splice(index, 1)
              }

              // Chuyển sang invoice mới hoặc tạo invoice mới nếu không còn invoice nào
              if (invoices.value.length === 0) {
                addNewInvoice()
              } else {
                const newActiveIndex = Math.min(index, invoices.value.length - 1)
                activeInvoiceId.value = invoices.value[newActiveIndex].id
              }

              // Lưu vào localStorage (đã xóa invoice)
              saveInvoicesToLocalStorage()

              // Redirect to VNPay
              window.location.href = paymentResponse.data.paymentUrl
              return
            } else {
              throw new Error('Không thể tạo URL thanh toán VNPay')
            }
          } catch (paymentError) {
            console.error('VNPay payment error:', paymentError)
            toastRef.value?.error('Lỗi', 'Không thể tạo thanh toán VNPay: ' + (paymentError.response?.data?.message || paymentError.message))
            return
          }
        } else if (currentInvoice.value.paymentMethod === 'combined') {
          // Handle combined payment for delivery - use same logic as VNPay but with partial amount
          try {
            // Create VNPay payment for the VNPay portion only
            const paymentData = {
              amount: currentInvoice.value.vnpayAmount,
              orderInfo: `Thanh toan don hang giao hang (Kết hợp) - ${currentInvoice.value.selectedCustomer?.hoTen || 'Khach le'}`,
              order: orderData
            }

            const paymentResponse = await paymentApi.createVnPayPaymentWithOrder(paymentData)

            if (paymentResponse.data && paymentResponse.data.paymentUrl) {
              // Xóa invoice ngay trước khi redirect (đã thanh toán) - không cần lưu lại
              const invoiceIdToRemove = currentInvoice.value.id

              // Release IMEIs và stock
              if (currentInvoice.value?.cartItems?.length) {
                currentInvoice.value.cartItems.forEach(item => {
                  if (item?.selectedImeis?.length) releaseReservedImeis(item.selectedImeis)
                  if (item?.quantity) adjustStock(item.chiTietSanPhamId, +item.quantity)
                })
              }

              // Xóa invoice khỏi danh sách
              const index = invoices.value.findIndex(inv => inv.id === invoiceIdToRemove)
              if (index > -1) {
                invoices.value.splice(index, 1)
              }

              // Chuyển sang invoice mới hoặc tạo invoice mới nếu không còn invoice nào
              if (invoices.value.length === 0) {
                addNewInvoice()
              } else {
                const newActiveIndex = Math.min(index, invoices.value.length - 1)
                activeInvoiceId.value = invoices.value[newActiveIndex].id
              }

              // Lưu vào localStorage (đã xóa invoice)
              saveInvoicesToLocalStorage()

              // Redirect to VNPay
              window.location.href = paymentResponse.data.paymentUrl
              return
            } else {
              throw new Error('Không thể tạo URL thanh toán VNPay')
            }
          } catch (paymentError) {
            console.error('Combined payment error:', paymentError)
            toastRef.value?.error('Lỗi', 'Không thể tạo thanh toán VNPay: ' + (paymentError.response?.data?.message || paymentError.message))
            return
          }
        }

        // For cash payment, proceed normally
        const response = await api.post('/api/hoa-don/pos-order', orderData)

        // Mark voucher as used if applicable
        if (currentInvoice.value.selectedVoucher && currentInvoice.value.selectedCustomerId) {
          await markVoucherAsUsed(currentInvoice.value.selectedVoucher, currentInvoice.value.selectedCustomerId)
        }

        // Tự động chuyển hướng sang trang hóa đơn chi tiết
        if (response.data && response.data.maHoaDon) {
          toastRef.value?.success('Thành công', 'Đặt hàng giao hàng thành công!')

          // Xóa invoice đã thanh toán ngay lập tức (không cần lưu lại)
          const invoiceIdToRemove = currentInvoice.value.id

          // Release IMEIs và stock
          if (currentInvoice.value?.cartItems?.length) {
            currentInvoice.value.cartItems.forEach(item => {
              if (item?.selectedImeis?.length) releaseReservedImeis(item.selectedImeis)
              if (item?.quantity) adjustStock(item.chiTietSanPhamId, +item.quantity)
            })
          }

          // Xóa invoice khỏi danh sách
          const index = invoices.value.findIndex(inv => inv.id === invoiceIdToRemove)
          if (index > -1) {
            invoices.value.splice(index, 1)
          }

          // Chuyển sang invoice mới hoặc tạo invoice mới nếu không còn invoice nào
          if (invoices.value.length === 0) {
            addNewInvoice()
          } else {
            const newActiveIndex = Math.min(index, invoices.value.length - 1)
            activeInvoiceId.value = invoices.value[newActiveIndex].id
          }

          // Lưu vào localStorage (đã xóa invoice)
          saveInvoicesToLocalStorage()

          // Chuyển hướng sang trang hóa đơn với mã hóa đơn vừa tạo
          setTimeout(() => {
            router.push({ path: '/hoa-don', query: { code: response.data.maHoaDon } })
          }, 1500)
        } else {
          toastRef.value?.success('Thành công', 'Đặt hàng giao hàng thành công!')

          // Xóa invoice đã thanh toán ngay lập tức
          const invoiceIdToRemove = currentInvoice.value.id

          // Release IMEIs và stock
          if (currentInvoice.value?.cartItems?.length) {
            currentInvoice.value.cartItems.forEach(item => {
              if (item?.selectedImeis?.length) releaseReservedImeis(item.selectedImeis)
              if (item?.quantity) adjustStock(item.chiTietSanPhamId, +item.quantity)
            })
          }

          // Xóa invoice khỏi danh sách
          const index = invoices.value.findIndex(inv => inv.id === invoiceIdToRemove)
          if (index > -1) {
            invoices.value.splice(index, 1)
          }

          // Chuyển sang invoice mới hoặc tạo invoice mới nếu không còn invoice nào
          if (invoices.value.length === 0) {
            addNewInvoice()
          } else {
            const newActiveIndex = Math.min(index, invoices.value.length - 1)
            activeInvoiceId.value = invoices.value[newActiveIndex].id
          }

          // Lưu vào localStorage (đã xóa invoice)
          saveInvoicesToLocalStorage()

          showDeliveryModal.value = false
          // Reset form delivery
          deliveryInfo.value = {
            tenNguoiNhan: '',
            soDienThoai: '',
            soNhaDuong: '',
            phuongXa: '',
            quanHuyen: '',
            tinhThanh: '',
            ghiChu: ''
          }
        }

      } catch (error) {
        console.error('Error creating delivery order:', error)
        toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi tạo đơn giao hàng')
      } finally {
        loading.value = false
      }
    }

    const clearCart = () => {
      currentInvoice.value.cartItems = []
      currentInvoice.value.selectedCustomer = null
      currentInvoice.value.selectedCustomerId = ''
      currentInvoice.value.selectedVoucher = null
      currentInvoice.value.subtotal = 0
      currentInvoice.value.discount = 0
      currentInvoice.value.total = 0
      currentInvoice.value.customerPaid = 0
      currentInvoice.value.change = 0
      currentInvoice.value.orderNote = ''
      currentInvoice.value.paymentMethod = 'cash' // Reset to cash
      saveInvoicesToLocalStorage()
    }

    // Lưu invoices vào localStorage
    const saveInvoicesToLocalStorage = () => {
      try {
        const dataToSave = {
          invoices: invoices.value,
          activeInvoiceId: activeInvoiceId.value,
          reservedImeis: Array.from(reservedImeis.value),
          saleMode: saleMode.value,
          deliveryInfo: deliveryInfo.value,
          timestamp: Date.now()
        }
        localStorage.setItem('pos_invoices', JSON.stringify(dataToSave))
      } catch (error) {
        console.error('Error saving invoices to localStorage:', error)
      }
    }

    // Kiểm tra xem đã qua ngày mới chưa
    const isNewDay = (oldTimestamp, newTimestamp) => {
      if (!oldTimestamp || !newTimestamp) return true

      const oldDate = new Date(oldTimestamp)
      const newDate = new Date(newTimestamp)

      // So sánh ngày (không tính giờ)
      return oldDate.getDate() !== newDate.getDate() ||
        oldDate.getMonth() !== newDate.getMonth() ||
        oldDate.getFullYear() !== newDate.getFullYear()
    }

    // Khôi phục invoices từ localStorage
    const loadInvoicesFromLocalStorage = () => {
      try {
        const saved = localStorage.getItem('pos_invoices')
        if (saved) {
          const data = JSON.parse(saved)

          // Kiểm tra xem đã qua ngày mới chưa
          if (data.timestamp && isNewDay(data.timestamp, Date.now())) {
            console.log('New day detected, clearing old invoices with old prices...')
            // Xóa toàn bộ invoices có sản phẩm với giá cũ
            localStorage.removeItem('pos_invoices')
            toastRef.value?.info('Thông báo', 'Đã qua ngày mới. Tất cả hóa đơn chờ đã được xóa.')
            return false
          }

          // Kiểm tra timestamp - nếu quá cũ (ví dụ: > 24 giờ) thì không khôi phục
          const maxAge = 24 * 60 * 60 * 1000 // 24 giờ
          if (data.timestamp && (Date.now() - data.timestamp) > maxAge) {
            console.log('Saved invoices are too old, clearing...')
            localStorage.removeItem('pos_invoices')
            return false
          }

          if (data.invoices && Array.isArray(data.invoices) && data.invoices.length > 0) {
            invoices.value = data.invoices
            if (data.activeInvoiceId) {
              activeInvoiceId.value = data.activeInvoiceId
            }
            if (data.reservedImeis && Array.isArray(data.reservedImeis)) {
              reservedImeis.value = new Set(data.reservedImeis)
            }
            if (data.saleMode) {
              saleMode.value = data.saleMode
            }
            if (data.deliveryInfo) {
              deliveryInfo.value = { ...deliveryInfo.value, ...data.deliveryInfo }
            }

            // Tính lại totals cho tất cả invoices
            invoices.value.forEach(inv => {
              calculateTotalsForInvoice(inv)
            })

            // Kiểm tra và thông báo về giá đã thay đổi
            const priceChangedItems = []
            invoices.value.forEach(invoice => {
              if (invoice.cartItems) {
                invoice.cartItems.forEach(item => {
                  if (item.priceChanged) {
                    if (!priceChangedItems.find(p => p.chiTietSanPhamId === item.chiTietSanPhamId)) {
                      priceChangedItems.push({
                        chiTietSanPhamId: item.chiTietSanPhamId,
                        tenSanPham: item.tenSanPham
                      })
                    }
                  }
                })
              }
            })

            if (priceChangedItems.length > 0) {
              const itemNames = priceChangedItems.slice(0, 3).map(p => p.tenSanPham).join(', ')
              toastRef.value?.warning('Thông báo',
                `Giá của ${priceChangedItems.length} sản phẩm đã thay đổi. Các sản phẩm trong hóa đơn chờ vẫn bán theo giá cũ. ${itemNames}${priceChangedItems.length > 3 ? '...' : ''}`)
            }

            return true
          }
        }
      } catch (error) {
        console.error('Error loading invoices from localStorage:', error)
      }
      return false
    }

    // Tính lại totals cho một invoice cụ thể
    const calculateTotalsForInvoice = (invoice) => {
      if (!invoice || !invoice.cartItems) return

      let subtotal = 0
      invoice.cartItems.forEach(item => {
        // Sử dụng giaKhiThem (giá tại thời điểm thêm vào) nếu có, nếu không thì dùng gia
        const price = Number(item.giaKhiThem || item.gia || 0)
        const qty = item.quantity || 1
        subtotal += price * qty
      })

      invoice.subtotal = subtotal

      // Tính discount từ voucher
      let discount = 0
      if (invoice.selectedVoucher) {
        const voucher = invoice.selectedVoucher
        if (voucher.loaiPhieuGiamGia === 'PERCENT') {
          discount = Math.min(subtotal * (voucher.giaTri / 100), voucher.giaTriToiDa || subtotal)
        } else {
          discount = Math.min(voucher.giaTri, subtotal)
        }
      }

      invoice.discount = discount
      invoice.total = subtotal - discount
    }



    // Watch invoices để tự động lưu khi có thay đổi
    watch([invoices, activeInvoiceId, reservedImeis, saleMode], () => {
      saveInvoicesToLocalStorage()
    }, { deep: true })

    // Kiểm tra qua ngày mới mỗi phút và clear invoices nếu cần
    let newDayCheckInterval = null
    if (typeof window !== 'undefined') {
      newDayCheckInterval = setInterval(() => {
        const saved = localStorage.getItem('pos_invoices')
        if (saved) {
          try {
            const data = JSON.parse(saved)
            if (data.timestamp && isNewDay(data.timestamp, Date.now())) {
              console.log('New day detected, clearing old invoices...')
              localStorage.removeItem('pos_invoices')
              // Clear invoices trong memory
              invoices.value = [{
                id: 1,
                name: 'Hóa đơn 1',
                cartItems: [],
                selectedCustomerId: '',
                selectedCustomer: null,
                selectedVoucher: null,
                subtotal: 0,
                discount: 0,
                total: 0,
                customerPaid: 0,
                change: 0,
                orderNote: '',
                paymentMethod: 'cash',
                cashAmount: 0,
                vnpayAmount: 0
              }]
              activeInvoiceId.value = 1
              reservedImeis.value = new Set()
              toastRef.value?.info('Thông báo', 'Đã qua ngày mới. Tất cả hóa đơn chờ đã được xóa.')
            }
          } catch (error) {
            console.error('Error checking new day:', error)
          }
        }
      }, 60000) // Check every minute
    }

    // Lifecycle
    onMounted(async () => {
      updateTime()
      setInterval(updateTime, 1000)

      // Kiểm tra và xóa invoice đã thanh toán (từ sessionStorage - khi quay lại từ VNPay)
      // Lưu ý: Invoice đã được xóa trước khi redirect, nhưng vẫn kiểm tra để đảm bảo an toàn
      const invoiceIdToRemove = sessionStorage.getItem('pos_invoice_to_remove')
      if (invoiceIdToRemove) {
        const invoiceId = parseInt(invoiceIdToRemove)
        const invoiceIndex = invoices.value.findIndex(inv => inv.id === invoiceId)
        if (invoiceIndex > -1) {
          // Invoice vẫn còn trong danh sách (trường hợp hiếm), xóa nó
          const invoice = invoices.value[invoiceIndex]
          if (invoice?.cartItems?.length) {
            invoice.cartItems.forEach(item => {
              if (item?.selectedImeis?.length) releaseReservedImeis(item.selectedImeis)
              if (item?.quantity) adjustStock(item.chiTietSanPhamId, +item.quantity)
            })
          }
          invoices.value.splice(invoiceIndex, 1)
          if (activeInvoiceId.value === invoiceId) {
            if (invoices.value.length === 0) {
              addNewInvoice()
            } else {
              const newActiveIndex = Math.min(invoiceIndex, invoices.value.length - 1)
              activeInvoiceId.value = invoices.value[newActiveIndex]?.id || 1
            }
          }
          saveInvoicesToLocalStorage()
        }
        sessionStorage.removeItem('pos_invoice_to_remove')
      }

      // Khôi phục invoices từ localStorage trước
      const restored = loadInvoicesFromLocalStorage()
      if (restored) {
        toastRef.value?.success('Đã khôi phục', 'Đã khôi phục hóa đơn chờ từ phiên làm việc trước')
      }

      await Promise.all([
        loadProducts(),
        loadCustomers(),
        loadVouchers(),
        loadProvinces()
      ])

      // Poll vouchers and products periodically to reflect real-time changes
      const lastSuggestedVoucherId = ref(null)
      setInterval(async () => {
        try {
          // Reload vouchers
          const cid = currentInvoice.value.selectedCustomerId || null
          if (cid) {
            await loadCustomerVouchers(cid)
          } else {
            await loadVouchers()
            loadAvailableVouchers(null)
          }

          // Revalidate current voucher
          await revalidateCurrentVoucher()

          // Reload products list to reflect changes (new products, price changes, status changes)
          await loadProducts()

          // Suggest best voucher if none selected and a better one appears
          const bv = bestVoucher.value
          if (!currentInvoice.value.selectedVoucher && bv && lastSuggestedVoucherId.value !== bv.id) {
            lastSuggestedVoucherId.value = bv.id
            toastRef.value?.success('Có voucher phù hợp', `Gợi ý: ${bv.tenPhieuGiamGia}. Mở danh sách để áp dụng.`)
          }
          if (typeof window !== 'undefined') {
            window.__POS_VOUCHERS__ = vouchers
            window.__POS__ = { currentInvoice }
          }
        } catch {}
      }, 10000) // Poll every 10 seconds
    })

    // Lưu khi chuyển trang hoặc đóng tab
    onBeforeUnmount(() => {
      saveInvoicesToLocalStorage()
      // Clear interval khi unmount
      if (newDayCheckInterval) {
        clearInterval(newDayCheckInterval)
      }
    })

    // Lưu khi người dùng rời khỏi trang
    if (typeof window !== 'undefined') {
      window.addEventListener('beforeunload', () => {
        saveInvoicesToLocalStorage()
      })
    }

    return {
      // Reactive data
      searchQuery,
      customerSearch,
      currentTime,
      currentDate,
      loading,
      toastRef,
      products,
      customers,
      invoices,
      activeInvoiceId,
      showDeliveryModal,
      deliveryInfo,
      showProductModal,
      productSearchQuery,
      showCustomerSuggestions,
      filteredCustomers,
      showProductSuggestions,
      showVoucherModal,
      vouchers,
      availableVouchers,
      voucherTab,
      manualVoucherCode,
      manualVoucherLoading,
      manualVoucherResult,
      showImeiModal,
      selectedProduct,
      availableImeis,
      imeiSearchQuery,
      selectedImeis,
      quantityToAdd,
      filteredImeis,
      showQuickCustomerModal,
      quickCustomerForm,

      // Computed
      currentInvoice,
      filteredProducts,
      modalFilteredProducts,
      currentInvoiceIndex,
      visibleInvoices,
      bestVoucher,
      formattedCashAmount,
      formattedVnpayAmount,
      isPaymentAmountValid,

      // Methods
      searchProducts,
      searchCustomers,
      addToCart,
      increaseQuantity,
      decreaseQuantity,
      removeFromCart,
      updateQuantity,
      setQuickAmount,
      addToCartFromModal,
      previousInvoice,
      nextInvoice,
      getActualIndex,
      draggedIndex,
      onDragStart,
      onDragOver,
      onDrop,
      selectCustomer,
      clearSelectedCustomer,
      removeCustomer,
      hideCustomerSuggestions,
      hideProductSuggestions,
      addToCartFromSearch,
      loadVouchers,
      loadCustomerVouchers,
      loadAvailableVouchers,
      selectVoucher,
      removeVoucher,
      markVoucherAsUsed,
      applyBestVoucher,
      calculateVoucherDiscount,
      calculateVoucherDiscountAmount,
      applyManualVoucher,
      selectManualVoucher,
      clearManualVoucher,
      openVoucherModal,
      handleCashAmountInput,
      handleVnpayAmountInput,
      resetCombinedPaymentFields,
      calculateChange,
      handleCustomerPaidInput,
      formattedCustomerPaid,
      formatCurrency,
      getSuggestedAmounts,
      formatCurrencyVietnamese,
      loadAvailableImeis,
      loadAvailableImeisForExistingProduct,
      selectImei,
      confirmImeiSelection,
      closeImeiModal,
      addToCartWithQuantity,
      openQuickCustomerModal,
      createQuickCustomer,
      closeQuickCustomerModal,
      addNewInvoice,
      closeInvoice,
      setActiveInvoice,
      normalSale,
      confirmDelivery,
      openDeliveryModal,
      switchToDeliveryMode,
      saleMode,
      closeDeliveryModal,
      resetDeliveryInfo,
      loadProvinces,
      loadWards,
      calculateCodFee,
      onProvinceChange,
      onWardChange,
      tinhThanhPhoList,
      phuongXaList,
      loadingProvinces,
      loadingWards,

      // Previous addresses
      previousAddresses,
      selectedPreviousAddressId,
      applyPreviousAddress,

      // Voucher helpers
      isVoucherApplicableLocal
    }
  }
}
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hide-scroll {
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
}
.hide-scroll::-webkit-scrollbar {
  width: 0;
  height: 0;
}
</style>
