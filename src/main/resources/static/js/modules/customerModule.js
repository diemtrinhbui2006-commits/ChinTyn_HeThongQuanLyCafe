import { customerApi } from '../api/customerApi.js';
import { showToast } from '../utils/toast.js';
import { completeManagementForm, managementFormError } from './managementForms.js';

/**
 * Module Khách Hàng (Customers)
 */
export async function loadCustomersAdmin() {
  const tbody = document.getElementById('customersAdminBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="5" align="center">Đang tải...</td></tr>';

  try {
    const list = await customerApi.getAll();
    tbody.innerHTML = '';

    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" align="center">Chưa có khách hàng nào</td></tr>';
      return;
    }

    list.forEach(c => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>#${c.id}</strong></td>
        <td><strong>${c.name}</strong></td>
        <td>${c.phone || '--'}</td>
        <td>${c.email || '--'}</td>
        <td style="font-size:0.85rem; color:#64748b;">${c.address || '--'}</td>
        <td><span class="badge-tag active">${Number(c.loyaltyPoints || 0)} điểm</span></td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="color:red;">Lỗi: ${err.message}</td></tr>`;
  }
}

export function initCustomerAdminEvents() {
  const btn = document.getElementById('btnAddCustomerSubmit');
  const nameInput = document.getElementById('addCustomerName');
  const phoneInput = document.getElementById('addCustomerPhone');
  const emailInput = document.getElementById('addCustomerEmail');

  if (btn && nameInput && phoneInput) {
    btn.addEventListener('click', async () => {
      if (btn.disabled) return;
      const name = nameInput.value.trim();
      const phone = phoneInput.value.trim();
      const email = emailInput ? emailInput.value.trim() : '';

      if (!name || !phone) {
        showToast('Vui lòng nhập họ tên và số điện thoại!', 'warning');
        return;
      }

      try {
        btn.disabled = true;
        await customerApi.create({ name, phone, email });
        nameInput.value = '';
        phoneInput.value = '';
        if (emailInput) emailInput.value = '';
        completeManagementForm('btnAddCustomerSubmit');
        showToast('Thêm khách hàng thành công!', 'success');
        loadCustomersAdmin();
      } catch (err) {
        managementFormError('btnAddCustomerSubmit', err.message);
        showToast('Lỗi thêm khách: ' + err.message, 'error');
      } finally {
        btn.disabled = false;
      }
    });
  }
}
