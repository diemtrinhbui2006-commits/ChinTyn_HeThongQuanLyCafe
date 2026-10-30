import { tableApi } from '../api/tableApi.js';
import { productApi } from '../api/productApi.js';
import { categoryApi } from '../api/categoryApi.js';
import { invoiceApi } from '../api/invoiceApi.js';
import { getCurrentUser } from './authModule.js';
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
  productPage: 1,
  productPageSize: 8,
};

let cashDueTotal = null;
let eventsBound = false;
let pendingProduct = null;

export function showTableSelection() {
  document.querySelector('.pos-container').classList.remove('ordering');
}

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

    posState.tables = (tables || []).map(t => ({
      ...t,
      displayName: t.name || ('Bàn ' + (t.tableNumber ?? t.id)),
      name: t.name || `Bàn ${t.tableNumber || t.id}`,
    }));
    posState.products = products || [];
    posState.categories = categories || [];

    renderTables();
    renderCategories();
    renderProducts();

    if (posState.selectedTable) {
      const refreshed = posState.tables.find(t => t.id === posState.selectedTable.id);
      if (refreshed) {
        posState.selectedTable = refreshed;
      }
    }
  } catch (err) {
    showToast('Lỗi tải dữ liệu POS: ' + err.message, 'error');
  }
}

function bindPosEvents() {
  if (eventsBound) return;
  eventsBound = true;
  const dialog = document.getElementById('productOptionsDialog');
  ['btnCloseProductOptions', 'btnCancelProductOptions'].forEach(id => document.getElementById(id).addEventListener('click', () => dialog.close()));
  document.getElementById('productOptionsForm').addEventListener('submit', async event => {
    event.preventDefault();
    if (!pendingProduct) return;
    const quantity = Number(document.getElementById('optionQuantity').value);
    if (!Number.isInteger(quantity) || quantity < 1 || quantity > pendingProduct.quantity) {
      document.getElementById('productOptionsError').textContent = 'Số lượng không hợp lệ hoặc vượt tồn kho.';
      return;
    }
    const button = document.getElementById('btnCompleteProductOptions');
    if (button.disabled) return;
    button.disabled = true;
    try {
      const success = await addFood(pendingProduct, {
        quantity,
        sugarPercent: Number(document.querySelector('[name="sugar"]:checked').value),
        iceOption: document.querySelector('[name="ice"]:checked').value,
        note: document.getElementById('optionNote').value.trim(),
      });
      if (success) dialog.close();
    } finally { button.disabled = false; }
  });
  document.getElementById('btnChooseAnotherTable').addEventListener('click', showTableSelection);
  // Tìm kiếm món ăn
  const searchInput = document.getElementById('productSearchInput');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      posState.searchKeyword = e.target.value.toLowerCase().trim();
      posState.productPage = 1;
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

  // Trừ mệnh giá khỏi số tiền còn phải trả
  const cashInput = document.getElementById('cashRemainingInput');
  if (cashInput) cashInput.addEventListener('input', calculateChange);

  document.querySelectorAll('[data-cash-deduct]').forEach(button => {
    button.addEventListener('click', () => {
      if (!cashInput) return;
      const currentDue = Number(cashInput.value) || 0;
      const denomination = Number(button.dataset.cashDeduct);
      cashInput.value = String(currentDue - denomination);
      calculateChange();
    });
  });

  const exactCashButton = document.getElementById('btnCashExact');
  if (exactCashButton) {
    exactCashButton.addEventListener('click', () => {
      if (!cashInput) return;
      cashInput.value = '0';
      calculateChange();
    });
  }

  const clearCashButton = document.getElementById('btnClearCash');
  if (clearCashButton) {
    clearCashButton.addEventListener('click', () => {
      if (!cashInput) return;
      cashInput.value = String(posState.activeInvoice?.totalAmount || 0);
      calculateChange();
    });
  }

  // Áp dụng chiết khấu
  const discountBtn = document.getElementById('btnApplyDiscount');
  if (discountBtn) discountBtn.addEventListener('click', applyDiscount);

  // Nút thanh toán
  const payBtn = document.getElementById('btnPayNow');
  if (payBtn) payBtn.addEventListener('click', handleCheckout);

  // Nút in tạm tính
  const printTempBtn = document.getElementById('btnPrintTempReceipt');
  if (printTempBtn) {
    printTempBtn.addEventListener('click', () => {
      const inv = posState.activeInvoice;
      if (!inv || !inv.details || inv.details.length === 0) {
        showToast('Bàn này chưa gọi món nào để in hóa đơn!', 'warning');
        return;
      }
      const tableName = posState.selectedTable?.displayName || posState.selectedTable?.name || ('Bàn #' + (posState.selectedTable?.tableNumber ?? inv.tableId));
      openThermalReceipt(inv, tableName);
    });
  }

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
    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: #94a3b8; padding: 20px;">Không có bàn nào</div>';
    return;
  }

  filtered.forEach(table => {
    const isOccupied = table.status === 'OCCUPIED';
    const isSelected = posState.selectedTable && posState.selectedTable.id === table.id;
    const tableName = table.displayName || table.name || ('Bàn ' + (table.tableNumber ?? table.id));

    const box = document.createElement('button');
    box.type = 'button';
    box.className = `table-box ${isOccupied ? 'occupied' : 'available'} ${isSelected ? 'selected' : ''}`;
    box.innerHTML = `
      <div style="display:flex; justify-content:space-between; align-items:start;">
        <span class="table-title">${tableName}</span>
        <span class="table-badge">${isOccupied ? 'Có khách' : 'Trống'}</span>
      </div>
      <div class="table-seats">
        <span>👥 ${table.capacity ?? table.seats ?? 4} chỗ</span>
      </div>
    `;

    box.addEventListener('click', () => selectTable(table));
    grid.appendChild(box);
  });
}

// Chọn bàn
export async function selectTable(table) {
  if (['RESERVED', 'OUT_OF_SERVICE'].includes(table.status)) {
    showToast('Bàn này đang được đặt trước hoặc ngừng sử dụng.', 'warning');
    return;
  }
  posState.selectedTable = table;
  const cashInput = document.getElementById('cashRemainingInput');
  if (cashInput) cashInput.value = '';
  renderTables();

  const tableName = table.displayName || table.name || ('Bàn ' + (table.tableNumber ?? table.id));
  document.getElementById('posSelectedTableName').textContent = tableName;
  document.querySelector('.pos-container').classList.add('ordering');
  const titleEl = document.getElementById('posActiveTableTitle');
  if (titleEl) {
    titleEl.textContent = `${tableName} (${table.status === 'OCCUPIED' ? 'Đang phục vụ' : 'Bàn trống'})`;
  }

  // Nếu bàn đang phục vụ thì lấy hóa đơn hiện tại
  if (table.status === 'OCCUPIED') {
    try {
      const inv = await invoiceApi.getOpenByTable(table.id);
      posState.activeInvoice = inv;
      renderInvoiceCart();
    } catch (e) {
      posState.activeInvoice = null;
      renderInvoiceCart();
    }
  } else {
    // Bàn trống thì dọn sạch giỏ
    posState.activeInvoice = null;
    renderInvoiceCart();
  }
}

// 2. Render Danh mục món
function renderCategories() {
  const container = document.getElementById('categoriesBar');
  if (!container) return;
  container.innerHTML = '<button type="button" class="cat-chip" data-id="ALL">Tất cả món</button>';

  posState.categories.forEach(cat => {
    const chip = document.createElement('button');
    chip.type = 'button';
    chip.className = 'cat-chip';
    chip.dataset.id = cat.id;
    chip.textContent = cat.name;
    chip.addEventListener('click', () => {
      document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      posState.categoryFilter = cat.id;
      posState.productPage = 1;
      renderProducts();
    });
    container.appendChild(chip);
  });

  const allChip = container.querySelector('[data-id="ALL"]');
  container.querySelectorAll('.cat-chip').forEach(chip => {
    const active = String(chip.dataset.id) === String(posState.categoryFilter);
    chip.classList.toggle('active', active);
    chip.setAttribute('aria-pressed', String(active));
  });
  if (allChip) {
    allChip.addEventListener('click', (e) => {
      document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('active'));
      e.target.classList.add('active');
      posState.categoryFilter = 'ALL';
      posState.productPage = 1;
      renderProducts();
    });
  }
}

// 3. Render Thực đơn món ăn (Có Phân Trang)
export function renderProducts() {
  document.querySelectorAll('#categoriesBar .cat-chip').forEach(chip => {
    const active = String(chip.dataset.id) === String(posState.categoryFilter);
    chip.classList.toggle('active', active);
    chip.setAttribute('aria-pressed', String(active));
  });
  const grid = document.getElementById('productsGrid');
  const pagEl = document.getElementById('posProductsPagination');
  if (!grid) return;
  grid.innerHTML = '';

  const filtered = posState.products.filter(p => {
    const matchCat = posState.categoryFilter === 'ALL' || String(p.categoryId) === String(posState.categoryFilter);
    const matchSearch = !posState.searchKeyword || (p.name && p.name.toLowerCase().includes(posState.searchKeyword));
    return matchCat && matchSearch;
  });

  const total = filtered.length;
  const totalPages = Math.ceil(total / posState.productPageSize) || 1;
  if (posState.productPage > totalPages) posState.productPage = totalPages;
  if (posState.productPage < 1) posState.productPage = 1;

  if (filtered.length === 0) {
    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: #94a3b8; padding: 40px;">Không tìm thấy món phù hợp</div>';
    if (pagEl) pagEl.innerHTML = '';
    return;
  }

  const start = (posState.productPage - 1) * posState.productPageSize;
  const end = Math.min(start + posState.productPageSize, total);
  const pagedProducts = filtered.slice(start, end);

  pagedProducts.forEach(p => {
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
      card.addEventListener('click', () => openProductOptions(p));
    }
    grid.appendChild(card);
  });

  // Render thanh phân trang POS
  if (pagEl) {
    pagEl.innerHTML = `
      <span>Trang <strong>${posState.productPage} / ${totalPages}</strong> (tổng ${total} món)</span>
      <div class="pos-pagination-btns">
        <button class="btn-action-sm" id="btnPosPrev" ${posState.productPage === 1 ? 'disabled' : ''}>‹ Trước</button>
        <button class="btn-action-sm" id="btnPosNext" ${posState.productPage === totalPages ? 'disabled' : ''}>Sau ›</button>
      </div>
    `;

    const prevBtn = document.getElementById('btnPosPrev');
    if (prevBtn) {
      prevBtn.addEventListener('click', () => {
        posState.productPage--;
        renderProducts();
      });
    }

    const nextBtn = document.getElementById('btnPosNext');
    if (nextBtn) {
      nextBtn.addEventListener('click', () => {
        posState.productPage++;
        renderProducts();
      });
    }
  }
}

// 4. Thêm món vào bàn (Gọi món)
function openProductOptions(product) {
  pendingProduct = product;
  document.getElementById('productOptionsForm').reset();
  document.getElementById('productOptionsTitle').textContent = product.name;
  document.getElementById('optionQuantity').max = product.quantity ?? 9999;
  document.getElementById('productOptionsError').textContent = '';
  document.getElementById('productOptionsDialog').showModal();
}
async function addFood(product, options) {
  if (!posState.selectedTable) {
    showToast('Vui lòng chọn một bàn trước khi gọi món!', 'warning');
    return;
  }

  try {
    const user = getCurrentUser();
    const employeeId = user?.employeeId || 1;

    const payload = {
      tableId: posState.selectedTable.id,
      employeeId: employeeId,
      items: [{ productId: product.id, ...options }],
    };

    const updated = await invoiceApi.orderFood(payload);
    posState.activeInvoice = updated;

    const tableName = posState.selectedTable.displayName || posState.selectedTable.name || ('Bàn ' + (posState.selectedTable.tableNumber ?? posState.selectedTable.id));
    showToast(`Đã thêm "${product.name}" vào ${tableName}`, 'success');

    // Chuyển bàn sang trạng thái có khách
    posState.selectedTable.status = 'OCCUPIED';
    const titleEl = document.getElementById('posActiveTableTitle');
    if (titleEl) {
      titleEl.textContent = `${tableName} (Đang phục vụ)`;
    }

    renderInvoiceCart();
    await loadPosData();
    return true;
  } catch (err) {
    document.getElementById('productOptionsError').textContent = err.message;
    return false;
  }
}

// 5. Render Giỏ hàng bàn đang chọn
export function renderInvoiceCart() {
  const container = document.getElementById('cartItemsBox');
  if (!container) return;
  container.innerHTML = '';

  const inv = posState.activeInvoice;
  if (!inv || !inv.details || inv.details.length === 0) {
    container.innerHTML = '<div class="empty-cart-msg">Bàn đang trống.<br>Hãy bấm vào món bên cạnh để gọi món!</div>';
    updateTotalsUI(0, 0, 0);
    return;
  }

  inv.details.forEach(item => {
    const div = document.createElement('div');
    div.className = 'cart-item';
    div.innerHTML = `
      <div class="cart-item-name">${item.productName || ('Món #' + item.productId)}</div>
      <div class="cart-item-qty">
        <button class="btn-qty" data-prod="${item.productId}" data-action="dec">-</button>
        <span>${item.quantity}</span>
        <button class="btn-qty" data-prod="${item.productId}" data-action="inc">+</button>
      </div>
      <div class="cart-item-price">${formatVND(item.lineTotal || (item.unitPrice * item.quantity))}</div>
    `;

    div.querySelector('[data-action="dec"]').addEventListener('click', () => changeItemQty(item, -1));
    div.querySelector('[data-action="inc"]').addEventListener('click', () => changeItemQty(item, 1));
    const optionsText = document.createElement('small');
    optionsText.className = 'cart-item-options';
    optionsText.textContent = (item.sugarPercent ?? 100) + '% đường · '
      + (item.iceOption === 'NONE' ? 'Không đá' : 'Đá bình thường')
      + (item.note ? ' · ' + item.note : '');
    div.querySelector('.cart-item-name').appendChild(optionsText);
    container.appendChild(div);
  });

  updateTotalsUI(inv.subtotal || 0, inv.discount || 0, inv.totalAmount || 0);
}

// Cập nhật số lượng món
async function changeItemQty(item, delta) {
  const inv = posState.activeInvoice;
  if (!inv || !posState.selectedTable) return;

  try {
    const user = getCurrentUser();
    const employeeId = user?.employeeId || inv.employeeId || 1;

    let updated;
    if (delta > 0) {
      updated = await invoiceApi.orderFood({
        tableId: posState.selectedTable.id,
        employeeId: employeeId,
        items: [{ productId: item.productId, quantity: 1, sugarPercent: item.sugarPercent, iceOption: item.iceOption, note: item.note }],
      });
    } else {
      updated = await invoiceApi.removeFood(inv.id, item.productId, 1, item.id);
    }

    if (updated) {
      posState.activeInvoice = updated;
    } else {
      posState.activeInvoice = await invoiceApi.getOpenByTable(posState.selectedTable.id).catch(() => null);
    }

    renderInvoiceCart();
    await loadPosData();
  } catch (err) {
    showToast('Lỗi cập nhật món: ' + err.message, 'error');
  }
}

function updateTotalsUI(subtotal, discount, total) {
  const subEl = document.getElementById('cartSubTotal');
  const disEl = document.getElementById('cartDiscount');
  const totEl = document.getElementById('cartFinalTotal');

  if (subEl) subEl.textContent = formatVND(subtotal);
  if (disEl) disEl.textContent = formatVND(discount);
  if (totEl) totEl.textContent = formatVND(total);

  const cashInput = document.getElementById('cashRemainingInput');
  if (cashInput) {
    const previousDue = Number(cashInput.value);
    if (cashInput.value === '' || cashDueTotal === null || !Number.isFinite(previousDue)) {
      cashInput.value = String(total);
    } else if (cashDueTotal !== total) {
      cashInput.value = String(previousDue + total - cashDueTotal);
    }
  }
  cashDueTotal = total;
  calculateChange();
}

function calculateChange() {
  const cashInput = document.getElementById('cashRemainingInput');
  const changeEl = document.getElementById('changeReturnText');
  if (!cashInput || !changeEl) return;

  const remaining = Number(cashInput.value) || 0;

  if (remaining < 0) {
    changeEl.textContent = formatVND(Math.abs(remaining));
    changeEl.style.color = '#10b981';
    changeEl.parentElement.firstElementChild.textContent = 'Tiền thối';
  } else if (remaining > 0) {
    changeEl.textContent = formatVND(remaining);
    changeEl.style.color = '#ef4444';
    changeEl.parentElement.firstElementChild.textContent = 'Còn phải trả';
  } else {
    changeEl.textContent = '0 ₫';
    changeEl.style.color = '#10b981';
    changeEl.parentElement.firstElementChild.textContent = 'Tiền thối';
  }
}

// Áp dụng % giảm giá
async function applyDiscount() {
  const inv = posState.activeInvoice;
  if (!inv) {
    showToast('Chưa có hóa đơn để giảm giá!', 'warning');
    return;
  }

  const percentInput = document.getElementById('discountPercentInput');
  const percent = parseFloat(percentInput?.value);
  if (isNaN(percent) || percent < 0 || percent > 100) {
    showToast('Phần trăm giảm giá phải từ 0% đến 100%!', 'warning');
    return;
  }

  try {
    const updated = await invoiceApi.applyDiscount(inv.id, { discountPercent: percent });
    posState.activeInvoice = updated;
    renderInvoiceCart();
    showToast(`Đã áp dụng giảm giá ${percent}%`, 'success');
  } catch (err) {
    showToast('Lỗi áp dụng giảm giá: ' + err.message, 'error');
  }
}

// Thanh toán hóa đơn
async function handleCheckout() {
  const inv = posState.activeInvoice;
  if (!inv || !inv.details || inv.details.length === 0) {
    showToast('Bàn chưa gọi món nào để thanh toán!', 'warning');
    return;
  }

  const tableName = posState.selectedTable?.displayName || posState.selectedTable?.name || ('Bàn #' + (posState.selectedTable?.tableNumber ?? inv.tableId));
  const confirmed = confirm(`Xác nhận thanh toán cho ${tableName} với số tiền ${formatVND(inv.totalAmount)}?`);
  if (!confirmed) return;

  try {
    const checkoutResult = await invoiceApi.checkout(inv.id, { paymentMethod: 'CASH' });
    showToast(`Thanh toán thành công ${tableName}!`, 'success');

    // Mở popup in hóa đơn nhiệt 80mm
    openThermalReceipt(checkoutResult || inv, tableName);

    // Reset bàn về trạng thái trống
    posState.activeInvoice = null;
    const cashInput = document.getElementById('cashRemainingInput');
    if (cashInput) cashInput.value = '';
    if (posState.selectedTable) {
      posState.selectedTable.status = 'AVAILABLE';
      const titleEl = document.getElementById('posActiveTableTitle');
      if (titleEl) {
        titleEl.textContent = `${tableName} (Bàn trống)`;
      }
    }

    await loadPosData();
    renderInvoiceCart();
  } catch (err) {
    showToast('Lỗi thanh toán: ' + err.message, 'error');
  }
}

// Hủy bàn
async function handleCancelTable() {
  const inv = posState.activeInvoice;
  if (!inv) {
    showToast('Bàn này không có hóa đơn để hủy!', 'warning');
    return;
  }

  const tableName = posState.selectedTable?.displayName || posState.selectedTable?.name || ('Bàn #' + (posState.selectedTable?.tableNumber ?? inv.tableId));
  const confirmed = confirm(`CẢNH BÁO: Bạn có chắc chắn muốn HỦY TOÀN BỘ đơn hàng của ${tableName}?`);
  if (!confirmed) return;

  try {
    await invoiceApi.cancel(inv.id);
    showToast(`Đã hủy hóa đơn ${tableName}`, 'warning');

    posState.activeInvoice = null;
    if (posState.selectedTable) {
      posState.selectedTable.status = 'AVAILABLE';
      const titleEl = document.getElementById('posActiveTableTitle');
      if (titleEl) {
        titleEl.textContent = `${tableName} (Bàn trống)`;
      }
    }

    await loadPosData();
    renderInvoiceCart();
  } catch (err) {
    showToast('Lỗi khi hủy bàn: ' + err.message, 'error');
  }
}
