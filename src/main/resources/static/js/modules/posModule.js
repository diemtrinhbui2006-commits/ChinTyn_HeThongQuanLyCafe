import { tableApi } from '../api/tableApi.js';
import { productApi } from '../api/productApi.js';
import { categoryApi } from '../api/categoryApi.js';
import { invoiceApi } from '../api/invoiceApi.js';
import { formatVND } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';
import { getProductImage } from '../utils/imageMatcher.js';
import { openThermalReceipt } from './receiptModal.js';

// State nội bộ của POS
export const posState = {
  tables: [],
  products: [],
  categories: [],
  selectedTable: null,
  activeInvoice: null,
  tableFilter: 'ALL',
  categoryFilter: 'ALL',
  searchKeyword: '',
};

export async function initPosModule() {
  bindPosEvents();
  await loadPosData();
}

export async function loadPosData() {
  try {
    const [tables, products, categories] = await Promise.all([
      tableApi.getAll(),
      productApi.getAll(),
      categoryApi.getAll(),
    ]);

    posState.tables = tables || [];
    posState.products = products || [];
    posState.categories = categories || [];

    renderTables();
    renderCategories();
    renderProducts();

    if (posState.selectedTable) {
      const refreshed = posState.tables.find(t => t.id === posState.selectedTable.id);
      if (refreshed) selectTable(refreshed);
    }
  } catch (err) {
    showToast('Lỗi tải dữ liệu POS: ' + err.message, 'error');
  }
}

function bindPosEvents() {
  // Tìm kiếm món ăn
  const searchInput = document.getElementById('productSearchInput');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      posState.searchKeyword = e.target.value.toLowerCase().trim();
      renderProducts();
    });
  }

  // Lọc bàn (Tất cả, Trống, Đang dùng)
  document.querySelectorAll('.filter-pill').forEach(pill => {
    pill.addEventListener('click', () => {
      document.querySelectorAll('.filter-pill').forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      posState.tableFilter = pill.dataset.filter;
      renderTables();
    });
  });

  // Tính tiền thừa khi khách đưa tiền
  const cashInput = document.getElementById('cashReceivedInput');
  if (cashInput) cashInput.addEventListener('input', calculateChange);

  // Áp dụng chiết khấu
  const discountBtn = document.getElementById('btnApplyDiscount');
  if (discountBtn) discountBtn.addEventListener('click', applyDiscount);

  // Nút thanh toán
  const payBtn = document.getElementById('btnPayNow');
  if (payBtn) payBtn.addEventListener('click', handleCheckout);

  // Nút hủy bàn
  const cancelBtn = document.getElementById('btnCancelTable');
  if (cancelBtn) cancelBtn.addEventListener('click', handleCancelTable);
}

// 1. Render Sơ Đồ Bàn
export function renderTables() {
  const grid = document.getElementById('tablesGrid');
  if (!grid) return;
  grid.innerHTML = '';

  const filtered = posState.tables.filter(t => {
    if (posState.tableFilter === 'AVAILABLE') return t.status === 'AVAILABLE';
    if (posState.tableFilter === 'OCCUPIED') return t.status === 'OCCUPIED';
    return true;
  });

  if (filtered.length === 0) {
    grid.innerHTML = '<div style="grid-column: span 2; text-align: center; color: #94a3b8; padding: 20px;">Không có bàn nào</div>';
    return;
  }

  filtered.forEach(t => {
    const isOccupied = t.status === 'OCCUPIED';
    const isSelected = posState.selectedTable?.id === t.id;

    const box = document.createElement('div');
    box.className = `table-box ${isOccupied ? 'occupied' : 'available'} ${isSelected ? 'selected' : ''}`;
    box.innerHTML = `
      <div class="table-title">${t.name || 'Bàn #' + t.id}</div>
      <div class="table-badge">${isOccupied ? '🟠 Đang dùng' : '🟢 Bàn trống'}</div>
    `;

    box.addEventListener('click', () => selectTable(t));
    grid.appendChild(box);
  });
}

export async function selectTable(table) {
  posState.selectedTable = table;
  renderTables();

  const titleEl = document.getElementById('posActiveTableTitle');
  if (titleEl) titleEl.textContent = `Hóa Đơn - ${table.name || 'Bàn ' + table.id}`;

  try {
    const inv = await invoiceApi.getOpenByTable(table.id);
    posState.activeInvoice = inv;
    renderInvoiceCart();
  } catch (e) {
    posState.activeInvoice = null;
    renderInvoiceCart();
  }
}

// 2. Render Danh mục món
function renderCategories() {
  const container = document.getElementById('categoriesBar');
  if (!container) return;
  container.innerHTML = '<span class="cat-chip active" data-id="ALL">Tất cả</span>';

  posState.categories.forEach(cat => {
    const chip = document.createElement('span');
    chip.className = 'cat-chip';
    chip.dataset.id = cat.id;
    chip.textContent = cat.name;
    chip.addEventListener('click', () => {
      document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      posState.categoryFilter = cat.id;
      renderProducts();
    });
    container.appendChild(chip);
  });

  const allChip = container.querySelector('[data-id="ALL"]');
  if (allChip) {
    allChip.addEventListener('click', (e) => {
      document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('active'));
      e.target.classList.add('active');
      posState.categoryFilter = 'ALL';
      renderProducts();
    });
  }
}

// 3. Render Thực đơn món ăn
export function renderProducts() {
  const grid = document.getElementById('productsGrid');
  if (!grid) return;
  grid.innerHTML = '';

  const filtered = posState.products.filter(p => {
    const matchCat = posState.categoryFilter === 'ALL' || String(p.categoryId) === String(posState.categoryFilter);
    const matchSearch = !posState.searchKeyword || (p.name && p.name.toLowerCase().includes(posState.searchKeyword));
    return matchCat && matchSearch;
  });

  if (filtered.length === 0) {
    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: #94a3b8; padding: 40px;">Không tìm thấy món phù hợp</div>';
    return;
  }

  filtered.forEach(p => {
    const isAvail = p.available !== false && (p.quantity == null || p.quantity > 0);
    const imgUrl = getProductImage(p.name);

    const card = document.createElement('div');
    card.className = `product-card ${isAvail ? '' : 'disabled'}`;
    card.innerHTML = `
      <div class="product-thumb">
        ${imgUrl ? `<img src="${imgUrl}" alt="${p.name}">` : '<span style="font-size:2.5rem;">☕</span>'}
        <span class="badge-stock">${isAvail ? `Còn ${p.quantity ?? 99}` : 'Hết hàng'}</span>
      </div>
      <div class="product-card-body">
        <div class="product-name">${p.name}</div>
        <div class="product-price">${formatVND(p.price)}</div>
      </div>
    `;

    if (isAvail) {
      card.addEventListener('click', () => addFood(p));
    }
    grid.appendChild(card);
  });
}

// 4. Thêm món vào bàn
async function addFood(product) {
  if (!posState.selectedTable) {
    showToast('Vui lòng chọn một bàn trước khi gọi món!', 'warning');
    return;
  }

  try {
    const payload = {
      tableId: posState.selectedTable.id,
      productId: product.id,
      quantity: 1,
    };
    const updated = await invoiceApi.orderFood(payload);
    posState.activeInvoice = updated;
    showToast(`Đã thêm "${product.name}" vào ${posState.selectedTable.name}`, 'success');

    await loadPosData();
    renderInvoiceCart();
  } catch (err) {
    showToast('Lỗi khi thêm món: ' + err.message, 'error');
  }
}

// 5. Render Giỏ hàng bàn đang chọn
export function renderInvoiceCart() {
  const container = document.getElementById('cartItemsBox');
  if (!container) return;
  container.innerHTML = '';

  const subEl = document.getElementById('cartSubTotal');
  const disEl = document.getElementById('cartDiscount');
  const totalEl = document.getElementById('cartFinalTotal');

  if (!posState.selectedTable || !posState.activeInvoice || !posState.activeInvoice.details || posState.activeInvoice.details.length === 0) {
    container.innerHTML = '<div class="empty-cart-msg">Bàn chưa có món nào.<br>Chọn món bên thực đơn để gọi!</div>';
    if (subEl) subEl.textContent = '0 ₫';
    if (disEl) disEl.textContent = '0 ₫';
    if (totalEl) totalEl.textContent = '0 ₫';
    calculateChange();
    return;
  }

  posState.activeInvoice.details.forEach(d => {
    const item = document.createElement('div');
    item.className = 'cart-item';
    item.innerHTML = `
      <div style="flex:1;">
        <div class="cart-item-name">${d.productName || 'Món #' + d.productId}</div>
        <div class="cart-item-price">${formatVND(d.price)}</div>
      </div>
      <div class="cart-qty-ctrl">
        <button class="cart-btn-qty btn-minus">-</button>
        <span style="font-weight:700; width:18px; text-align:center;">${d.quantity}</span>
        <button class="cart-btn-qty btn-plus">+</button>
      </div>
      <div style="font-weight:700; width:70px; text-align:right;">
        ${formatVND(d.totalPrice || (d.price * d.quantity))}
      </div>
      <button class="cart-btn-remove" title="Xóa món">×</button>
    `;

    item.querySelector('.btn-plus').addEventListener('click', (e) => {
      e.stopPropagation();
      addFood({ id: d.productId, name: d.productName });
    });

    item.querySelector('.btn-minus').addEventListener('click', async (e) => {
      e.stopPropagation();
      await removeFood(d.productId, 1);
    });

    item.querySelector('.cart-btn-remove').addEventListener('click', async (e) => {
      e.stopPropagation();
      await removeFood(d.productId, d.quantity);
    });

    container.appendChild(item);
  });

  const sub = posState.activeInvoice.totalAmount || 0;
  const dis = posState.activeInvoice.discountAmount || 0;
  const fin = posState.activeInvoice.finalAmount != null ? posState.activeInvoice.finalAmount : (sub - dis);

  if (subEl) subEl.textContent = formatVND(sub);
  if (disEl) disEl.textContent = '- ' + formatVND(dis);
  if (totalEl) totalEl.textContent = formatVND(fin);

  calculateChange();
}

async function removeFood(productId, qty) {
  if (!posState.activeInvoice) return;
  try {
    const updated = await invoiceApi.removeFood(posState.activeInvoice.id, productId, qty);
    posState.activeInvoice = updated;
    await loadPosData();
    renderInvoiceCart();
    showToast('Đã cập nhật món trong bàn', 'success');
  } catch (err) {
    showToast('Lỗi xóa món: ' + err.message, 'error');
  }
}

// 6. Áp dụng giảm giá
async function applyDiscount() {
  if (!posState.activeInvoice) {
    showToast('Chưa có hóa đơn để giảm giá', 'warning');
    return;
  }
  const input = document.getElementById('discountPercentInput');
  const pct = parseFloat(input.value);
  if (isNaN(pct) || pct < 0 || pct > 100) {
    showToast('Vui lòng nhập phần trăm giảm giá hợp lệ (0-100%)', 'error');
    return;
  }

  try {
    const updated = await invoiceApi.applyDiscount(posState.activeInvoice.id, { discountPercent: pct });
    posState.activeInvoice = updated;
    renderInvoiceCart();
    showToast(`Đã áp dụng giảm giá ${pct}%`, 'success');
  } catch (err) {
    showToast('Lỗi giảm giá: ' + err.message, 'error');
  }
}

// 7. Tính tiền thối
function calculateChange() {
  const cashInput = document.getElementById('cashReceivedInput');
  const changeEl = document.getElementById('changeReturnText');
  if (!cashInput || !changeEl) return;

  const finalAmount = posState.activeInvoice?.finalAmount ?? (posState.activeInvoice?.totalAmount ?? 0);
  const cash = parseFloat(cashInput.value) || 0;

  if (cash === 0 || finalAmount === 0) {
    changeEl.textContent = '0 ₫';
    changeEl.style.color = 'var(--success)';
    return;
  }

  const change = cash - finalAmount;
  if (change < 0) {
    changeEl.textContent = `Thiếu ${formatVND(Math.abs(change))}`;
    changeEl.style.color = 'var(--danger)';
  } else {
    changeEl.textContent = formatVND(change);
    changeEl.style.color = 'var(--success)';
  }
}

// 8. Chốt thanh toán
async function handleCheckout() {
  if (!posState.selectedTable || !posState.activeInvoice || !posState.activeInvoice.details || posState.activeInvoice.details.length === 0) {
    showToast('Không có món nào để thanh toán!', 'warning');
    return;
  }

  const ok = confirm(`Xác nhận thanh toán cho ${posState.selectedTable.name}?\nTổng tiền: ${formatVND(posState.activeInvoice.finalAmount || posState.activeInvoice.totalAmount)}`);
  if (!ok) return;

  try {
    const paid = await invoiceApi.checkout(posState.activeInvoice.id, { paymentMethod: 'CASH' });
    showToast(`Thanh toán thành công cho ${posState.selectedTable.name}!`, 'success');
    openThermalReceipt(paid, posState.selectedTable.name);

    posState.activeInvoice = null;
    await loadPosData();
    renderInvoiceCart();
  } catch (err) {
    showToast('Lỗi thanh toán: ' + err.message, 'error');
  }
}

// 9. Hủy bàn
async function handleCancelTable() {
  if (!posState.activeInvoice) return;
  const ok = confirm(`Hủy đơn hàng của ${posState.selectedTable?.name}? Món sẽ được hoàn lại kho.`);
  if (!ok) return;

  try {
    await invoiceApi.cancel(posState.activeInvoice.id);
    showToast('Đã hủy hóa đơn thành công', 'success');
    posState.activeInvoice = null;
    await loadPosData();
    renderInvoiceCart();
  } catch (err) {
    showToast('Lỗi hủy hóa đơn: ' + err.message, 'error');
  }
}
