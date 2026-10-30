import { formatVND, formatDate } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';
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
  const transfer = ['BANK_TRANSFER', 'E_WALLET'].includes(inv.paymentMethod) && Number(fin) > 0;
  const methodNames = { CASH: 'Tiền mặt', BANK_TRANSFER: 'Chuyển khoản Techcombank', E_WALLET: 'Ví điện tử', CARD: 'Thẻ' };
  const paymentQr = transfer ? `<div class="receipt-payment-qr"><p><strong>${methodNames[inv.paymentMethod]}</strong></p><img id="receiptPaymentQr" src="/api/invoices/${inv.id}/payment-qr?method=${inv.paymentMethod}&amp;v=${encodeURIComponent(fin)}" alt="QR chuyển khoản hóa đơn ${inv.id}" width="230" height="230"><p>Số tiền: <strong>${formatVND(fin)}</strong></p><p>Nội dung: HD${inv.id}</p><p class="receipt-paid-note">${inv.status === 'PAID' ? 'ĐÃ THANH TOÁN · Không chuyển tiền lần nữa' : 'Quét mã để điền sẵn số tiền hóa đơn'}</p><p id="receiptQrStatus" role="status">Đang tải QR...</p></div>` : `<p>Phương thức: ${methodNames[inv.paymentMethod] || 'Chưa thanh toán'}</p>`;

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
    ${paymentQr}
    <div class="divider"></div>
    <p style="text-align:center; font-size:0.75rem;">Cảm ơn Quý khách & Hẹn gặp lại!</p>
    <p style="text-align:center; font-size:0.75rem;">WiFi: ChinTynCoffee - Pass: 88888888</p>
  `;

  const qrImage = document.getElementById('receiptPaymentQr');
  const printButton = document.getElementById('btnPrintReceiptBtn');
  printButton.disabled = !!qrImage;
  if (qrImage) {
    qrImage.onload = () => {
      document.getElementById('receiptQrStatus').textContent = '';
      printButton.disabled = false;
    };
    qrImage.onerror = () => {
      document.getElementById('receiptQrStatus').textContent = 'Không tải được QR. Hãy đóng và mở lại hóa đơn trước khi in.';
    };
    if (qrImage.complete && qrImage.naturalWidth > 0) qrImage.onload();
  }

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
    printBtn.addEventListener('click', async () => {
      try {
        const qr = document.getElementById('receiptPaymentQr');
        if (qr) await qr.decode();
        window.print();
      } catch {
        showToast('QR chưa tải xong. Vui lòng mở lại hóa đơn trước khi in.', 'error');
      }
    });
  }
}
