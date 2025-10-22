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
                    <div class="text-lg font-bold text-black">{{ formatCurrency(product.gia) }}</div>
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
      <main class="flex-1 bg-white overflow-y-auto">
        <!-- Product List -->
        <div class="p-4">
          <!-- Product Items -->
          <div class="space-y-3">
            <div
              v-for="item in currentInvoice.cartItems"
              :key="item.chiTietSanPhamId"
              class="flex items-center justify-between p-4 bg-slate-50 rounded-lg border border-slate-200"
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
                </h4>
                <p class="text-xs text-slate-500">{{ item.tenHang || 'N/A' }} • {{ item.tenRam || 'N/A' }} • {{ item.tenRom || 'N/A' }} • {{ item.tenMauSac || 'N/A' }}</p>
                <p class="text-xs text-slate-500">Tồn: {{ item.soLuongTon }} | KH đặt: 0</p>
              </div>

              <!-- Price -->
              <div class="text-right mr-4">
                <div class="text-lg font-bold text-black">{{ formatCurrency(item.gia) }}</div>
              </div>

              <!-- Quantity Controls -->
              <div class="flex items-center space-x-2">
                <button
                  @click="decreaseQuantity(item.chiTietSanPhamId)"
                  class="w-8 h-8 flex items-center justify-center bg-slate-200 hover:bg-slate-300 rounded-full text-slate-600 transition-colors"
                >
                  -
                </button>
                <div class="w-12 text-center">
                  <input
                    type="number"
                    :value="item.quantity"
                    @input="updateQuantity(item.chiTietSanPhamId, $event.target.value)"
                    class="w-full text-center border border-slate-300 rounded px-1 py-1 text-sm"
                    min="1"
                    :max="item.soLuongTon"
                  />
                </div>
                <button
                  @click="increaseQuantity(item.chiTietSanPhamId)"
                  class="w-8 h-8 flex items-center justify-center bg-slate-200 hover:bg-slate-300 rounded-full text-slate-600 transition-colors"
                >
                  +
                </button>
              </div>

              <!-- Remove Button -->
              <button
                @click="removeFromCart(item.chiTietSanPhamId)"
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

        <!-- Note Input - Moved to bottom -->
        <div class="mt-auto p-4 border-t border-slate-200 bg-slate-50">
          <div class="flex items-center space-x-2">
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
        </div>
      </main>

      <!-- Payment Section (Right) -->
      <aside class="w-96 bg-white shadow-lg flex flex-col border-l border-slate-200 h-full overflow-hidden">
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
        <div class="flex-1 p-4 space-y-4 overflow-y-auto">
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

          <!-- Payment Method Selection -->
          <div class="space-y-3">
            <div class="flex items-center space-x-4">
              <label class="flex items-center space-x-2">
                <input type="radio" name="paymentMethod" value="cash" v-model="paymentMethod" class="text-orange-600">
                <span class="text-sm">Tiền mặt</span>
              </label>
              <label class="flex items-center space-x-2">
                <input type="radio" name="paymentMethod" value="zalopay" v-model="paymentMethod" class="text-orange-600">
                <span class="text-sm">ZaloPay</span>
              </label>
              <label class="flex items-center space-x-2">
                <input type="radio" name="paymentMethod" value="vnpay" v-model="paymentMethod" class="text-orange-600">
                <span class="text-sm">Ví VNPay</span>
              </label>
            </div>

            <!-- ZaloPay Info -->
            <div v-if="paymentMethod === 'zalopay'" class="mt-2">
              <div class="text-xs text-slate-500 mb-2">Thanh toán qua ZaloPay - Quét QR hoặc mở app ZaloPay</div>
              <div class="bg-blue-50 border border-blue-200 rounded p-3">
                <div class="flex items-center space-x-2">
                  <div class="w-6 h-6 bg-blue-500 rounded flex items-center justify-center">
                    <span class="text-white text-xs font-bold">Z</span>
                  </div>
                  <span class="text-sm font-medium text-blue-800">ZaloPay</span>
                </div>
                <div class="text-xs text-blue-600 mt-1">Thanh toán nhanh chóng và an toàn</div>
              </div>
            </div>

            <!-- VNPay selected: no extra bank list, pay directly via VNPay -->
          </div>

          <!-- Voucher Section -->
          <div class="space-y-3">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-slate-700">Voucher giảm giá</span>
              <button
                @click="showVoucherModal = true"
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

          <!-- Quick Payment Amounts -->
          <div class="grid grid-cols-3 gap-2">
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

        <!-- Payment Footer -->
        <div class="p-4 border-t border-slate-200 bg-slate-50 flex-shrink-0">
          <!-- Main Payment Button -->
          <button
            @click="handlePayment"
            :disabled="currentInvoice.cartItems.length === 0"
            class="w-full py-4 bg-orange-400 hover:bg-orange-600 disabled:bg-slate-300 disabled:cursor-not-allowed text-white rounded-lg font-bold text-lg transition-colors"
          >
            THANH TOÁN
          </button>

          <!-- Secondary Buttons -->
          <div class="flex space-x-2 mt-3">
            <button
              @click="normalSale"
              :disabled="currentInvoice.cartItems.length === 0"
              class="flex-1 flex items-center justify-center space-x-2 px-4 py-2 bg-green-600 hover:bg-green-700 disabled:bg-slate-300 disabled:cursor-not-allowed text-white rounded-lg font-medium transition-colors"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
              </svg>
              <span>Bán nhanh</span>
            </button>

            <button
              @click="showDeliveryModal = true"
              :disabled="currentInvoice.cartItems.length === 0"
              class="flex-1 flex items-center justify-center space-x-2 px-4 py-2 bg-orange-400 hover:bg-orange-600 disabled:bg-slate-300 disabled:cursor-not-allowed text-white rounded-lg font-medium transition-colors"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4"></path>
              </svg>
              <span>Giao hàng</span>
            </button>
          </div>

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
    </div>

    <!-- Delivery Modal -->
    <div v-if="showDeliveryModal" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg p-6 w-96 max-w-full mx-4">
        <h3 class="text-lg font-semibold mb-4">Thông tin giao hàng</h3>
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Địa chỉ giao hàng</label>
            <textarea
              v-model="deliveryAddress"
              placeholder="Nhập địa chỉ giao hàng"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
              rows="3"
            ></textarea>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Ghi chú</label>
            <textarea
              v-model="deliveryNote"
              placeholder="Ghi chú thêm (tùy chọn)"
              class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-transparent"
              rows="2"
            ></textarea>
          </div>
        </div>
        <div class="flex space-x-3 mt-6">
          <button
            @click="showDeliveryModal = false"
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
        </div>

        <!-- Modal Content -->
        <div class="flex-1 overflow-y-auto p-6">
          <div class="space-y-4">
            <div
              v-for="voucher in availableVouchers"
              :key="voucher.id"
              @click="selectVoucher(voucher)"
              class="p-4 border border-slate-200 rounded-lg hover:border-blue-300 hover:shadow-md cursor-pointer transition-all"
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
                  <div class="text-lg font-bold text-green-600">
                    -{{ calculateVoucherDiscount(voucher) }}
                  </div>
                  <div class="text-xs text-slate-500">
                    Còn {{ voucher.soLuongDung }} lượt
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Empty State -->
          <div v-if="availableVouchers.length === 0" class="flex flex-col items-center justify-center h-64 text-slate-500">
            <svg class="w-16 h-16 mb-4 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"></path>
            </svg>
            <p class="text-lg font-medium">Không có voucher khả dụng</p>
            <p class="text-sm">Không có voucher nào phù hợp với đơn hàng này</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Toast Component -->
    <Toast ref="toastRef" />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import Toast from '../components/Toast.vue'
import SimpleUserMenu from '../components/SimpleUserMenu.vue'
import api from '../services/api'
import { paymentApi } from '../services/api'

export default {
  name: 'PosPage',
  components: {
    Toast,
    SimpleUserMenu
  },
  setup() {
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
        orderNote: ''
      }
    ])
    const activeInvoiceId = ref(1)

    // Delivery modal
    const showDeliveryModal = ref(false)
    const deliveryAddress = ref('')
    const deliveryNote = ref('')

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

    // IMEI Modal
    const showImeiModal = ref(false)
    const selectedProduct = ref(null)
    const availableImeis = ref([])
    const imeiSearchQuery = ref('')
    const selectedImeis = ref([])
    const quantityToAdd = ref(1)

    // Payment method & VNPay
    const paymentMethod = ref('cash')
    const transferBanks = ref([])
    const transferBankCode = ref('')
    const vnpayBankCode = ref('')
    const vnpayBanks = ref([]) // not used in UI anymore but kept for future extension

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

    const loadProducts = async () => {
      try {
        console.log('Loading products from API...')

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
      // Check if product already exists in cart
      const existingItem = currentInvoice.value.cartItems.find(item => item.chiTietSanPhamId === product.chiTietSanPhamId)

      if (existingItem) {
        // If product exists, open IMEI modal for additional quantity
        selectedProduct.value = product
        quantityToAdd.value = 1
        selectedImeis.value = []
        loadAvailableImeisForExistingProduct(product.chiTietSanPhamId)
        showImeiModal.value = true
      } else {
        // If new product, open IMEI modal normally
        selectedProduct.value = product
        quantityToAdd.value = 1
        selectedImeis.value = []
        loadAvailableImeis(product.chiTietSanPhamId)
        showImeiModal.value = true
      }
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
        availableImeis.value = (response.data || []).filter(imei => imei.trangThai === 1)
      } catch (error) {
        console.error('Error loading IMEIs:', error)
        availableImeis.value = []
        toastRef.value?.error('Lỗi', 'Không thể tải danh sách IMEI')
      }
    }

    const loadAvailableImeisForExistingProduct = async (chiTietSanPhamId) => {
      try {
        // Load available IMEIs for the specific product variant
        const response = await api.get(`/api/imei/chi-tiet/${chiTietSanPhamId}`)
        // Filter only available IMEIs (trangThai = 1)
        let allImeis = (response.data || []).filter(imei => imei.trangThai === 1)

        // Get already used IMEIs for this product in current cart
        const existingItem = currentInvoice.value.cartItems.find(item => item.chiTietSanPhamId === chiTietSanPhamId)
        const usedImeis = existingItem ? (existingItem.selectedImeis || []) : []

        // Filter out already used IMEIs
        availableImeis.value = allImeis.filter(imei => !usedImeis.includes(imei.imei))
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

    const confirmImeiSelection = () => {
      if (selectedImeis.value.length !== quantityToAdd.value || !selectedProduct.value) {
        toastRef.value?.error('Lỗi', `Vui lòng chọn đúng ${quantityToAdd.value} IMEI`)
        return
      }

      // Check if enough IMEIs are available
      if (selectedImeis.value.length > availableImeis.value.length) {
        toastRef.value?.error('Lỗi', 'Không đủ IMEI khả dụng')
        return
      }

      // Check if product already exists in cart
      const existingItemIndex = currentInvoice.value.cartItems.findIndex(item => item.chiTietSanPhamId === selectedProduct.value.chiTietSanPhamId)

      if (existingItemIndex > -1) {
        // If product exists, add IMEIs to existing item
        const existingItem = currentInvoice.value.cartItems[existingItemIndex]
        const newImeis = selectedImeis.value.map(imei => imei.imei)

        // Add new IMEIs to existing ones
        existingItem.selectedImeis = [...(existingItem.selectedImeis || []), ...newImeis]
        existingItem.quantity += quantityToAdd.value

        calculateTotals()
        toastRef.value?.success('Thành công', `Đã thêm ${quantityToAdd.value} IMEI cho ${selectedProduct.value.tenSanPham}`)
      } else {
        // If new product, add new item to cart
        currentInvoice.value.cartItems.push({
          chiTietSanPhamId: selectedProduct.value.chiTietSanPhamId,
          tenSanPham: selectedProduct.value.tenSanPham,
          gia: selectedProduct.value.gia,
          quantity: quantityToAdd.value,
          soLuongTon: selectedProduct.value.soLuongTon,
          tenHang: selectedProduct.value.tenHang,
          tenRam: selectedProduct.value.tenRam,
          tenRom: selectedProduct.value.tenRom,
          tenMauSac: selectedProduct.value.tenMauSac,
          selectedImeis: selectedImeis.value.map(imei => imei.imei)
        })

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

    const increaseQuantity = (chiTietSanPhamId) => {
      const item = currentInvoice.value.cartItems.find(item => item.chiTietSanPhamId === chiTietSanPhamId)
      if (item && item.quantity < item.soLuongTon) {
        // Open IMEI selection modal for additional quantity
        const product = {
          chiTietSanPhamId: item.chiTietSanPhamId,
          tenSanPham: item.tenSanPham,
          gia: item.gia,
          soLuongTon: item.soLuongTon,
          tenHang: item.tenHang,
          tenRam: item.tenRam,
          tenRom: item.tenRom,
          tenMauSac: item.tenMauSac
        }

        selectedProduct.value = product
        quantityToAdd.value = 1
        selectedImeis.value = []
        loadAvailableImeisForExistingProduct(chiTietSanPhamId)
        showImeiModal.value = true
      } else {
        toastRef.value?.error('Lỗi', 'Không đủ hàng trong kho')
      }
    }

    const decreaseQuantity = (chiTietSanPhamId) => {
      const item = currentInvoice.value.cartItems.find(item => item.chiTietSanPhamId === chiTietSanPhamId)
      if (item && item.quantity > 1) {
        item.quantity -= 1

        // Remove one IMEI from the list (the last one)
        if (item.selectedImeis && item.selectedImeis.length > 0) {
          item.selectedImeis.pop()
        }

        calculateTotals()
      }
    }

    const removeFromCart = (chiTietSanPhamId) => {
      const index = currentInvoice.value.cartItems.findIndex(item => item.chiTietSanPhamId === chiTietSanPhamId)
      if (index > -1) {
        currentInvoice.value.cartItems.splice(index, 1)
        calculateTotals()
      }
    }

    const updateQuantity = (chiTietSanPhamId, newQuantity) => {
      const quantity = parseInt(newQuantity) || 1
      const item = currentInvoice.value.cartItems.find(item => item.chiTietSanPhamId === chiTietSanPhamId)
      if (item) {
        if (quantity > item.soLuongTon) {
          toastRef.value?.error('Lỗi', 'Không đủ hàng trong kho')
          return
        }
        if (quantity <= 0) {
          removeFromCart(chiTietSanPhamId)
        } else {
          const currentQuantity = item.quantity
          const difference = quantity - currentQuantity

          if (difference > 0) {
            // If increasing quantity, open IMEI modal for additional quantity
            const product = {
              chiTietSanPhamId: item.chiTietSanPhamId,
              tenSanPham: item.tenSanPham,
              gia: item.gia,
              soLuongTon: item.soLuongTon,
              tenHang: item.tenHang,
              tenRam: item.tenRam,
              tenRom: item.tenRom,
              tenMauSac: item.tenMauSac
            }

            selectedProduct.value = product
            quantityToAdd.value = difference
            selectedImeis.value = []
            loadAvailableImeisForExistingProduct(chiTietSanPhamId)
            showImeiModal.value = true
          } else if (difference < 0) {
            // If decreasing quantity, remove IMEIs and update quantity
            const removeCount = Math.abs(difference)
            if (item.selectedImeis && item.selectedImeis.length > 0) {
              // Remove the last N IMEIs
              for (let i = 0; i < removeCount && item.selectedImeis.length > 0; i++) {
                item.selectedImeis.pop()
              }
            }
            item.quantity = quantity
            calculateTotals()
          }
        }
      }
    }

    const calculateTotals = () => {
      currentInvoice.value.subtotal = currentInvoice.value.cartItems.reduce((sum, item) =>
        sum + (item.gia * item.quantity), 0
      )

      // Calculate voucher discount
      let voucherDiscount = 0
      if (currentInvoice.value.selectedVoucher) {
        voucherDiscount = calculateVoucherDiscountAmount(currentInvoice.value.selectedVoucher)
      }

      currentInvoice.value.discount = voucherDiscount
      currentInvoice.value.total = currentInvoice.value.subtotal - currentInvoice.value.discount

      // Update available vouchers when subtotal changes
      loadAvailableVouchers()
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

    const addToCartFromSearch = (product) => {
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
        const [publicResponse, privateResponse] = await Promise.all([
          api.get('/api/phieu-giam-gia'), // Public vouchers
          api.get(`/api/phieu-giam-gia/customer/${customerId}`).catch(() => ({ data: [] })) // Private vouchers
        ])

        const publicVouchers = publicResponse.data.filter(voucher => !voucher.riengTu)
        const privateVouchers = privateResponse.data || []

        // Combine all vouchers
        const allVouchers = [...publicVouchers, ...privateVouchers]

        // Remove duplicates based on ID
        const uniqueVouchers = allVouchers.filter((voucher, index, self) =>
          index === self.findIndex(v => v.id === voucher.id)
        )

        vouchers.value = uniqueVouchers
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
        orderNote: ''
      }

      invoices.value.push(newInvoice)
      setActiveInvoice(newInvoice.id)
    }

    const closeInvoice = (invoiceId) => {
      if (invoices.value.length <= 1) return

      const index = invoices.value.findIndex(inv => inv.id === invoiceId)
      if (index === -1) return

      invoices.value.splice(index, 1)

      if (activeInvoiceId.value === invoiceId) {
        const newActiveIndex = Math.min(index, invoices.value.length - 1)
        activeInvoiceId.value = invoices.value[newActiveIndex].id
      }
    }

    const setActiveInvoice = (invoiceId) => {
      activeInvoiceId.value = invoiceId
    }

    // Sales methods
    const normalSale = async () => {
      if (currentInvoice.value.cartItems.length === 0) {
        toastRef.value?.error('Lỗi', 'Vui lòng thêm sản phẩm vào giỏ hàng')
        return
      }

      loading.value = true

      try {
        const orderData = {
          hoaDon: {
            khachHangId: currentInvoice.value.selectedCustomerId || null,
            tongTien: currentInvoice.value.total,
            tongTienSauGiam: currentInvoice.value.total - currentInvoice.value.discount,
            loaiHoaDon: 'NORMAL',
            ghiChu: currentInvoice.value.orderNote,
            phieuGiamGiaId: currentInvoice.value.selectedVoucher ? currentInvoice.value.selectedVoucher.id : null,
            tenKhachHang: currentInvoice.value.selectedCustomer ? currentInvoice.value.selectedCustomer.hoTen : 'Khách lẻ',
            soDienThoai: currentInvoice.value.selectedCustomer ? currentInvoice.value.selectedCustomer.soDienThoai : null
          },
          chiTietHoaDon: currentInvoice.value.cartItems.map(item => ({
            sanPhamId: item.chiTietSanPhamId,
            soLuong: item.quantity,
            donGia: item.gia,
            thanhTien: item.gia * item.quantity,
            selectedImeis: item.selectedImeis || []
          }))
        }

        const response = await api.post('/api/hoa-don/pos-order', orderData)

        // Mark voucher as used if applicable
        if (currentInvoice.value.selectedVoucher && currentInvoice.value.selectedCustomerId) {
          await markVoucherAsUsed(currentInvoice.value.selectedVoucher, currentInvoice.value.selectedCustomerId)
        }

        // Tự động chuyển hướng sang trang hóa đơn chi tiết
        if (response.data && response.data.maHoaDon) {
          toastRef.value?.success('Thành công', 'Bán hàng thành công!')

          // Chuyển hướng sang trang hóa đơn với mã hóa đơn vừa tạo
          setTimeout(() => {
            window.location.href = `/hoa-don?track=${response.data.maHoaDon}`
          }, 1500)
        } else {
          toastRef.value?.success('Thành công', 'Bán hàng thành công!')
          clearCart()
        }

      } catch (error) {
        console.error('Error creating order:', error)
        toastRef.value?.error('Lỗi', 'Có lỗi xảy ra khi tạo hóa đơn')
      } finally {
        loading.value = false
      }
    }

    // Unified payment handler
    const handlePayment = async () => {
      // ZaloPay payment
      if (paymentMethod.value === 'zalopay') {
        if (currentInvoice.value.total <= 0) {
          toastRef.value?.error('Lỗi', 'Tổng tiền phải lớn hơn 0 để thanh toán ZaloPay')
          return
        }
        try {
          // Prepare order data for ZaloPay
          const orderData = {
            hoaDon: {
              khachHangId: currentInvoice.value.selectedCustomerId ? parseInt(currentInvoice.value.selectedCustomerId) : null,
              tongTien: currentInvoice.value.subtotal,
              tongTienSauGiam: currentInvoice.value.total,
              loaiHoaDon: 'NORMAL',
              ghiChu: currentInvoice.value.orderNote || '',
              tenKhachHang: currentInvoice.value.selectedCustomer ? currentInvoice.value.selectedCustomer.hoTen : 'Khách lẻ',
              soDienThoai: currentInvoice.value.selectedCustomer ? currentInvoice.value.selectedCustomer.soDienThoai : null,
              diaChi: null,
              phieuGiamGiaId: currentInvoice.value.selectedVoucher ? parseInt(currentInvoice.value.selectedVoucher.id) : null
            },
            chiTietHoaDon: currentInvoice.value.cartItems.map(item => ({
              sanPhamId: item.chiTietSanPhamId,
              soLuong: item.quantity,
              donGia: item.gia,
              thanhTien: item.gia * item.quantity,
              selectedImeis: item.selectedImeis || []
            }))
          }

          console.log('Sending ZaloPay request:', {
            amount: Math.max(0, Math.round(currentInvoice.value.total)),
            orderInfo: `POS-${Date.now()}`,
            order: orderData
          })

          const res = await paymentApi.createZaloPayPaymentWithOrder({
            amount: Math.max(0, Math.round(currentInvoice.value.total)),
            orderInfo: `POS-${Date.now()}`,
            order: orderData
          })
          
          console.log('ZaloPay response:', res.data)
          const url = res.data?.paymentUrl
          if (url) {
            window.location.href = url
            return
          } else {
            toastRef.value?.error('Lỗi', 'Không nhận được URL thanh toán từ server')
          }
        } catch (e) {
          console.error('ZaloPay create payment failed', e)
          console.error('Error details:', e.response?.data)
          toastRef.value?.error('Lỗi', 'Không tạo được thanh toán ZaloPay: ' + (e.response?.data?.message || e.message))
        }
      }

      // VNPay payment
      if (paymentMethod.value === 'vnpay') {
        if (currentInvoice.value.total <= 0) {
          toastRef.value?.error('Lỗi', 'Tổng tiền phải lớn hơn 0 để thanh toán VNPay')
          return
        }
        try {
          // Prepare order data for VNPay
          const orderData = {
            hoaDon: {
              khachHangId: currentInvoice.value.selectedCustomerId ? parseInt(currentInvoice.value.selectedCustomerId) : null,
              tongTien: currentInvoice.value.subtotal, // Gửi tổng tiền gốc trước giảm giá
              tongTienSauGiam: currentInvoice.value.total, // Gửi tổng tiền sau giảm giá
              loaiHoaDon: 'NORMAL',
              ghiChu: currentInvoice.value.orderNote || '',
              tenKhachHang: currentInvoice.value.selectedCustomer ? currentInvoice.value.selectedCustomer.hoTen : 'Khách lẻ',
              soDienThoai: currentInvoice.value.selectedCustomer ? currentInvoice.value.selectedCustomer.soDienThoai : null,
              diaChi: null,
              phieuGiamGiaId: currentInvoice.value.selectedVoucher ? parseInt(currentInvoice.value.selectedVoucher.id) : null
            },
            chiTietHoaDon: currentInvoice.value.cartItems.map(item => ({
              sanPhamId: item.chiTietSanPhamId,
              soLuong: item.quantity,
              donGia: item.gia,
              thanhTien: item.gia * item.quantity,
              selectedImeis: item.selectedImeis || []
            }))
          }

          console.log('Sending VNPay request:', {
            amount: Math.max(0, Math.round(currentInvoice.value.total)),
            orderInfo: `POS-${Date.now()}`,
            bankCode: vnpayBankCode.value || undefined,
            order: orderData
          })
          
          console.log('Invoice details:', {
            subtotal: currentInvoice.value.subtotal,
            discount: currentInvoice.value.discount,
            total: currentInvoice.value.total,
            selectedVoucher: currentInvoice.value.selectedVoucher
          })

          const res = await paymentApi.createVnPayPaymentWithOrder({
            amount: Math.max(0, Math.round(currentInvoice.value.total)), // Số tiền thực tế phải trả (sau giảm giá)
            orderInfo: `POS-${Date.now()}`,
            bankCode: vnpayBankCode.value || undefined,
            order: orderData
          })
          
          console.log('VNPay response:', res.data)
          const url = res.data?.paymentUrl
          if (url) {
            window.location.href = url
            return
          } else {
            toastRef.value?.error('Lỗi', 'Không nhận được URL thanh toán từ server')
          }
        } catch (e) {
          console.error('VNPay create payment failed', e)
          console.error('Error details:', e.response?.data)
          toastRef.value?.error('Lỗi', 'Không tạo được thanh toán VNPay: ' + (e.response?.data?.message || e.message))
        }
      }

      // Default: normal internal sale flow
      await normalSale()
    }

    const confirmDelivery = async () => {
      if (!deliveryAddress.value.trim()) {
        toastRef.value?.error('Lỗi', 'Vui lòng nhập địa chỉ giao hàng')
        return
      }

      loading.value = true

      try {
        const orderData = {
          hoaDon: {
            khachHangId: currentInvoice.value.selectedCustomerId || null,
            tongTien: currentInvoice.value.total,
            tongTienSauGiam: currentInvoice.value.total - currentInvoice.value.discount,
            loaiHoaDon: 'DELIVERY',
            diaChi: deliveryAddress.value,
            ghiChu: deliveryNote.value || currentInvoice.value.orderNote,
            phieuGiamGiaId: currentInvoice.value.selectedVoucher ? currentInvoice.value.selectedVoucher.id : null,
            tenKhachHang: currentInvoice.value.selectedCustomer ? currentInvoice.value.selectedCustomer.hoTen : 'Khách lẻ',
            soDienThoai: currentInvoice.value.selectedCustomer ? currentInvoice.value.selectedCustomer.soDienThoai : null
          },
          chiTietHoaDon: currentInvoice.value.cartItems.map(item => ({
            sanPhamId: item.chiTietSanPhamId,
            soLuong: item.quantity,
            donGia: item.gia,
            thanhTien: item.gia * item.quantity,
            selectedImeis: item.selectedImeis || []
          }))
        }

        const response = await api.post('/api/hoa-don/pos-order', orderData)

        // Mark voucher as used if applicable
        if (currentInvoice.value.selectedVoucher && currentInvoice.value.selectedCustomerId) {
          await markVoucherAsUsed(currentInvoice.value.selectedVoucher, currentInvoice.value.selectedCustomerId)
        }

        // Tự động chuyển hướng sang trang hóa đơn chi tiết
        if (response.data && response.data.maHoaDon) {
          toastRef.value?.success('Thành công', 'Đặt hàng giao hàng thành công!')

          // Chuyển hướng sang trang hóa đơn với mã hóa đơn vừa tạo
          setTimeout(() => {
            window.location.href = `/hoa-don?track=${response.data.maHoaDon}`
          }, 1500)
        } else {
          toastRef.value?.success('Thành công', 'Đặt hàng giao hàng thành công!')
          clearCart()
          showDeliveryModal.value = false
          deliveryAddress.value = ''
          deliveryNote.value = ''
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
    }

    // Lifecycle
    onMounted(async () => {
      updateTime()
      setInterval(updateTime, 1000)

      await Promise.all([
        loadProducts(),
        loadCustomers(),
        loadVouchers()
      ])

      // Load transfer banks for selection
      try {
        const res = await paymentApi.getBanks()
        transferBanks.value = res.data || []
      } catch (e) {
        transferBanks.value = []
      }
    })

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
      deliveryAddress,
      deliveryNote,
      showProductModal,
      productSearchQuery,
      showCustomerSuggestions,
      filteredCustomers,
      showProductSuggestions,
      showVoucherModal,
      vouchers,
      availableVouchers,
      showImeiModal,
      selectedProduct,
      availableImeis,
      imeiSearchQuery,
      selectedImeis,
      quantityToAdd,
      transferBanks,
      transferBankCode,
      paymentMethod,
      vnpayBanks,
      vnpayBankCode,
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
      handlePayment,
      confirmDelivery
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
</style>
