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
        <td><strong>Bàn ${t.tableNumber || t.id}</strong> <span style="color:#64748b; font-size:0.8rem;">(${t.capacity || 4} chỗ)</span></td>
        <td><span class="badge-tag ${isOccupied ? 'occupied' : 'active'}">${isOccupied ? 'Đang dùng' : 'Trống'}</span></td>
        <td><button class="btn-action-sm danger btn-delete-table" data-id="${t.id}">Xóa</button></td>
      `;
      tbody.appendChild(tr);
    });

    tbody.querySelectorAll('.btn-delete-table').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.id;
        if (!confirm('Bạn có chắc muốn xóa bàn này khỏi CSDL?')) return;
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
      const raw = input.value.trim();
      if (!raw) {
        showToast('Vui lòng nhập số bàn (ví dụ: 11 hoặc Bàn 11)!', 'warning');
        return;
      }
      const numMatch = raw.match(/\d+/);
      const tableNumber = numMatch ? parseInt(numMatch[0]) : parseInt(raw);
      if (!tableNumber || isNaN(tableNumber) || tableNumber <= 0) {
        showToast('Số bàn phải là số nguyên lớn hơn 0!', 'warning');
        return;
      }
      try {
        await tableApi.create({ tableNumber, capacity: 4, status: 'AVAILABLE' });
        input.value = '';
        showToast(`Đã thêm Bàn ${tableNumber} vào CSDL thành công!`, 'success');
        loadTablesAdmin();
      } catch (err) {
        showToast('Lỗi thêm bàn: ' + err.message, 'error');
      }
    });
  }
}
