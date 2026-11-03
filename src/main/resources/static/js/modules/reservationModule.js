import { reservationApi } from '../api/reservationApi.js';
import { tableApi } from '../api/tableApi.js';
import { formatDate } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';

let reservationsList = [];
let tablesList = [];

export async function loadReservations() {
  const tbody = document.getElementById('reservationsTableBody');
  if (tbody) {
    tbody.innerHTML = '<tr><td colspan="8" align="center" style="padding:20px; color:#64748b;">⏳ Đang tải danh sách đặt bàn...</td></tr>';
  }

  const grid = document.getElementById('resVisualTablesGrid');
  if (grid) {
    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: #94a3b8; padding: 24px;">⏳ Đang tải sơ đồ bàn ăn...</div>';
  }

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

    renderVisualTables();
    renderReservationsTable();
  } catch (error) {
    if (tbody) tbody.innerHTML = `<tr><td colspan="8" align="center" style="color:red; padding:20px;">Lỗi: ${error.message}</td></tr>`;
    if (grid) grid.innerHTML = `<div style="grid-column: 1/-1; text-align: center; color: red; padding: 24px;">Lỗi tải sơ đồ bàn: ${error.message}</div>`;
  }
}

/**
 * Hiển thị trực quan từng bàn ăn dưới dạng thẻ màu sắc chuẩn hóa:
 * - Màu xanh lá cây (Bàn trống)
 * - Màu cam (Đang phục vụ)
 * - Màu vàng viền nét đứt (Đã đặt trước)
 */
function renderVisualTables() {
  const grid = document.getElementById('resVisualTablesGrid');
  if (!grid) return;

  const total = tablesList.length;
  let countAvailable = 0;
  let countOccupied = 0;
  let countReserved = 0;

  // Xác định số lượng từng trạng thái bàn
  tablesList.forEach(t => {
    const hasActiveBooking = reservationsList.some(r => (r.tableId === t.id || r.tableNumber === t.tableNumber) && r.status === 'BOOKED');
    if (t.status === 'RESERVED' || hasActiveBooking) {
      countReserved++;
    } else if (t.status === 'OCCUPIED') {
      countOccupied++;
    } else {
      countAvailable++;
    }
  });

  const elTotal = document.getElementById('resKpiTotal');
  const elAvail = document.getElementById('resKpiAvailable');
  const elOcc = document.getElementById('resKpiOccupied');
  const elRes = document.getElementById('resKpiReserved');

  if (elTotal) elTotal.textContent = `Tổng: ${total} bàn`;
  if (elAvail) elAvail.textContent = `🟢 Bàn trống: ${countAvailable}`;
  if (elOcc) elOcc.textContent = `🟠 Đang phục vụ: ${countOccupied}`;
  if (elRes) elRes.textContent = `🟡 Đã đặt trước: ${countReserved}`;

  if (tablesList.length === 0) {
    grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; color: #94a3b8; padding: 24px;">Chưa có bàn nào trong hệ thống</div>';
    return;
  }

  // Sắp xếp bàn theo số bàn tăng dần
  const sorted = [...tablesList].sort((a, b) => (a.tableNumber || a.id) - (b.tableNumber || b.id));

  grid.innerHTML = sorted.map(t => {
    const activeBooking = reservationsList.find(r => (r.tableId === t.id || r.tableNumber === t.tableNumber) && r.status === 'BOOKED');
    const isReserved = t.status === 'RESERVED' || !!activeBooking;
    const isOccupied = !isReserved && t.status === 'OCCUPIED';

    let cardClass = 'available';
    let statusBadgeText = 'Bàn trống';
    let statusBadgeClass = 'available';
    let actionHint = '<span class="res-table-action-hint">➕ Bấm để đặt bàn</span>';
    let bookingInfoHtml = '';

    if (isReserved) {
      cardClass = 'reserved';
      statusBadgeText = 'Đã đặt trước';
      statusBadgeClass = 'reserved';
      actionHint = '<span class="res-table-action-hint" style="color:#a16207;">👁️ Bấm xem chi tiết hẹn</span>';
      if (activeBooking) {
        bookingInfoHtml = `
          <div class="res-table-booking-info">
            <div><strong>Khách:</strong> ${escapeHtml(activeBooking.customerName)}</div>
            ${activeBooking.phone ? `<div><strong>SĐT:</strong> ${escapeHtml(activeBooking.phone)}</div>` : ''}
            <div><strong>Giờ hẹn:</strong> ${formatDate(activeBooking.reservedFor)}</div>
            <div><strong>Khách:</strong> 👥 ${activeBooking.partySize || 1} người</div>
          </div>
        `;
      } else {
        bookingInfoHtml = `
          <div class="res-table-booking-info">
            <div><em>Đang giữ chỗ đặt trước</em></div>
          </div>
        `;
      }
    } else if (isOccupied) {
      cardClass = 'occupied';
      statusBadgeText = 'Đang phục vụ';
      statusBadgeClass = 'occupied';
      actionHint = '<span class="res-table-action-hint" style="color:#ea580c;">☕ Đang có khách ngồi</span>';
    }

    const tableName = t.name || `Bàn ${t.tableNumber || t.id}`;

    return `
      <div class="res-table-card ${cardClass}" data-table-id="${t.id}" data-status="${isReserved ? 'RESERVED' : (isOccupied ? 'OCCUPIED' : 'AVAILABLE')}">
        <div class="res-table-header">
          <span class="res-table-name">${tableName}</span>
          <span class="res-table-badge ${statusBadgeClass}">${statusBadgeText}</span>
        </div>
        <div class="res-table-body">
          <div style="font-weight:600; color:#475569;">👥 ${t.capacity || t.seats || 4} chỗ ngồi</div>
          ${bookingInfoHtml}
          ${actionHint}
        </div>
      </div>
    `;
  }).join('');

  // Gắn sự kiện click tương tác trên từng thẻ bàn
  grid.querySelectorAll('.res-table-card').forEach(card => {
    card.addEventListener('click', () => {
      const tableId = Number(card.dataset.tableId);
      const status = card.dataset.status;

      if (status === 'AVAILABLE') {
        // Mở modal đặt bàn và tự động chọn sẵn bàn này
        const openModalBtn = document.getElementById('btnOpenAddReservationModal');
        if (openModalBtn) openModalBtn.click();
        const tableSelect = document.getElementById('resModalTableSelect');
        if (tableSelect) {
          tableSelect.value = String(tableId);
        }
      } else if (status === 'RESERVED') {
        // Tìm thông tin đặt bàn tương ứng và lọc bảng bên dưới
        const activeBooking = reservationsList.find(r => (r.tableId === tableId || r.tableNumber === tableId) && r.status === 'BOOKED');
        if (activeBooking) {
          showToast(`Bàn ${activeBooking.tableNumber || tableId}: Khách ${activeBooking.customerName} (${formatDate(activeBooking.reservedFor)})`, 'info');
          const searchInput = document.getElementById('resSearchInput');
          if (searchInput) {
            searchInput.value = activeBooking.customerName;
            renderReservationsTable();
          }
        } else {
          showToast('Bàn đang ở trạng thái Đã đặt trước.', 'info');
        }
      } else if (status === 'OCCUPIED') {
        showToast('Bàn đang có khách phục vụ trực tiếp tại quán.', 'info');
      }
    });
  });
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
    const badgeClass = isBooked ? 'badge-tag reserved' : 'badge-tag cancelled';

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
