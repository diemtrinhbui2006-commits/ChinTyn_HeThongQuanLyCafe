import { productApi } from '../api/productApi.js';
import { categoryApi } from '../api/categoryApi.js';
import { formatVND } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';

/**
 * Module Quản Lý Thực Đơn (Products)
 */
export async function loadProductsAdmin() {
  const tbody = document.getElementById('productsAdminBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="6" align="center">Đang tải...</td></tr>';

  try {
    const list = await productApi.getAll();
    tbody.innerHTML = '';

    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" align="center">Chưa có món nào</td></tr>';
      return;
    }

    list.forEach(p => {
      const isAvail = p.available !== false;
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>#${p.id}</strong></td>
        <td>${p.name}</td>
        <td>${p.category?.name || p.categoryId || 'Đồ uống'}</td>
        <td style="font-weight:700; color:#d97706;">${formatVND(p.price)}</td>
        <td><span class="badge-tag ${isAvail ? 'active' : 'disabled'}">${isAvail ? 'Đang bán' : 'Ngừng bán'}</span></td>
        <td>
          <button class="btn-action-sm btn-toggle-avail" data-id="${p.id}" data-status="${!isAvail}">
            ${isAvail ? 'Ngừng bán' : 'Bật bán'}
          </button>
        </td>
      `;
      tbody.appendChild(tr);
    });

    // Gán sự kiện click cho các nút Bật/Tắt bán
    tbody.querySelectorAll('.btn-toggle-avail').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.id;
        const newStatus = btn.dataset.status === 'true';
        try {
          await productApi.setAvailability(id, newStatus);
          showToast('Cập nhật trạng thái thành công', 'success');
          loadProductsAdmin();
        } catch (e) {
          showToast('Lỗi cập nhật: ' + e.message, 'error');
        }
      });
    });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="color:red;">Lỗi: ${err.message}</td></tr>`;
  }
}

export function initProductModalEvents() {
  const openBtn = document.getElementById('btnOpenAddProductModal');
  if (openBtn) {
    openBtn.addEventListener('click', async () => {
      const select = document.getElementById('addProductCategorySelect');
      if (select) {
        select.innerHTML = '';
        const cats = await categoryApi.getAll().catch(() => []);
        cats.forEach(c => {
          select.innerHTML += `<option value="${c.id}">${c.name}</option>`;
        });
      }
      document.getElementById('addProductModal')?.classList.add('show');
    });
  }

  const closeBtn = document.getElementById('btnCloseAddProductModal');
  if (closeBtn) {
    closeBtn.addEventListener('click', () => {
      document.getElementById('addProductModal')?.classList.remove('show');
    });
  }

  const submitBtn = document.getElementById('btnSubmitAddProduct');
  if (submitBtn) {
    submitBtn.addEventListener('click', async () => {
      const name = document.getElementById('addProductName')?.value.trim();
      const price = parseFloat(document.getElementById('addProductPrice')?.value);
      const categoryId = parseInt(document.getElementById('addProductCategorySelect')?.value);
      const quantity = parseInt(document.getElementById('addProductQty')?.value) || 100;
      const description = document.getElementById('addProductDesc')?.value.trim();

      if (!name || isNaN(price) || price <= 0 || !categoryId) {
        showToast('Vui lòng nhập tên món và giá tiền hợp lệ (> 0)!', 'warning');
        return;
      }

      try {
        await productApi.create({ name, price, categoryId, quantity, description });
        showToast(`Đã thêm món "${name}" thành công!`, 'success');
        document.getElementById('addProductModal')?.classList.remove('show');
        loadProductsAdmin();
      } catch (err) {
        showToast('Lỗi thêm món: ' + err.message, 'error');
      }
    });
  }
}
