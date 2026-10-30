import { reservationApi } from '../api/reservationApi.js';
import { tableApi } from '../api/tableApi.js';
import { formatDate } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';

let reservationsList = [];
let tablesList = [];

export async function loadReservations() {
  const tbody = document.getElementById('reservationsTableBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="8" align="center" style="padding:20px; color:#64748b;">⏳ Đang tải danh sách đặt bàn...</td></tr>';

  try {
    const [resData, tabData] = await Promise.all([
      reservationApi.getAll().catch(() => []),
      tableApi.getAll().catch(() => []),
    ]);

    reservationsList = resData || [];
    tablesList = tabData || [];

    // Populate table options in modal
    const tableSelect = document.getElementById('resModalTableSelect');
    if (tableSelect) {
      tableSelect.innerHTML = '<option value="">-- Chọn bàn ăn --</option>' +
        tablesList
          .filter(t => t.status === 'AVAILABLE')
          .map(t => `<option value="${t.id}">Bàn ${t.tableNumber || t.id} (${t.capacity || t.seats || 4} ghế)</option>`)
          .join('');
    }

    renderReservationsTable();
  } catch (error) {
    tbody.innerHTML = `<tr><td colspan="8" align="center" style="color:red; padding:20px;">Lỗi: ${error.message}</td></tr>`;
  }
}

function renderReservationsTable() {
  const tbody = document.getElementById('reservationsTableBody');
  if (!tbody) return;

  const filterStatus = document.getElementById('resFilterStatus')?.value || 'ALL';
  const keyword = (document.getElementById('resSearchInput')?.value || '').trim().toLowerCase();

  const filtered = reservationsList.filter(r => {
    const matchStatus = filterStatus === 'ALL' || r.status === filterStatus;
    const matchKw = !keyword || (r.customerName && r.customerName.toLowerCase().includes(keyword)) ||
      (r.phone && r.phone.includes(keyword)) || String(r.tableNumber).includes(keyword);
    return matchStatus && matchKw;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" align="center" style="padding:24px; color:#94a3b8;">Không có thông tin đặt bàn nào</td></tr>';
    return;
  }

  tbody.innerHTML = filtered.map(r => {
    const isBooked = r.status === 'BOOKED';
    const statusText = isBooked ? 'Đã đặt trước' : (r.status === 'CANCELLED' ? 'Đã hủy' : r.status);
    const badgeClass = isBooked ? 'badge-tag active' : 'badge-tag cancelled';

    return `
      <tr>
        <td><strong>#${r.id}</strong></td>
        <td><span style="font-weight:700; color:var(--primary-accent-hover, #d97706);">Bàn ${r.tableNumber || r.tableId}</span></td>
        <td><strong>${escapeHtml(r.customerName)}</strong></td>
        <td>${escapeHtml(r.phone || '--')}</td>
        <td>${formatDate(r.reservedFor)}</td>
        <td>👥 ${r.partySize || 1} người</td>
        <td><span class="${badgeClass}">${statusText}</span></td>
        <td>
          ${isBooked ? `<button class="btn-action-sm btn-cancel-res" data-id="${r.id}" style="color:#ef4444; border-color:#fecaca;">❌ Hủy bàn</button>` : '<span style="color:#94a3b8; font-size:0.8rem;">Đã xử lý</span>'}
        </td>
      </tr>
    `;
  }).join('');

  // Bind cancel buttons
  tbody.querySelectorAll('.btn-cancel-res').forEach(btn => {
    btn.onclick = async () => {
      const id = btn.dataset.id;
      if (!confirm(`Xác nhận hủy đặt bàn #${id}? Bàn sẽ được giải phóng về trạng thái Trống.`)) return;
      try {
        await reservationApi.cancel(id);
        showToast(`Đã hủy đặt bàn #${id} thành công!`, 'warning');
        await loadReservations();
      } catch (err) {
        showToast(`Lỗi hủy đặt bàn: ${err.message}`, 'error');
      }
    };
  });
}

export function initReservationEvents() {
  const openModalBtn = document.getElementById('btnOpenAddReservationModal');
  const modal = document.getElementById('addReservationModal');
  const closeModalBtn = document.getElementById('btnCloseReservationModal');
  const cancelModalBtn = document.getElementById('btnCancelReservationModal');
  const form = document.getElementById('formAddReservation');

  if (openModalBtn) {
    openModalBtn.onclick = () => {
      if (modal) modal.classList.add('show');
      // Set default time to now + 2 hours
      const dateInput = document.getElementById('resModalDateTime');
      if (dateInput && !dateInput.value) {
        const d = new Date(Date.now() + 2 * 60 * 60 * 1000);
        d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
        dateInput.value = d.toISOString().slice(0, 16);
      }
    };
  }

  const hideModal = () => { if (modal) modal.classList.remove('show'); };
  if (closeModalBtn) closeModalBtn.onclick = hideModal;
  if (cancelModalBtn) cancelModalBtn.onclick = hideModal;

  if (form) {
    form.onsubmit = async (e) => {
      e.preventDefault();
      const tableId = document.getElementById('resModalTableSelect')?.value;
      const customerName = document.getElementById('resModalCustomerName')?.value.trim();
      const phone = document.getElementById('resModalCustomerPhone')?.value.trim();
      const reservedFor = document.getElementById('resModalDateTime')?.value;
      const partySize = Number(document.getElementById('resModalPartySize')?.value || 2);
      const note = document.getElementById('resModalNote')?.value.trim();

      if (!tableId || !customerName || !reservedFor) {
        showToast('Vui lòng điền đầy đủ bàn, tên khách và thời gian đặt!', 'warning');
        return;
      }

      try {
        await reservationApi.create({
          tableId: Number(tableId),
          customerName,
          phone,
          reservedFor,
          partySize,
          note,
        });
        showToast('Đặt bàn thành công! Trạng thái bàn đã chuyển sang Đặt Trước.', 'success');
        hideModal();
        form.reset();
        await loadReservations();
      } catch (err) {
        showToast(`Không thể tạo đặt bàn: ${err.message}`, 'error');
      }
    };
  }

  // Filter events
  document.getElementById('resFilterStatus')?.addEventListener('change', renderReservationsTable);
  document.getElementById('resSearchInput')?.addEventListener('input', renderReservationsTable);
  document.getElementById('btnRefreshReservations')?.addEventListener('click', loadReservations);
}

function escapeHtml(text) {
  return String(text || '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
