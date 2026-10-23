import { invoiceApi } from '../api/invoiceApi.js';
import { formatVND, formatDate } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';
import { openThermalReceipt } from './receiptModal.js';

/**
 * Module Quản Lý & Lịch Sử Đơn Hàng (Orders)
 */
export async function loadOrdersHistory() {
  const tbody = document.getElementById('ordersTableBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="6" align="center" style="padding:20px; color:#64748b;">⏳ Đang tải danh sách đơn hàng...</td></tr>';

  try {
    const list = await invoiceApi.getAll();
    tbody.innerHTML = '';

    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" align="center" style="padding:20px; color:#94a3b8;">Chưa có hóa đơn nào</td></tr>';
      return;
    }

    // Sắp xếp đơn hàng mới nhất lên trên cùng (id giảm dần)
    const sortedList = [...list].sort((a, b) => (b.id - a.id));

    sortedList.forEach(inv => {
      const isPaid = inv.status === 'PAID';
      const isCancelled = inv.status === 'CANCELLED';
      const statusText = isPaid ? 'Đã thanh toán' : (isCancelled ? 'Đã hủy' : 'Đang phục vụ');
      const statusClass = isPaid ? 'paid' : (isCancelled ? 'cancelled' : 'pending');
      const tableName = inv.tableNumber ? `Bàn ${inv.tableNumber}` : (inv.tableId ? `Bàn ${inv.tableId}` : 'Mang về');
      const total = inv.totalAmount != null ? inv.totalAmount : (inv.finalAmount != null ? inv.finalAmount : (inv.subtotal || 0));

      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>#${inv.id}</strong></td>
        <td>${tableName}</td>
        <td>${formatDate(inv.paidAt || inv.createdAt)}</td>
        <td style="font-weight:700; color:#d97706;">${formatVND(total)}</td>
        <td><span class="badge-tag ${statusClass}">${statusText}</span></td>
        <td><button class="btn-action-sm btn-view-invoice" data-id="${inv.id}" title="Xem hóa đơn và in lại">🖨️ Xem & In</button></td>
      `;
      tbody.appendChild(tr);
    });

    tbody.querySelectorAll('.btn-view-invoice').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.id;
        try {
          const inv = await invoiceApi.getById(id);
          const tName = inv.tableNumber ? `Bàn ${inv.tableNumber}` : (inv.tableId ? `Bàn ${inv.tableId}` : 'Mang về');
          openThermalReceipt(inv, tName);
        } catch (err) {
          showToast('Lỗi xem hóa đơn: ' + err.message, 'error');
        }
      });
    });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="color:red; text-align:center; padding:16px;">Lỗi tải lịch sử đơn hàng: ${err.message}</td></tr>`;
  }
}
