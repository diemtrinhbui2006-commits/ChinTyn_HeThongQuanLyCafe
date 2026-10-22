import { userApi } from '../api/userApi.js';

/**
 * Module Nhân Viên & Tài Khoản (Users)
 */
export async function loadUsersAdmin() {
  const tbody = document.getElementById('usersAdminBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="6" align="center">Đang tải dữ liệu từ CSDL...</td></tr>';

  try {
    const [users, employees] = await Promise.all([
      userApi.getAllUsers().catch(() => []),
      userApi.getAllEmployees().catch(() => []),
    ]);

    tbody.innerHTML = '';

    if (!users || users.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" align="center">Chưa có tài khoản nào trong CSDL</td></tr>';
      return;
    }

    const empMap = {};
    (employees || []).forEach(emp => {
      empMap[emp.id] = emp;
    });

    users.forEach(u => {
      const emp = u.employeeId ? empMap[u.employeeId] : null;
      const empName = emp ? emp.name : '(Chưa liên kết)';
      const empPos = emp ? (emp.position || '--') : '--';
      const contact = emp ? `${emp.phone || ''} ${emp.email ? `(${emp.email})` : ''}` : '--';

      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>#${u.id}</strong></td>
        <td><strong>${u.username}</strong></td>
        <td>${empName}</td>
        <td><span style="background:#e0f2fe; color:#0369a1; padding:2px 8px; border-radius:4px; font-size:0.8rem; font-weight:600;">${empPos}</span></td>
        <td style="font-size:0.85rem; color:#64748b;">${contact}</td>
        <td><span class="badge-tag active">${u.role || 'STAFF'}</span></td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="color:red;">Lỗi: ${err.message}</td></tr>`;
  }
}
