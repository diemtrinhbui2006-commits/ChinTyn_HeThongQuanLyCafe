import { invoiceApi } from '../api/invoiceApi.js';
import { tableApi } from '../api/tableApi.js';
import { productApi } from '../api/productApi.js';
import { formatVND, formatDate } from '../utils/formatters.js';

/**
 * Module Thống Kê & Tổng Quan Dashboard
 */
export async function loadDashboard() {
  try {
    const [invoices, tables, products] = await Promise.all([
      invoiceApi.getAll().catch(() => []),
      tableApi.getAll().catch(() => []),
      productApi.getAll().catch(() => []),
    ]);

    // 1. Tính toán 4 chỉ số KPI
    const paidInvoices = invoices.filter(i => i.status === 'PAID');
    const totalRev = paidInvoices.reduce((sum, i) => sum + (i.totalAmount || i.finalAmount || 0), 0);
    const activeTables = tables.filter(t => t.status === 'OCCUPIED').length;

    const revEl = document.getElementById('kpiRevenueToday');
    const countEl = document.getElementById('kpiOrdersCount');
    const tablesEl = document.getElementById('kpiActiveTables');
    const prodEl = document.getElementById('kpiTotalProducts');

    if (revEl) revEl.textContent = formatVND(totalRev);
    if (countEl) countEl.textContent = paidInvoices.length;
    if (tablesEl) tablesEl.textContent = `${activeTables} / ${tables.length}`;
    if (prodEl) prodEl.textContent = products.length;

    // 2. Render bảng đơn hàng gần nhất (sắp xếp mới nhất lên trước)
    const tbody = document.getElementById('dashboardRecentOrdersBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    const recent = [...invoices].sort((a, b) => b.id - a.id).slice(0, 6);
    if (recent.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" align="center" style="color:#94a3b8; padding:16px;">Chưa có đơn hàng nào</td></tr>';
      return;
    }

    recent.forEach(inv => {
      const isPaid = inv.status === 'PAID';
      const isCancelled = inv.status === 'CANCELLED';
      const statusText = isPaid ? 'Đã thanh toán' : (isCancelled ? 'Đã hủy' : 'Đang phục vụ');
      const statusClass = isPaid ? 'paid' : (isCancelled ? 'cancelled' : 'pending');
      const tr = document.createElement('tr');
      tr.innerHTML = `
        <td><strong>#${inv.id}</strong></td>
        <td>Bàn ${inv.tableNumber || inv.tableId || '--'}</td>
        <td>${formatDate(inv.paidAt || inv.createdAt)}</td>
        <td style="font-weight:700; color:#d97706;">${formatVND(inv.totalAmount || inv.finalAmount)}</td>
        <td><span class="badge-tag ${statusClass}">${statusText}</span></td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    console.error('Lỗi nạp Dashboard:', err);
  }
}
