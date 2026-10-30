import { shiftApi } from '../api/shiftApi.js';
import { formatVND, formatDate } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';
import { getCurrentUser } from './authModule.js';

let shiftsList = [];
let currentActiveShift = null;

export async function loadShifts() {
  const quickButton = document.getElementById('btnQuickShift');
  if (quickButton) quickButton.disabled = true;
  const tbody = document.getElementById('shiftsTableBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="9" align="center" style="padding:20px; color:#64748b;">⏳ Đang tải dữ liệu ca làm việc...</td></tr>';

  try {
    const list = await shiftApi.getAll();
    shiftsList = list || [];

    // Find active shift
    currentActiveShift = shiftsList.find(s => !s.closedAt && s.employeeId === getCurrentUser().employeeId) || null;

    updateShiftKpis();
    renderShiftsTable();
    populateActiveShiftsSelect();
    if (quickButton) quickButton.disabled = false;
    return true;
  } catch (err) {
    if (quickButton) { quickButton.disabled = false; quickButton.textContent = 'Tải lại trạng thái ca'; }
    tbody.innerHTML = `<tr><td colspan="9" align="center" style="color:red; padding:20px;">Lỗi: ${err.message}</td></tr>`;
    return false;
  }
}

function updateShiftKpis() {
  const quick = document.getElementById('btnQuickShift');
  if (quick) {
    quick.textContent = currentActiveShift ? '● Đóng ca' : '+ Mở ca';
    quick.classList.toggle('shift-is-open', !!currentActiveShift);
    quick.title = currentActiveShift ? 'Đóng ca của bạn và xem báo cáo doanh thu trong ngày' : 'Mở ca làm việc của bạn';
  }
  const statusEl = document.getElementById('shiftKpiStatus');
  const employeeEl = document.getElementById('shiftKpiEmployee');
  const openingCashEl = document.getElementById('shiftKpiOpeningCash');
  const btnToggleShift = document.getElementById('btnToggleShiftAction');

  if (currentActiveShift) {
    if (statusEl) {
      statusEl.textContent = '🟢 Đang Mở Ca';
      statusEl.style.color = '#10b981';
    }
    if (employeeEl) employeeEl.textContent = `${currentActiveShift.employeeName} (Ca #${currentActiveShift.id})`;
    if (openingCashEl) openingCashEl.textContent = formatVND(currentActiveShift.openingCash);
    if (btnToggleShift) {
      btnToggleShift.textContent = '🔴 Đóng Ca & Đối Soát Tiền';
      btnToggleShift.className = 'btn-primary';
      btnToggleShift.style.background = '#ef4444';
      btnToggleShift.onclick = () => openCloseShiftModal(currentActiveShift.id);
    }
  } else {
    if (statusEl) {
      statusEl.textContent = '⚪ Chưa Mở Ca';
      statusEl.style.color = '#64748b';
    }
    if (employeeEl) employeeEl.textContent = 'Không có ca nào đang hoạt động';
    if (openingCashEl) openingCashEl.textContent = '0 ₫';
    if (btnToggleShift) {
      btnToggleShift.textContent = '🟢 Mở Ca Làm Việc Mới';
      btnToggleShift.className = 'btn-primary';
      btnToggleShift.style.background = '#10b981';
      btnToggleShift.onclick = () => openOpenShiftModal();
    }
  }
}

function populateActiveShiftsSelect() {
  const select = document.getElementById('closeShiftModalSelect');
  if (!select) return;
  const openList = shiftsList.filter(s => !s.closedAt);
  if (openList.length === 0) {
    select.innerHTML = '<option value="">-- Hiện không có ca nào đang mở --</option>';
  } else {
    select.innerHTML = openList.map(s =>
      `<option value="${s.id}">Ca #${s.id} - ${s.employeeName} (Mở lúc: ${formatDate(s.openedAt)})</option>`
    ).join('');
  }
}

function renderShiftsTable() {
  const tbody = document.getElementById('shiftsTableBody');
  if (!tbody) return;

  if (shiftsList.length === 0) {
    tbody.innerHTML = '<tr><td colspan="9" align="center" style="padding:24px; color:#94a3b8;">Chưa có lịch sử ca làm việc nào</td></tr>';
    return;
  }

  tbody.innerHTML = shiftsList.map(s => {
    const isClosed = !!s.closedAt;
    const diff = (s.closingCash != null && s.expectedCash != null)
      ? Number(s.closingCash) - Number(s.expectedCash)
      : null;

    let diffDisplay = '--';
    if (diff != null) {
      if (diff === 0) diffDisplay = '<span style="color:#10b981; font-weight:700;">Khớp (0 ₫)</span>';
      else if (diff > 0) diffDisplay = `<span style="color:#10b981; font-weight:700;">+${formatVND(diff)} (Dư)</span>`;
      else diffDisplay = `<span style="color:#ef4444; font-weight:700;">${formatVND(diff)} (Thiếu)</span>`;
    }

    return `
      <tr>
        <td><strong>#${s.id}</strong></td>
        <td><strong>${escapeHtml(s.employeeName)}</strong></td>
        <td>${formatDate(s.openedAt)}</td>
        <td>${isClosed ? formatDate(s.closedAt) : '<span class="badge-tag pending">Đang làm việc</span>'}</td>
        <td>${formatVND(s.openingCash)}</td>
        <td style="font-weight:600;">${s.expectedCash != null ? formatVND(s.expectedCash) : '--'}</td>
        <td style="font-weight:700; color:var(--text-primary);">${s.closingCash != null ? formatVND(s.closingCash) : '--'}</td>
        <td>${diffDisplay}</td>
        <td><small style="color:#64748b;">${escapeHtml(s.note || '--')}</small>${isClosed ? `<button type="button" class="btn-action-sm" data-shift-report="${s.id}">Xem báo cáo</button>` : ''}</td>
      </tr>
    `;
  }).join('');
  tbody.querySelectorAll('[data-shift-report]').forEach(button => {
    button.addEventListener('click', () => showDailyShiftReport(button.dataset.shiftReport));
  });
}

function openOpenShiftModal() {
  const modal = document.getElementById('openShiftModal');
  const user = getCurrentUser();
  const empNameEl = document.getElementById('openShiftEmpName');
  if (empNameEl) empNameEl.textContent = user.name || user.username || 'Nhân viên';
  if (modal) modal.classList.add('show');
}

function openCloseShiftModal(shiftId = null) {
  const modal = document.getElementById('closeShiftModal');
  if (shiftId) {
    const sel = document.getElementById('closeShiftModalSelect');
    if (sel) sel.value = String(shiftId);
  }
  if (modal) modal.classList.add('show');
}

export function initShiftEvents() {
  document.getElementById('btnQuickShift').addEventListener('click', async () => {
    if (!await loadShifts()) return;
    if (currentActiveShift) openCloseShiftModal(currentActiveShift.id);
    else openOpenShiftModal();
  });
  document.getElementById('btnDismissShiftReport').addEventListener('click', () => document.getElementById('dailyShiftReportDialog').close());
  document.getElementById('btnPrintShiftReport').addEventListener('click', () => {
    document.body.classList.add('printing-shift-report');
    window.print();
  });
  window.addEventListener('afterprint', () => document.body.classList.remove('printing-shift-report'));
  // Modal Mở ca
  const openModal = document.getElementById('openShiftModal');
  const closeOpenModalBtn = document.getElementById('btnCloseOpenShiftModal');
  const cancelOpenModalBtn = document.getElementById('btnCancelOpenShiftModal');
  const formOpen = document.getElementById('formOpenShift');

  const hideOpen = () => { if (openModal) openModal.classList.remove('show'); };
  if (closeOpenModalBtn) closeOpenModalBtn.onclick = hideOpen;
  if (cancelOpenModalBtn) cancelOpenModalBtn.onclick = hideOpen;

  if (formOpen) {
    formOpen.onsubmit = async (e) => {
      e.preventDefault();
      const submit = formOpen.querySelector('[type="submit"]');
      if (submit.disabled) return;
      const openingCash = Number(document.getElementById('openShiftCashInput')?.value || 0);
      const user = getCurrentUser();
      const employeeId = user.employeeId;
      if (!employeeId) { showToast('Tài khoản chưa được gắn nhân viên để mở ca.', 'warning'); return; }

      try {
        submit.disabled = true;
        await shiftApi.open({ employeeId, openingCash });
        showToast('Đã mở ca làm việc thành công!', 'success');
        hideOpen();
        formOpen.reset();
        await loadShifts();
      } catch (err) {
        showToast(`Không thể mở ca: ${err.message}`, 'error');
      } finally {
        submit.disabled = false;
      }
    };
  }

  // Modal Đóng ca
  const closeModal = document.getElementById('closeShiftModal');
  const closeCloseModalBtn = document.getElementById('btnCloseCloseShiftModal');
  const cancelCloseModalBtn = document.getElementById('btnCancelCloseShiftModal');
  const formClose = document.getElementById('formCloseShift');

  const hideClose = () => { if (closeModal) closeModal.classList.remove('show'); };
  if (closeCloseModalBtn) closeCloseModalBtn.onclick = hideClose;
  if (cancelCloseModalBtn) cancelCloseModalBtn.onclick = hideClose;

  if (formClose) {
    formClose.onsubmit = async (e) => {
      e.preventDefault();
      const submit = formClose.querySelector('[type="submit"]');
      if (submit.disabled) return;
      const shiftId = document.getElementById('closeShiftModalSelect')?.value;
      const closingCash = Number(document.getElementById('closeShiftCashInput')?.value || 0);
      const note = document.getElementById('closeShiftNoteInput')?.value.trim();

      if (!shiftId) {
        showToast('Vui lòng chọn ca làm việc cần đóng!', 'warning');
        return;
      }

      try {
        submit.disabled = true;
        await shiftApi.close(shiftId, { closingCash, note });
        showToast('Đã đóng ca và đối soát tiền két thành công!', 'success');
        hideClose();
        formClose.reset();
        await loadShifts();
        await showDailyShiftReport(shiftId);
      } catch (err) {
        showToast(`Không thể đóng ca: ${err.message}`, 'error');
      } finally {
        submit.disabled = false;
      }
    };
  }

  document.getElementById('btnRefreshShifts')?.addEventListener('click', loadShifts);
}

async function showDailyShiftReport(shiftId) {
  try {
    const report = await shiftApi.dailyReport(shiftId);
    const shift = report.shift;
    const labels = { CASH: 'Tiền mặt', BANK_TRANSFER: 'Chuyển khoản', CARD: 'Thẻ', E_WALLET: 'Ví điện tử', OTHER: 'Khác' };
    const rows = Object.entries(report.byPaymentMethod).map(([method, amount]) => `<tr><td>${escapeHtml(labels[method] || method)}</td><td>${formatVND(amount)}</td></tr>`).join('');
    document.getElementById('dailyShiftReportContent').innerHTML = `
      <p><strong>CHINTYN COFFEE</strong></p><p>Ngày ${escapeHtml(report.date.split('-').reverse().join('/'))} · Chốt lúc ${formatDate(report.cutoff)}</p>
      <p>Ca #${shift.id} · Nhân viên: ${escapeHtml(shift.employeeName)}</p>
      <p class="daily-report-scope">Doanh thu toàn quán trong ngày, từ 00:00 đến thời điểm đóng ca. Chỉ tính hóa đơn đã thanh toán.</p>
      <table><tbody><tr><td>Số hóa đơn đã thanh toán</td><td>${report.invoiceCount}</td></tr><tr><td>Tiền món trước giảm giá</td><td>${formatVND(report.gross)}</td></tr><tr><td>Giảm giá</td><td>${formatVND(report.discount)}</td></tr><tr class="report-total"><td>DOANH THU</td><td>${formatVND(report.revenue)}</td></tr>${rows}</tbody></table>
      <h3>Đối soát tiền mặt của ca #${shift.id}</h3><table><tbody><tr><td>Tiền đầu ca</td><td>${formatVND(shift.openingCash)}</td></tr><tr><td>Tiền mặt dự kiến trong két</td><td>${formatVND(shift.expectedCash)}</td></tr><tr><td>Tiền kiểm kê khi đóng ca</td><td>${formatVND(shift.closingCash)}</td></tr><tr><td>Chênh lệch</td><td>${formatVND(Number(shift.closingCash) - Number(shift.expectedCash))}</td></tr></tbody></table>
      <p>Ghi chú: ${escapeHtml(shift.note || 'Không có')}</p>`;
    document.getElementById('dailyShiftReportDialog').showModal();
  } catch (error) {
    showToast('Không tải được báo cáo ca đã đóng. Có thể mở lại ở Ca & đối soát: ' + error.message, 'error');
  }
}

function escapeHtml(text) {
  return String(text || '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
