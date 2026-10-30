import { supplierExpenseApi } from '../api/supplierExpenseApi.js';
import { formatVND, formatDate } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';

let expensesList = [];
let suppliersList = [];
let ingredientsList = [];
let currentSubTab = 'expenses'; // 'expenses' | 'suppliers'

export async function loadSupplierExpenses() {
  try {
    const [expData, supData, ingData] = await Promise.all([
      supplierExpenseApi.getExpenses().catch(() => []),
      supplierExpenseApi.getSuppliers().catch(() => []),
      supplierExpenseApi.getIngredients().catch(() => []),
    ]);

    expensesList = expData || [];
    suppliersList = supData || [];
    ingredientsList = ingData || [];

    // Populate dropdowns in modals
    populateModalsDropdowns();

    // Render active tab
    if (currentSubTab === 'expenses') {
      renderExpensesTable();
    } else {
      renderSuppliersTable();
    }
  } catch (err) {
    showToast(`Lỗi tải dữ liệu chi phí & NCC: ${err.message}`, 'error');
  }
}

function populateModalsDropdowns() {
  const expSupSelect = document.getElementById('expenseModalSupplierSelect');
  if (expSupSelect) {
    expSupSelect.innerHTML = '<option value="">-- Không chọn (Chi phí ngoài) --</option>' +
      suppliersList.map(s => `<option value="${s.id}">${escapeHtml(s.name)}</option>`).join('');
  }

  const expIngSelect = document.getElementById('expenseModalIngredientSelect');
  if (expIngSelect) {
    expIngSelect.innerHTML = '<option value="">-- Không nhập kho (Chi phí thường) --</option>' +
      ingredientsList.map(i => `<option value="${i.id}">${escapeHtml(i.name)} (${escapeHtml(i.unit)})</option>`).join('');
  }
}

function renderExpensesTable() {
  const tbody = document.getElementById('expensesTableBody');
  const totalAmountEl = document.getElementById('kpiTotalExpensesAmount');
  if (!tbody) return;

  const total = expensesList.reduce((sum, e) => sum + (Number(e.amount) || 0), 0);
  if (totalAmountEl) totalAmountEl.textContent = formatVND(total);

  if (expensesList.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" align="center" style="padding:24px; color:#94a3b8;">Chưa ghi nhận khoản chi phí nào</td></tr>';
    return;
  }

  const methodLabels = {
    CASH: '💵 Tiền mặt',
    BANK_TRANSFER: '🏦 Chuyển khoản',
    CARD: '💳 Quẹt thẻ',
    E_WALLET: '📱 Ví điện tử',
  };

  tbody.innerHTML = expensesList.map(e => `
    <tr>
      <td><strong>#${e.id}</strong></td>
      <td><strong>${escapeHtml(e.description)}</strong></td>
      <td><span class="badge-tag pending">${escapeHtml(e.category || 'Chi phí')}</span></td>
      <td style="font-weight:700; color:#ef4444;">-${formatVND(e.amount)}</td>
      <td>${methodLabels[e.paymentMethod] || e.paymentMethod}</td>
      <td>${escapeHtml(e.supplierName || 'Vãng lai')}</td>
      <td>${formatDate(e.createdAt)}</td>
    </tr>
  `).join('');
}

function renderSuppliersTable() {
  const tbody = document.getElementById('suppliersTableBody');
  if (!tbody) return;

  if (suppliersList.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" align="center" style="padding:24px; color:#94a3b8;">Chưa có nhà cung cấp nào</td></tr>';
    return;
  }

  tbody.innerHTML = suppliersList.map(s => `
    <tr>
      <td><strong>#${s.id}</strong></td>
      <td><strong style="color:var(--text-primary);">${escapeHtml(s.name)}</strong></td>
      <td>${escapeHtml(s.contactName || '--')}</td>
      <td><span style="font-weight:600;">${escapeHtml(s.phone || '--')}</span></td>
      <td>${escapeHtml(s.email || '--')}</td>
      <td><small style="color:#64748b;">${escapeHtml(s.address || '--')}</small></td>
      <td>
        <button class="btn-action-sm btn-edit-supplier" data-id="${s.id}" title="Chỉnh sửa thông tin">✏️ Sửa</button>
      </td>
    </tr>
  `).join('');

  tbody.querySelectorAll('.btn-edit-supplier').forEach(btn => {
    btn.onclick = () => {
      const id = Number(btn.dataset.id);
      const sup = suppliersList.find(s => s.id === id);
      if (sup) openSupplierModal(sup);
    };
  });
}

function openSupplierModal(sup = null) {
  const modal = document.getElementById('supplierModal');
  const title = document.getElementById('supplierModalTitle');
  const form = document.getElementById('formSupplier');
  if (!modal || !form) return;

  if (sup) {
    if (title) title.textContent = '✏️ Chỉnh Sửa Nhà Cung Cấp';
    document.getElementById('supplierModalId').value = sup.id;
    document.getElementById('supplierModalName').value = sup.name || '';
    document.getElementById('supplierModalContact').value = sup.contactName || '';
    document.getElementById('supplierModalPhone').value = sup.phone || '';
    document.getElementById('supplierModalEmail').value = sup.email || '';
    document.getElementById('supplierModalAddress').value = sup.address || '';
  } else {
    if (title) title.textContent = '🚚 Thêm Nhà Cung Cấp Mới';
    form.reset();
    document.getElementById('supplierModalId').value = '';
  }
  modal.classList.add('show');
}

export function initSupplierExpenseEvents() {
  // Tab switcher (Expenses vs Suppliers)
  const tabBtnExp = document.getElementById('tabBtnExpenses');
  const tabBtnSup = document.getElementById('tabBtnSuppliers');
  const viewExp = document.getElementById('subviewExpenses');
  const viewSup = document.getElementById('subviewSuppliers');

  if (tabBtnExp && tabBtnSup) {
    tabBtnExp.onclick = () => {
      currentSubTab = 'expenses';
      tabBtnExp.classList.add('active');
      tabBtnSup.classList.remove('active');
      if (viewExp) viewExp.style.display = 'block';
      if (viewSup) viewSup.style.display = 'none';
      renderExpensesTable();
    };

    tabBtnSup.onclick = () => {
      currentSubTab = 'suppliers';
      tabBtnSup.classList.add('active');
      tabBtnExp.classList.remove('active');
      if (viewExp) viewExp.style.display = 'none';
      if (viewSup) viewSup.style.display = 'block';
      renderSuppliersTable();
    };
  }

  // Modal Chi phí
  const expModal = document.getElementById('addExpenseModal');
  const openExpBtn = document.getElementById('btnOpenAddExpenseModal');
  const closeExpBtn = document.getElementById('btnCloseExpenseModal');
  const cancelExpBtn = document.getElementById('btnCancelExpenseModal');
  const formExp = document.getElementById('formAddExpense');

  const hideExp = () => { if (expModal) expModal.classList.remove('show'); };
  if (openExpBtn) openExpBtn.onclick = () => { if (expModal) expModal.classList.add('show'); };
  if (closeExpBtn) closeExpBtn.onclick = hideExp;
  if (cancelExpBtn) cancelExpBtn.onclick = hideExp;

  if (formExp) {
    formExp.onsubmit = async (e) => {
      e.preventDefault();
      const description = document.getElementById('expenseModalDesc')?.value.trim();
      const category = document.getElementById('expenseModalCategory')?.value.trim();
      const amount = Number(document.getElementById('expenseModalAmount')?.value || 0);
      const paymentMethod = document.getElementById('expenseModalPaymentMethod')?.value;
      const supplierIdVal = document.getElementById('expenseModalSupplierSelect')?.value;
      const ingredientIdVal = document.getElementById('expenseModalIngredientSelect')?.value;
      const stockQtyVal = document.getElementById('expenseModalStockQty')?.value;

      if (!description || !category || amount <= 0) {
        showToast('Vui lòng điền nội dung, danh mục và số tiền hợp lệ!', 'warning');
        return;
      }

      const payload = {
        description,
        category,
        amount,
        paymentMethod,
        supplierId: supplierIdVal ? Number(supplierIdVal) : null,
        ingredientId: ingredientIdVal ? Number(ingredientIdVal) : null,
        stockQuantity: (ingredientIdVal && stockQtyVal) ? Number(stockQtyVal) : null,
      };

      try {
        await supplierExpenseApi.createExpense(payload);
        showToast('Đã ghi nhận khoản chi phí & cập nhật kho!', 'success');
        hideExp();
        formExp.reset();
        await loadSupplierExpenses();
      } catch (err) {
        showToast(`Lỗi ghi chi phí: ${err.message}`, 'error');
      }
    };
  }

  // Modal Nhà Cung Cấp
  const supModal = document.getElementById('supplierModal');
  const openSupBtn = document.getElementById('btnOpenAddSupplierModal');
  const closeSupBtn = document.getElementById('btnCloseSupplierModal');
  const cancelSupBtn = document.getElementById('btnCancelSupplierModal');
  const formSup = document.getElementById('formSupplier');

  const hideSup = () => { if (supModal) supModal.classList.remove('show'); };
  if (openSupBtn) openSupBtn.onclick = () => openSupplierModal(null);
  if (closeSupBtn) closeSupBtn.onclick = hideSup;
  if (cancelSupBtn) cancelSupBtn.onclick = hideSup;

  if (formSup) {
    formSup.onsubmit = async (e) => {
      e.preventDefault();
      const id = document.getElementById('supplierModalId')?.value;
      const name = document.getElementById('supplierModalName')?.value.trim();
      const contactName = document.getElementById('supplierModalContact')?.value.trim();
      const phone = document.getElementById('supplierModalPhone')?.value.trim();
      const email = document.getElementById('supplierModalEmail')?.value.trim();
      const address = document.getElementById('supplierModalAddress')?.value.trim();

      if (!name) {
        showToast('Tên nhà cung cấp là bắt buộc!', 'warning');
        return;
      }

      const payload = { name, contactName, phone, email, address };

      try {
        if (id) {
          await supplierExpenseApi.updateSupplier(id, payload);
          showToast('Đã cập nhật nhà cung cấp thành công!', 'success');
        } else {
          await supplierExpenseApi.createSupplier(payload);
          showToast('Đã thêm nhà cung cấp mới thành công!', 'success');
        }
        hideSup();
        formSup.reset();
        await loadSupplierExpenses();
      } catch (err) {
        showToast(`Lỗi lưu nhà cung cấp: ${err.message}`, 'error');
      }
    };
  }

  document.getElementById('btnRefreshSupplierExpenses')?.addEventListener('click', loadSupplierExpenses);
}

function escapeHtml(text) {
  return String(text || '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
