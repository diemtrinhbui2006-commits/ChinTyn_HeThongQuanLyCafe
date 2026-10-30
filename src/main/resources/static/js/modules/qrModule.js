import { qrApi } from '../api/qrApi.js';
import { tableApi } from '../api/tableApi.js';
import { productApi } from '../api/productApi.js';
import { formatDate } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';
import { getCurrentUser } from './authModule.js';

let tablesList = [];
let productsMap = new Map();
let pendingOrdersList = [];
let currentGeneratedQr = null;

export async function loadQrManagement() {
  try {
    const [tabData, prodData, pendingData] = await Promise.all([
      tableApi.getAll().catch(() => []),
      productApi.getAll().catch(() => []),
      qrApi.getPendingOrders().catch(() => []),
    ]);

    tablesList = tabData || [];
    productsMap = new Map((prodData || []).map(p => [p.id, p.name]));
    pendingOrdersList = pendingData || [];

    populateQrTablesDropdown();
    renderPendingQueueTable();
  } catch (err) {
    showToast(`Lỗi tải dữ liệu QR: ${err.message}`, 'error');
  }
}

function populateQrTablesDropdown() {
  const select = document.getElementById('qrTableSelect');
  if (!select) return;
  select.innerHTML = '<option value="">-- Chọn bàn cần tạo QR --</option>' +
    tablesList.map(t => `<option value="${t.id}">Bàn ${t.tableNumber || t.id} (${t.status === 'AVAILABLE' ? 'Trống' : 'Có khách'})</option>`).join('');
}

function renderPendingQueueTable() {
  const tbody = document.getElementById('qrPendingOrdersTableBody');
  const badgeEl = document.getElementById('qrPendingCountBadge');
  if (!tbody) return;

  if (badgeEl) badgeEl.textContent = `${pendingOrdersList.length} yêu cầu`;

  if (pendingOrdersList.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" align="center" style="padding:24px; color:#94a3b8;">Không có yêu cầu gọi món QR nào đang chờ</td></tr>';
    return;
  }

  tbody.innerHTML = pendingOrdersList.map(order => {
    let itemsText = '';
    try {
      const items = JSON.parse(order.itemsJson || '[]');
      itemsText = items.map(i => {
        const pName = productsMap.get(i.productId) || `Món #${i.productId}`;
        return `<strong>${escapeHtml(pName)}</strong> × ${i.quantity}`;
      }).join(', ');
    } catch {
      itemsText = escapeHtml(order.itemsJson || '--');
    }

    return `
      <tr>
        <td><strong>#${order.id}</strong></td>
        <td><span style="font-weight:700; color:var(--primary-accent-hover, #d97706); font-size:1rem;">Bàn ${order.tableNumber}</span></td>
        <td>
          <strong>${escapeHtml(order.guestName || 'Khách tại bàn')}</strong>
          ${order.guestPhone ? `<br><small style="color:#64748b;">📞 ${escapeHtml(order.guestPhone)}</small>` : ''}
        </td>
        <td>${itemsText || '--'}</td>
        <td>${formatDate(order.createdAt)}</td>
        <td>
          <button class="btn-primary btn-approve-qr" data-id="${order.id}" style="padding:6px 12px; font-size:0.8rem; background:#10b981;">
            ✅ Duyệt Vào Bàn
          </button>
        </td>
      </tr>
    `;
  }).join('');

  tbody.querySelectorAll('.btn-approve-qr').forEach(btn => {
    btn.onclick = async () => {
      const orderId = btn.dataset.id;
      const user = getCurrentUser();
      const employeeId = user.employeeId || 1;

      try {
        await qrApi.approveOrder(orderId, employeeId);
        showToast(`Đã duyệt đơn gọi món #${orderId} vào bàn thành công!`, 'success');
        await loadQrManagement();
      } catch (err) {
        showToast(`Lỗi duyệt đơn: ${err.message}`, 'error');
      }
    };
  });
}

export function initQrEvents() {
  const btnCreateQr = document.getElementById('btnGenerateTableQr');
  const qrDisplayBox = document.getElementById('qrCodeDisplayBox');

  if (btnCreateQr) {
    btnCreateQr.onclick = async () => {
      const tableId = document.getElementById('qrTableSelect')?.value;
      if (!tableId) {
        showToast('Vui lòng chọn bàn cần tạo mã QR!', 'warning');
        return;
      }

      try {
        const res = await qrApi.createToken(tableId);
        currentGeneratedQr = res;
        const fullUrl = `${location.origin}${res.url}`;
        const imageUrl = qrApi.getQrImageUrl(res.token);

        const targetTable = tablesList.find(t => t.id === Number(tableId));
        const tableName = targetTable ? `Bàn ${targetTable.tableNumber || targetTable.id}` : `Bàn #${tableId}`;

        if (qrDisplayBox) {
          qrDisplayBox.style.display = 'block';
          qrDisplayBox.innerHTML = `
            <div style="background:#fff; border:1px solid #e2e8f0; border-radius:12px; padding:20px; text-align:center; max-width:360px; margin:0 auto; box-shadow:0 4px 6px -1px rgb(0 0 0 / 0.1);">
              <h3 style="font-size:1.15rem; font-weight:800; color:var(--text-primary); margin-bottom:4px;">☕ CHINTYN COFFEE</h3>
              <p style="font-size:0.9rem; font-weight:700; color:var(--primary-accent-hover, #d97706);">${tableName}</p>
              <div style="margin:16px 0; display:inline-block; padding:10px; border:2px dashed #cbd5e1; border-radius:12px; background:#f8fafc;">
                <img src="${imageUrl}" alt="QR Bàn ${tableName}" style="width:200px; height:200px; display:block; margin:0 auto;" />
              </div>
              <p style="font-size:0.8rem; color:#64748b; margin-bottom:12px;">Quét mã trên để mở thực đơn và gọi món trực tiếp</p>
              <div style="display:flex; gap:8px; justify-content:center;">
                <a href="${fullUrl}" target="_blank" class="btn-action-sm" style="text-decoration:none; display:inline-flex; align-items:center; gap:4px;">
                  🔗 Mở Thử Link
                </a>
                <button type="button" class="btn-primary" onclick="window.print()" style="padding:6px 12px; font-size:0.8rem;">
                  🖨️ In Mã QR
                </button>
              </div>
            </div>
          `;
        }

        showToast(`Đã tạo mã QR cho ${tableName}!`, 'success');
      } catch (err) {
        showToast(`Không thể tạo mã QR: ${err.message}`, 'error');
      }
    };
  }

  document.getElementById('btnRefreshQrQueue')?.addEventListener('click', loadQrManagement);
}

function escapeHtml(text) {
  return String(text || '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
