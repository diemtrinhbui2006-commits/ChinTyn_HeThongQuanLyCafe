import { productApi } from '../api/productApi.js';
import { categoryApi } from '../api/categoryApi.js';
import { formatVND } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';

let cachedProducts = [];
let cachedCategories = [];

/**
 * Module Quản Lý Thực Đơn (Products)
 */
export async function loadProductsAdmin() {
  const tbody = document.getElementById('productsAdminBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="6" align="center">Đang tải danh sách món...</td></tr>';

  try {
    const [products, categories] = await Promise.all([
      productApi.getAll().catch(() => []),
      categoryApi.getAll().catch(() => []),
    ]);

    cachedProducts = products || [];
    cachedCategories = categories || [];

    // Tạo từ điển mã danh mục -> tên danh mục
    const catMap = {};
    cachedCategories.forEach(c => {
      catMap[c.id] = c.name;
    });

    tbody.innerHTML = '';

    if (cachedProducts.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" align="center">Chưa có món nào trong thực đơn</td></tr>';
      return;
    }

    cachedProducts.forEach(p => {
      const isAvail = p.available !== false;
      const catName = catMap[p.categoryId] || (p.category?.name) || 'Chung';
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>#${p.id}</strong></td>
        <td><strong>${p.name}</strong></td>
        <td><span style="background:#f1f5f9; padding:2px 8px; border-radius:4px; font-size:0.82rem;">${catName}</span></td>
        <td style="font-weight:700; color:#d97706;">${formatVND(p.price)}</td>
        <td><span class="badge-tag ${isAvail ? 'active' : 'disabled'}">${isAvail ? 'Đang bán' : 'Ngừng bán'}</span></td>
        <td>
          <button class="btn-action-sm btn-edit-prod" data-id="${p.id}" style="color:#2563eb; border-color:#93c5fd;">
            ✏️ Sửa
          </button>
          <button class="btn-action-sm btn-toggle-avail" data-id="${p.id}" data-status="${!isAvail}">
            ${isAvail ? 'Ngừng bán' : 'Bật bán'}
          </button>
        </td>
      `;
      tbody.appendChild(tr);
    });

    // 1. Gán sự kiện click Sửa món
    tbody.querySelectorAll('.btn-edit-prod').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = parseInt(btn.dataset.id);
        const prod = cachedProducts.find(x => x.id === id);
        if (prod) openEditProductModal(prod);
      });
    });

    // 2. Gán sự kiện click Bật/Tắt bán
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
    tbody.innerHTML = `<tr><td colspan="6" style="color:red;">Lỗi tải dữ liệu: ${err.message}</td></tr>`;
  }
}

/**
 * Mở modal chỉnh sửa món và điền sẵn thông tin
 */
function openEditProductModal(prod) {
  const modal = document.getElementById('editProductModal');
  if (!modal) return;

  document.getElementById('editProductId').value = prod.id;
  document.getElementById('editProductName').value = prod.name || '';
  document.getElementById('editProductPrice').value = prod.price || '';
  document.getElementById('editProductQty').value = prod.quantity ?? 100;
  document.getElementById('editProductDesc').value = prod.description || '';

  // Đổ danh mục vào select
  const select = document.getElementById('editProductCategorySelect');
  if (select) {
    select.innerHTML = '';
    cachedCategories.forEach(c => {
      const isSelected = (c.id === prod.categoryId) ? 'selected' : '';
      select.innerHTML += `<option value="${c.id}" ${isSelected}>${c.name}</option>`;
    });
  }

  modal.classList.add('show');
}

/**
 * Khởi tạo sự kiện cho các modal Thêm món và Sửa món
 */
export function initProductModalEvents() {
  // === THÊM MÓN MỚI ===
  const openAddBtn = document.getElementById('btnOpenAddProductModal');
  if (openAddBtn) {
    openAddBtn.addEventListener('click', async () => {
      const select = document.getElementById('addProductCategorySelect');
      if (select) {
        select.innerHTML = '';
        const cats = await categoryApi.getAll().catch(() => cachedCategories);
        cats.forEach(c => {
          select.innerHTML += `<option value="${c.id}">${c.name}</option>`;
        });
      }
      document.getElementById('addProductModal')?.classList.add('show');
    });
  }

  const closeAddBtn = document.getElementById('btnCloseAddProductModal');
  if (closeAddBtn) {
    closeAddBtn.addEventListener('click', () => {
      document.getElementById('addProductModal')?.classList.remove('show');
    });
  }

  const submitAddBtn = document.getElementById('btnSubmitAddProduct');
  if (submitAddBtn) {
    submitAddBtn.addEventListener('click', async () => {
      const name = document.getElementById('addProductName')?.value.trim();
      const price = parseFloat(document.getElementById('addProductPrice')?.value);
      const categoryId = parseInt(document.getElementById('addProductCategorySelect')?.value);
      const quantity = parseInt(document.getElementById('addProductQty')?.value) || 100;
      const description = document.getElementById('addProductDesc')?.value.trim() || '';

      if (!name) {
        showToast('Tên món không được để trống!', 'warning');
        return;
      }
      if (isNaN(price) || price <= 0) {
        showToast('Giá món phải lớn hơn 0!', 'warning');
        return;
      }
      if (!categoryId) {
        showToast('Vui lòng chọn danh mục cho món!', 'warning');
        return;
      }

      try {
        await productApi.create({ name, price, categoryId, quantity, description });
        showToast(`Đã thêm món "${name}" thành công!`, 'success');

        // Xóa trắng form sau khi thêm
        document.getElementById('addProductName').value = '';
        document.getElementById('addProductPrice').value = '';
        document.getElementById('addProductDesc').value = '';
        document.getElementById('addProductQty').value = '100';

        document.getElementById('addProductModal')?.classList.remove('show');
        loadProductsAdmin();
      } catch (err) {
        showToast('Lỗi thêm món: ' + err.message, 'error');
      }
    });
  }

  // === CHỈNH SỬA MÓN ===
  const closeEditBtn = document.getElementById('btnCloseEditProductModal');
  if (closeEditBtn) {
    closeEditBtn.addEventListener('click', () => {
      document.getElementById('editProductModal')?.classList.remove('show');
    });
  }

  const submitEditBtn = document.getElementById('btnSubmitEditProduct');
  if (submitEditBtn) {
    submitEditBtn.addEventListener('click', async () => {
      const id = parseInt(document.getElementById('editProductId')?.value);
      const name = document.getElementById('editProductName')?.value.trim();
      const price = parseFloat(document.getElementById('editProductPrice')?.value);
      const categoryId = parseInt(document.getElementById('editProductCategorySelect')?.value);
      const quantity = parseInt(document.getElementById('editProductQty')?.value) || 0;
      const description = document.getElementById('editProductDesc')?.value.trim() || '';

      if (!id) {
        showToast('Mã món không hợp lệ!', 'error');
        return;
      }
      if (!name) {
        showToast('Tên món không được để trống!', 'warning');
        return;
      }
      if (isNaN(price) || price <= 0) {
        showToast('Giá món phải lớn hơn 0!', 'warning');
        return;
      }
      if (!categoryId) {
        showToast('Vui lòng chọn danh mục cho món!', 'warning');
        return;
      }

      try {
        await productApi.update(id, { name, price, categoryId, quantity, description });
        showToast(`Đã cập nhật món "${name}" thành công!`, 'success');
        document.getElementById('editProductModal')?.classList.remove('show');
        loadProductsAdmin();
      } catch (err) {
        showToast('Lỗi cập nhật món: ' + err.message, 'error');
      }
    });
  }
}
