import { request } from '../api/apiClient.js';
import { showToast } from '../utils/toast.js';
import { completeManagementForm, managementFormError } from './managementForms.js';

const escape = value => String(value ?? '').replace(/[&<>"']/g, c =>
  ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const money = value => `${Number(value || 0).toLocaleString('vi-VN')} ₫`;
const asTable = (headers, rows) => `<table class="data-table"><thead><tr>${headers.map(x => `<th>${x}</th>`).join('')}</tr></thead><tbody>${rows || '<tr><td colspan="8">Chưa có dữ liệu</td></tr>'}</tbody></table>`;
const options = (values, id, label) => `<option value="">-- Chọn --</option>${(values || []).map(v => `<option value="${v[id]}">${escape(v[label])}</option>`).join('')}`;
const post = (url, data) => request(url, { method: 'POST', body: JSON.stringify(data) });
const put = (url, data) => request(url, { method: 'PUT', body: JSON.stringify(data) });
const setHtml = (id, content) => { const el = document.getElementById(id); if (el) el.innerHTML = content; };

export async function loadOperations() {
  try {
    const [ingredients, reservations, shifts, suppliers, expenses, promotions, queue, tables, rewards, lowProducts] = await Promise.all([
      request('/api/inventory/ingredients').catch(() => []),
      request('/api/reservations').catch(() => []),
      request('/api/shifts').catch(() => []),
      request('/api/suppliers').catch(() => []),
      request('/api/expenses').catch(() => []),
      request('/api/promotions').catch(() => []),
      request('/api/qr/orders/pending').catch(() => []),
      request('/api/tables').catch(() => []),
      request('/api/rewards').catch(() => []),
      request('/api/inventory/products/low-stock?threshold=5').catch(() => [])
    ]);

    setHtml('opIngredients', asTable(['Nguyên liệu', 'Đơn vị', 'Tồn kho', 'Ngưỡng', 'Cảnh báo'],
      (ingredients || []).map(i => `<tr><td>${escape(i.name)}</td><td>${escape(i.unit)}</td><td>${i.quantity}</td><td>${i.lowStockThreshold}</td><td>${Number(i.quantity) <= Number(i.lowStockThreshold) ? 'SẮP HẾT' : 'Đủ'}</td></tr>`).join('')));
    setHtml('opIngredientSelect', options(ingredients, 'id', 'name'));
    setHtml('opPurchaseIngredient', options(ingredients, 'id', 'name'));
    setHtml('opProductLowStock', `<h4>Món sắp hết (≤ 5 sản phẩm)</h4>` +
      asTable(['Món', 'Tồn kho', 'Ngưỡng cảnh báo'], (lowProducts || []).map(p => `<tr><td>${escape(p.name)}</td><td>${p.quantity}</td><td>${p.lowStockThreshold}</td></tr>`).join('')));

    // Legacy reservations fallback if element exists
    const resEl = document.getElementById('opReservations');
    if (resEl) {
      resEl.innerHTML = asTable(['Bàn', 'Khách', 'Liên hệ', 'Thời gian', 'Số khách', 'Trạng thái', ''],
        (reservations || []).map(r => `<tr><td>${r.tableNumber}</td><td>${escape(r.customerName)}</td><td>${escape(r.phone)}</td><td>${new Date(r.reservedFor).toLocaleString('vi-VN')}</td><td>${r.partySize}</td><td>${escape(r.status)}</td><td>${r.status === 'BOOKED' ? `<button data-cancel-reservation="${r.id}">Hủy</button>` : ''}</td></tr>`).join(''));
      resEl.querySelectorAll('[data-cancel-reservation]').forEach(button => button.onclick = async () => {
        try {
          await post(`/api/reservations/${button.dataset.cancelReservation}/cancel`, {});
          await loadOperations();
        } catch (error) {
          showToast(error.message, 'error');
        }
      });
    }

    setHtml('opShifts', asTable(['Nhân viên', 'Mở ca', 'Đóng ca', 'Tiền đầu ca', 'Dự kiến', 'Thực tế', 'Chênh lệch'],
      (shifts || []).map(s => `<tr><td>${escape(s.employeeName)}</td><td>${new Date(s.openedAt).toLocaleString('vi-VN')}</td><td>${s.closedAt ? new Date(s.closedAt).toLocaleString('vi-VN') : 'Đang mở'}</td><td>${money(s.openingCash)}</td><td>${money(s.expectedCash)}</td><td>${money(s.closingCash)}</td><td>${s.expectedCash == null || s.closingCash == null ? '--' : money(Number(s.closingCash) - Number(s.expectedCash))}</td></tr>`).join('')));
    const openShifts = (shifts || []).filter(s => !s.closedAt);
    setHtml('opShiftSelect', `<option value="">-- Chọn ca đang mở --</option>${openShifts.map(s => `<option value="${s.id}">${escape(s.employeeName)} · #${s.id}</option>`).join('')}`);

    setHtml('opSuppliers', asTable(['Nhà cung cấp', 'Điện thoại', 'Email', ''],
      (suppliers || []).map(s => `<tr><td>${escape(s.name)}</td><td>${escape(s.phone)}</td><td>${escape(s.email)}</td><td><button data-edit-supplier="${s.id}">Sửa liên hệ</button></td></tr>`).join('')));
    const supEl = document.getElementById('opSuppliers');
    if (supEl) {
      supEl.querySelectorAll('[data-edit-supplier]').forEach(button => button.onclick = async () => {
        const current = suppliers.find(s => s.id === Number(button.dataset.editSupplier));
        const phone = window.prompt('Số điện thoại nhà cung cấp:', current.phone || '');
        if (phone !== null) {
          try {
            await put(`/api/suppliers/${current.id}`, { ...current, phone });
            await loadOperations();
          } catch (error) {
            showToast(error.message, 'error');
          }
        }
      });
    }

    setHtml('opExpenseSupplier', options(suppliers, 'id', 'name'));
    setHtml('opExpenses', asTable(['Nội dung', 'Danh mục', 'Số tiền', 'Phương thức', 'Nhà cung cấp', 'Ngày'],
      (expenses || []).map(e => `<tr><td>${escape(e.description)}</td><td>${escape(e.category)}</td><td>${money(e.amount)}</td><td>${escape(e.paymentMethod)}</td><td>${escape(e.supplierName)}</td><td>${new Date(e.createdAt).toLocaleString('vi-VN')}</td></tr>`).join('')));

    // Promotions
    setHtml('opPromotions', asTable(['Mã', 'Tên', 'Ưu đãi', 'Tối thiểu', 'Sử dụng', 'Hiệu lực', ''],
      (promotions || []).map(p => `<tr><td>${escape(p.code)}</td><td>${escape(p.name)}</td><td>${p.discountValue}${p.discountType === 'PERCENT' ? '%' : ' ₫'}</td><td>${money(p.minimumSpend)}</td><td>${p.usageCount}${p.usageLimit ? ` / ${p.usageLimit}` : ''}</td><td>${p.active ? 'Đang bật' : 'Tắt'}</td><td><button data-toggle-promotion="${p.id}">${p.active ? 'Tắt mã' : 'Bật mã'}</button></td></tr>`).join('')));
    const promoEl = document.getElementById('opPromotions');
    if (promoEl) {
      promoEl.querySelectorAll('[data-toggle-promotion]').forEach(button => button.onclick = async () => {
        const current = promotions.find(p => p.id === Number(button.dataset.togglePromotion));
        try {
          await put(`/api/promotions/${current.id}`, { ...current, active: !current.active });
          await loadOperations();
        } catch (error) {
          showToast(error.message, 'error');
        }
      });
    }

    setHtml('opRewardSelect', `<option value="">-- Chọn phần thưởng --</option>${(rewards || []).map(r => `<option value="${r.id}">${escape(r.name)} · ${r.pointsCost} điểm</option>`).join('')}`);
    setHtml('opQrQueue', asTable(['Bàn', 'Khách', 'Món yêu cầu', 'Thời gian', 'Duyệt'],
      (queue || []).map(o => `<tr><td>${o.tableNumber}</td><td>${escape(o.guestName)}</td><td>${escape(o.itemsJson)}</td><td>${new Date(o.createdAt).toLocaleString('vi-VN')}</td><td><button data-approve-qr="${o.id}">Duyệt vào hóa đơn</button></td></tr>`).join('')));
    const qrQueueEl = document.getElementById('opQrQueue');
    if (qrQueueEl) {
      qrQueueEl.querySelectorAll('[data-approve-qr]').forEach(button => button.onclick = () => approveQr(button.dataset.approveQr));
    }

    for (const id of ['opReservationTable', 'opQrTable']) {
      setHtml(id, options(tables, 'id', 'tableNumber'));
    }
  } catch (error) {
    showToast(`Không thể tải dữ liệu vận hành: ${error.message}`, 'error');
  }
}

export function initOperationsEvents() {
  bind('opAddIngredient', async () => {
    await post('/api/inventory/ingredients', { name: val('opIngredientName'), unit: val('opIngredientUnit'), lowStockThreshold: Number(val('opIngredientThreshold') || 0) });
  });
  bind('opAdjustStock', async () => {
    await post(`/api/inventory/ingredients/${val('opIngredientSelect')}/adjust`, { quantity: Number(val('opStockQuantity')), note: 'Điều chỉnh tồn kho từ màn hình vận hành' });
  });
  bind('opCreateReservation', async () => {
    const date = val('opReservationDate');
    await post('/api/reservations', { tableId: Number(val('opReservationTable')), customerName: val('opReservationName'),
      phone: val('opReservationPhone'), reservedFor: date, partySize: Number(val('opReservationParty')) });
  });
  bind('opOpenShift', async () => {
    const user = await request('/api/auth/me');
    if (!user.employeeId) throw new Error('Tài khoản chưa gắn với hồ sơ nhân viên');
    await post('/api/shifts/open', { employeeId: user.employeeId, openingCash: Number(val('opOpeningCash')) });
  });
  bind('opCloseShift', async () => {
    await post(`/api/shifts/${val('opShiftSelect')}/close`, { closingCash: Number(val('opClosingCash')) });
  });
  bind('opAddSupplier', async () => {
    await post('/api/suppliers', { name: val('opSupplierName'), phone: val('opSupplierPhone') });
  });
  bind('opAddExpense', async () => {
    const ingredientId = val('opPurchaseIngredient');
    const supplierId = val('opExpenseSupplier');
    await post('/api/expenses', { description: val('opExpenseDescription'), category: val('opExpenseCategory'),
      amount: Number(val('opExpenseAmount')), paymentMethod: val('opExpenseMethod'),
      supplierId: supplierId ? Number(supplierId) : null,
      ingredientId: ingredientId ? Number(ingredientId) : null,
      stockQuantity: val('opPurchaseQuantity') ? Number(val('opPurchaseQuantity')) : null });
  });
  bind('opAddPromotion', async () => {
    await post('/api/promotions', { code: val('opPromoCode'), name: val('opPromoName'), discountType: val('opPromoType'),
      discountValue: Number(val('opPromoValue')), minimumSpend: val('opPromoMinimum') ? Number(val('opPromoMinimum')) : null,
      startsAt: val('opPromoStarts') || null,
      endsAt: val('opPromoEnds') || null,
      usageLimit: val('opPromoLimit') ? Number(val('opPromoLimit')) : null,
      active: true });
  });
  bind('opCreateQr', async () => {
    const result = await post(`/api/qr/tables/${val('opQrTable')}/token`, {});
    const link = `${location.origin}${result.url}`;
    setHtml('opQrLink', `<a href="${escape(link)}" target="_blank" rel="noopener">${escape(link)}</a>` +
      `<br><img alt="Mã QR đặt món" width="220" height="220" src="/api/qr/tokens/${encodeURIComponent(result.token)}/image">`);
  });
  bind('opLoadLoyalty', async () => {
    const data = await request(`/api/customers/${val('opLoyaltyCustomer')}/loyalty`);
    setHtml('opLoyaltyHistory', `<p>Điểm hiện có: <strong>${data.balance}</strong></p>` +
      asTable(['Ngày', 'Loại', 'Điểm', 'Nội dung'], data.history.map(e => `<tr><td>${new Date(e.createdAt).toLocaleString('vi-VN')}</td><td>${escape(e.entryType)}</td><td>${e.points}</td><td>${escape(e.description)}</td></tr>`).join('')));
  });
  bind('opAddReward', async () => {
    await post('/api/rewards', { name: val('opRewardName'), pointsCost: Number(val('opRewardCost')), active: true });
  });
  bind('opRedeemReward', async () => {
    await post(`/api/customers/${val('opLoyaltyCustomer')}/loyalty/redeem-reward`, { rewardId: Number(val('opRewardSelect')) });
    document.getElementById('opLoadLoyalty')?.click();
  });
}

async function approveQr(id) {
  try {
    const user = await request('/api/auth/me');
    if (!user.employeeId) throw new Error('Tài khoản chưa gắn với hồ sơ nhân viên');
    await post(`/api/qr/orders/${id}/approve`, { employeeId: user.employeeId });
    showToast('Đã duyệt yêu cầu QR vào hóa đơn.', 'success'); await loadOperations();
  } catch (error) { showToast(error.message, 'error'); }
}
function val(id) { return document.getElementById(id)?.value?.trim() || ''; }
function bind(id, action) {
  document.getElementById(id)?.addEventListener('click', async event => {
    const button = event.currentTarget;
    if (button.disabled) return;
    try { button.disabled = true; await action(); completeManagementForm(id); showToast('Đã lưu dữ liệu.', 'success'); await loadOperations(); }
    catch (error) { managementFormError(id, error.message); showToast(error.message, 'error'); }
    finally { button.disabled = false; }
  });
}
