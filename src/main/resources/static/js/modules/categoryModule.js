import { categoryApi } from '../api/categoryApi.js';
import { showToast } from '../utils/toast.js';
import { completeManagementForm, managementFormError } from './managementForms.js';

/**
 * Module Quản Lý Danh Mục Món (Categories)
 */
export async function loadCategoriesAdmin() {
  const tbody = document.getElementById('categoriesAdminBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="3" align="center">Đang tải...</td></tr>';

  try {
    const list = await categoryApi.getAll();
    tbody.innerHTML = '';

    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="3" align="center">Chưa có danh mục nào</td></tr>';
      return;
    }

    list.forEach(c => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>#${c.id}</strong></td>
        <td>${c.name}</td>
        <td><span class="badge-tag active">Hoạt động</span></td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="3" style="color:red;">Lỗi: ${err.message}</td></tr>`;
  }
}

export function initCategoryAdminEvents() {
  const btn = document.getElementById('btnAddCategorySubmit');
  const input = document.getElementById('addCategoryNameInput');
  if (btn && input) {
    btn.addEventListener('click', async () => {
      if (btn.disabled) return;
      const name = input.value.trim();
      if (!name) {
        showToast('Vui lòng nhập tên danh mục!', 'warning');
        return;
      }
      try {
        btn.disabled = true;
        await categoryApi.create({ name });
        input.value = '';
        completeManagementForm('btnAddCategorySubmit');
        showToast('Đã thêm danh mục mới!', 'success');
        loadCategoriesAdmin();
      } catch (err) {
        managementFormError('btnAddCategorySubmit', err.message);
        showToast('Lỗi thêm danh mục: ' + err.message, 'error');
      } finally {
        btn.disabled = false;
      }
    });
  }
}
