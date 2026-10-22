import { invoiceApi } from '../api/invoiceApi.js';
import { formatVND, formatDate } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';
import { openThermalReceipt } from './receiptModal.js';

/**
 * Module Lịch Sử Đơn Hàng (Orders)
 */
export async function loadOrdersHistory() {
  const tbody = document.getElementById('ordersTableBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="6" align="center">Đang tải...</td></tr>';

  try {
    const list = await invoiceApi.getAll();
    tbody.innerHTML = '';

    if (!list || list.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" align="center">Chưa có hóa đơn nào</td></tr>';
      return;
    }

    list.forEach(inv => {
      const isPaid = inv.status === 'PAID';
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>#${inv.id}</strong></td>
        <td>Bàn ${inv.tableId || '--'}</td>
        <td>${formatDate(inv.createdAt || inv.checkoutTime)}</td>
        <td style="font-weight:700;">${formatVND(inv.finalAmount || inv.totalAmount)}</td>
        <td><span class="badge-tag ${isPaid ? 'paid' : (inv.status === 'CANCELLED' ? 'cancelled' : 'pending')}">${inv.status}</span></td>
        <td><button class="btn-action-sm btn-view-invoice" data-id="${inv.id}">Xem & In</button></td>
      `;
      tbody.appendChild(tr);
    });

    tbody.querySelectorAll('.btn-view-invoice').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.dataset.id;
        try {
          const inv = await invoiceApi.getById(id);
          openThermalReceipt(inv, `Bàn ${inv.tableId || '--'}`);
        } catch (err) {
          showToast('Lỗi xem hóa đơn: ' + err.message, 'error');
        }
      });
    });
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="color:red;">Lỗi: ${err.message}</td></tr>`;
  }
}
