import { tableApi } from '../api/tableApi.js';
import { showToast } from '../utils/toast.js';

/**
 * Module Quản Lý Bàn Ăn (Tables)
 */
export async function loadTablesAdmin() {
  const tbody = document.getElementById('tablesAdminBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="4" align="center">Đang tải...</td></tr>';

  try {
    const list = await tableApi.getAll();
    tbody.innerHTML = '';

    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="4" align="center">Chưa có bàn nào</td></tr>';
      return;
    }

    list.forEach(t => {
      const isOccupied = t.status === 'OCCUPIED';
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>#${t.id}</strong></td>
        <td>${t.name}</td>
        <td><span class="badge-tag ${isOccupied ? 'occupied' : 'active'}">${isOccupied ? 'Đang dùng' : 'Trống'}</span></td>
        <td><button class="btn-action-sm danger btn-delete-table" data-id="${t.id}">Xóa</button></td>
      `;
      tbody.appendChild(tr);
    });

    tbody.querySelectorAll('.btn-delete-table').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.id;
        if (!confirm('Bạn có chắc muốn xóa bàn này?')) return;
        try {
          await tableApi.delete(id);
          showToast('Đã xóa bàn thành công', 'success');
          loadTablesAdmin();
        } catch (err) {
          showToast('Lỗi xóa bàn: ' + err.message, 'error');
        }
      });
    });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="4" style="color:red;">Lỗi: ${err.message}</td></tr>`;
  }
}

export function initTableAdminEvents() {
  const addBtn = document.getElementById('btnAddTableSubmit');
  const input = document.getElementById('addTableNameInput');
  if (addBtn && input) {
    addBtn.addEventListener('click', async () => {
      const name = input.value.trim();
      if (!name) {
        showToast('Vui lòng nhập tên bàn!', 'warning');
        return;
      }
      try {
        await tableApi.create({ name, status: 'AVAILABLE' });
        input.value = '';
        showToast('Đã thêm bàn mới!', 'success');
        loadTablesAdmin();
      } catch (err) {
        showToast('Lỗi thêm bàn: ' + err.message, 'error');
      }
    });
  }
}
