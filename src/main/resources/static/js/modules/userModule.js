import { userApi } from '../api/userApi.js';

/**
 * Module Nhân Viên & Tài Khoản (Users)
 */
export async function loadUsersAdmin() {
  const tbody = document.getElementById('usersAdminBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="4" align="center">Đang tải...</td></tr>';

  try {
    const list = await userApi.getAllUsers();
    tbody.innerHTML = '';

    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="4" align="center">Chưa có tài khoản nào</td></tr>';
      return;
    }

    list.forEach(u => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>#${u.id}</strong></td>
        <td>${u.username}</td>
        <td><span class="badge-tag active">${u.role || 'STAFF'}</span></td>
        <td>Nhân viên #${u.employeeId || '--'}</td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="4" style="color:red;">Lỗi: ${err.message}</td></tr>`;
  }
}
