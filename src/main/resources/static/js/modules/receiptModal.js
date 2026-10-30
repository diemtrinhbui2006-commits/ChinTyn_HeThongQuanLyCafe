import { formatVND, formatDate } from '../utils/formatters.js';
const escapeText = text => String(text ?? '').replace(/[&<>"']/g, ch => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[ch]));

/**
 * Module hiển thị & in hóa đơn nhiệt tiêu chuẩn 80mm
 */
export function openThermalReceipt(inv, tableName) {
  const modal = document.getElementById('thermalReceiptModal');
  const paper = document.getElementById('thermalReceiptPaper');
  if (!modal || !paper || !inv) return;

  const sub = inv.subtotal != null ? inv.subtotal : (inv.totalAmount || 0);
  const dis = inv.discount != null ? inv.discount : (inv.discountAmount || 0);
  const fin = inv.totalAmount != null ? inv.totalAmount : (inv.finalAmount != null ? inv.finalAmount : (sub - dis));
  const timeVal = inv.paidAt || inv.createdAt || inv.checkoutTime || new Date();

  let rows = '';
  if (inv.details && inv.details.length > 0) {
    inv.details.forEach(d => {
      const lineTotal = d.lineTotal != null ? d.lineTotal : (d.totalPrice != null ? d.totalPrice : ((d.unitPrice || d.price || 0) * (d.quantity || 1)));
      rows += `
        <tr>
          <td>${escapeText(d.productName || 'Món #' + (d.productId || d.id || ''))}<br><small>${d.sugarPercent ?? 100}% đường · ${d.iceOption === 'NONE' ? 'Không đá' : 'Đá bình thường'}${d.note ? ' · ' + escapeText(d.note) : ''}</small></td>
          <td align="center">${d.quantity || 1}</td>
          <td align="right">${formatVND(lineTotal)}</td>
        </tr>
      `;
    });
  } else {
    rows = '<tr><td colspan="3" align="center" style="color:#64748b; padding:8px 0;">(Chưa có món nào)</td></tr>';
  }

  const invoiceNumber = inv.id ? `#${inv.id}` : '#MỚI';
  const displayTable = tableName || (inv.tableNumber ? `Bàn ${inv.tableNumber}` : (inv.tableId ? `Bàn ${inv.tableId}` : 'Mang về'));

  paper.innerHTML = `
    <h2>☕ CHINTYN COFFEE</h2>
    <p style="text-align:center; font-size:0.75rem;">Hệ Thống Quản Lý Bán Hàng</p>
    <div class="divider"></div>
    <p><strong>HÓA ĐƠN THANH TOÁN</strong></p>
    <p>Số HĐ: ${invoiceNumber} | ${displayTable}</p>
    <p>Thời gian: ${formatDate(timeVal)}</p>
    <div class="divider"></div>
    <table class="receipt-items-table">
      <thead>
        <tr style="border-bottom: 1px solid #000;">
          <th align="left">Tên món</th>
          <th align="center">SL</th>
          <th align="right">Tiền</th>
        </tr>
      </thead>
      <tbody>${rows}</tbody>
    </table>
    <div class="divider"></div>
    <div style="display:flex; justify-content:space-between;">
      <span>Tạm tính:</span><span>${formatVND(sub)}</span>
    </div>
    <div style="display:flex; justify-content:space-between;">
      <span>Giảm giá:</span><span>- ${formatVND(dis)}</span>
    </div>
    <div style="display:flex; justify-content:space-between; font-weight:bold; font-size:1.1rem; margin-top:4px;">
      <span>TỔNG TIỀN:</span><span>${formatVND(fin)}</span>
    </div>
    <div class="divider"></div>
    <p style="text-align:center; font-size:0.75rem;">Cảm ơn Quý khách & Hẹn gặp lại!</p>
    <p style="text-align:center; font-size:0.75rem;">WiFi: ChinTynCoffee - Pass: 88888888</p>
  `;

  modal.classList.add('show');
}

export function initReceiptModalEvents() {
  const closeBtn = document.getElementById('btnCloseReceiptModal');
  if (closeBtn) {
    closeBtn.addEventListener('click', () => {
      document.getElementById('thermalReceiptModal').classList.remove('show');
    });
  }

  const printBtn = document.getElementById('btnPrintReceiptBtn');
  if (printBtn) {
    printBtn.addEventListener('click', () => {
      window.print();
    });
  }
}
