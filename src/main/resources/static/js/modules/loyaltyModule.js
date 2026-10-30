import { loyaltyApi } from '../api/loyaltyApi.js';
import { formatDate } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';

let customersList = [];
let rewardsList = [];
let selectedCustomerLoyalty = null;
let currentCustomerId = null;

export async function loadLoyalty() {
  try {
    const [custList, rewList] = await Promise.all([
      loyaltyApi.getCustomers().catch(() => []),
      loyaltyApi.getRewards().catch(() => []),
    ]);

    customersList = custList || [];
    rewardsList = rewList || [];

    populateCustomerDropdown();
    renderRewardsTable();

    // If a customer was previously selected, reload their loyalty data
    if (currentCustomerId) {
      await loadCustomerLoyaltyData(currentCustomerId);
    }
  } catch (err) {
    showToast(`Lỗi tải dữ liệu điểm thưởng: ${err.message}`, 'error');
  }
}

function populateCustomerDropdown() {
  const select = document.getElementById('loyaltyCustomerSelect');
  if (!select) return;
  select.innerHTML = '<option value="">-- Chọn khách hàng cần kiểm tra --</option>' +
    customersList.map(c => `<option value="${c.id}">${escapeHtml(c.name)} (${c.phone || 'Chưa có SĐT'})</option>`).join('');
  if (currentCustomerId) select.value = String(currentCustomerId);
}

export async function loadCustomerLoyaltyData(customerId) {
  currentCustomerId = customerId;
  const historyTbody = document.getElementById('loyaltyHistoryTableBody');
  const balanceEl = document.getElementById('loyaltyPointsBalance');
  const custNameEl = document.getElementById('loyaltyCustomerNameDisplay');
  const custTierEl = document.getElementById('loyaltyCustomerTierDisplay');
  const btnRedeem = document.getElementById('btnOpenRedeemRewardModal');

  if (!customerId) {
    if (balanceEl) balanceEl.textContent = '0 ⭐';
    if (custNameEl) custNameEl.textContent = 'Vui lòng chọn khách hàng';
    if (custTierEl) custTierEl.textContent = '--';
    if (historyTbody) historyTbody.innerHTML = '<tr><td colspan="4" align="center" style="padding:20px; color:#94a3b8;">Chọn một khách hàng ở trên để xem lịch sử</td></tr>';
    if (btnRedeem) btnRedeem.disabled = true;
    return;
  }

  const cust = customersList.find(c => c.id === Number(customerId));
  if (custNameEl && cust) custNameEl.textContent = `${cust.name} (${cust.phone || 'SĐT: --'})`;

  try {
    const data = await loyaltyApi.getCustomerLoyalty(customerId);
    selectedCustomerLoyalty = data;
    const balance = data.balance || 0;

    if (balanceEl) balanceEl.textContent = `${balance.toLocaleString('vi-VN')} ⭐`;
    if (btnRedeem) btnRedeem.disabled = balance <= 0;

    // Calculate tier
    let tier = 'Thành Viên Mới';
    if (balance >= 500) tier = '👑 Kim Cương';
    else if (balance >= 200) tier = '🥇 Vàng';
    else if (balance >= 50) tier = '🥈 Bạc';
    if (custTierEl) custTierEl.textContent = tier;

    if (!historyTbody) return;
    if (!data.history || data.history.length === 0) {
      historyTbody.innerHTML = '<tr><td colspan="4" align="center" style="padding:20px; color:#94a3b8;">Khách hàng chưa có lịch sử tích/đổi điểm</td></tr>';
      return;
    }

    historyTbody.innerHTML = data.history.map(item => {
      const isAdd = item.points > 0;
      const pointColor = isAdd ? '#10b981' : '#ef4444';
      const pointText = isAdd ? `+${item.points}` : `${item.points}`;

      return `
        <tr>
          <td>${formatDate(item.createdAt)}</td>
          <td><span class="badge-tag ${isAdd ? 'paid' : 'pending'}">${escapeHtml(item.entryType || 'Giao dịch')}</span></td>
          <td style="font-weight:700; color:${pointColor}; font-size:0.95rem;">${pointText} ⭐</td>
          <td>${escapeHtml(item.description || '--')}</td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    if (historyTbody) historyTbody.innerHTML = `<tr><td colspan="4" align="center" style="color:red; padding:16px;">Lỗi: ${err.message}</td></tr>`;
  }
}

function renderRewardsTable() {
  const tbody = document.getElementById('rewardsTableBody');
  if (!tbody) return;

  if (rewardsList.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" align="center" style="padding:24px; color:#94a3b8;">Chưa có phần thưởng nào được thiết lập</td></tr>';
    return;
  }

  tbody.innerHTML = rewardsList.map(r => `
    <tr>
      <td><strong>#${r.id}</strong></td>
      <td><strong style="color:var(--text-primary);">${escapeHtml(r.name)}</strong></td>
      <td><span style="font-weight:800; color:var(--primary-accent-hover, #d97706);">${r.pointsCost} ⭐</span></td>
      <td><small style="color:#64748b;">${escapeHtml(r.description || '--')}</small></td>
      <td><span class="badge-tag ${r.active ? 'active' : 'disabled'}">${r.active ? 'Đang bật' : 'Tạm tắt'}</span></td>
      <td>
        <button class="btn-action-sm btn-edit-reward" data-id="${r.id}" title="Chỉnh sửa phần thưởng">✏️ Sửa</button>
      </td>
    </tr>
  `).join('');

  tbody.querySelectorAll('.btn-edit-reward').forEach(btn => {
    btn.onclick = () => {
      const id = Number(btn.dataset.id);
      const rew = rewardsList.find(r => r.id === id);
      if (rew) openRewardModal(rew);
    };
  });
}

function openRewardModal(reward = null) {
  const modal = document.getElementById('rewardModal');
  const title = document.getElementById('rewardModalTitle');
  const form = document.getElementById('formReward');
  if (!modal || !form) return;

  if (reward) {
    if (title) title.textContent = '✏️ Chỉnh Sửa Phần Thưởng';
    document.getElementById('rewardModalId').value = reward.id;
    document.getElementById('rewardModalName').value = reward.name || '';
    document.getElementById('rewardModalCost').value = reward.pointsCost || '';
    document.getElementById('rewardModalDesc').value = reward.description || '';
    document.getElementById('rewardModalActive').checked = reward.active !== false;
  } else {
    if (title) title.textContent = '🎁 Thêm Phần Thưởng Mới';
    form.reset();
    document.getElementById('rewardModalId').value = '';
    document.getElementById('rewardModalActive').checked = true;
  }
  modal.classList.add('show');
}

export function initLoyaltyEvents() {
  // Dropdown khách hàng
  const custSelect = document.getElementById('loyaltyCustomerSelect');
  if (custSelect) {
    custSelect.onchange = () => {
      loadCustomerLoyaltyData(custSelect.value);
    };
  }

  // Modal Thêm/Sửa quà
  const rewModal = document.getElementById('rewardModal');
  const openRewBtn = document.getElementById('btnOpenAddRewardModal');
  const closeRewBtn = document.getElementById('btnCloseRewardModal');
  const cancelRewBtn = document.getElementById('btnCancelRewardModal');
  const formRew = document.getElementById('formReward');

  const hideRew = () => { if (rewModal) rewModal.classList.remove('show'); };
  if (openRewBtn) openRewBtn.onclick = () => openRewardModal(null);
  if (closeRewBtn) closeRewBtn.onclick = hideRew;
  if (cancelRewBtn) cancelRewBtn.onclick = hideRew;

  if (formRew) {
    formRew.onsubmit = async (e) => {
      e.preventDefault();
      const id = document.getElementById('rewardModalId')?.value;
      const name = document.getElementById('rewardModalName')?.value.trim();
      const pointsCost = Number(document.getElementById('rewardModalCost')?.value || 0);
      const description = document.getElementById('rewardModalDesc')?.value.trim();
      const active = document.getElementById('rewardModalActive')?.checked;

      if (!name || pointsCost <= 0) {
        showToast('Vui lòng nhập tên quà và số điểm đổi lớn hơn 0!', 'warning');
        return;
      }

      const payload = { name, pointsCost, description, active };

      try {
        if (id) {
          await loyaltyApi.updateReward(id, payload);
          showToast('Đã cập nhật phần thưởng thành công!', 'success');
        } else {
          await loyaltyApi.createReward(payload);
          showToast('Đã thêm phần thưởng mới thành công!', 'success');
        }
        hideRew();
        formRew.reset();
        await loadLoyalty();
      } catch (err) {
        showToast(`Lỗi lưu phần thưởng: ${err.message}`, 'error');
      }
    };
  }

  // Modal Đổi Quà cho khách
  const redeemModal = document.getElementById('redeemRewardModal');
  const openRedeemBtn = document.getElementById('btnOpenRedeemRewardModal');
  const closeRedeemBtn = document.getElementById('btnCloseRedeemRewardModal');
  const cancelRedeemBtn = document.getElementById('btnCancelRedeemRewardModal');
  const formRedeem = document.getElementById('formRedeemReward');

  const hideRedeem = () => { if (redeemModal) redeemModal.classList.remove('show'); };
  if (openRedeemBtn) {
    openRedeemBtn.onclick = () => {
      if (!currentCustomerId) {
        showToast('Vui lòng chọn khách hàng trước khi đổi quà!', 'warning');
        return;
      }
      const sel = document.getElementById('redeemModalSelectReward');
      if (sel) {
        const activeRewards = rewardsList.filter(r => r.active);
        sel.innerHTML = '<option value="">-- Chọn phần thưởng --</option>' +
          activeRewards.map(r => `<option value="${r.id}">${escapeHtml(r.name)} (${r.pointsCost} ⭐)</option>`).join('');
      }
      if (redeemModal) redeemModal.classList.add('show');
    };
  }
  if (closeRedeemBtn) closeRedeemBtn.onclick = hideRedeem;
  if (cancelRedeemBtn) cancelRedeemBtn.onclick = hideRedeem;

  if (formRedeem) {
    formRedeem.onsubmit = async (e) => {
      e.preventDefault();
      const rewardId = document.getElementById('redeemModalSelectReward')?.value;
      if (!rewardId) {
        showToast('Vui lòng chọn phần thưởng cần đổi!', 'warning');
        return;
      }

      try {
        await loyaltyApi.redeemReward(currentCustomerId, Number(rewardId));
        showToast('Đổi phần thưởng cho khách hàng thành công!', 'success');
        hideRedeem();
        formRedeem.reset();
        await loadCustomerLoyaltyData(currentCustomerId);
      } catch (err) {
        showToast(`Không thể đổi quà: ${err.message}`, 'error');
      }
    };
  }

  document.getElementById('btnRefreshLoyalty')?.addEventListener('click', loadLoyalty);
}

function escapeHtml(text) {
  return String(text || '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
