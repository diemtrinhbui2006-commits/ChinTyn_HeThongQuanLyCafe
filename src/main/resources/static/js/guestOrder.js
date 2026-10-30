import { getProductImage } from './utils/imageMatcher.js';
import { formatVND } from './utils/formatters.js';

const token = new URLSearchParams(location.search).get('token');
const menuList = document.getElementById('guestMenuList');
const tableLabel = document.getElementById('tableLabel');
const countBadge = document.getElementById('guestItemsCountBadge');
const selectedCountEl = document.getElementById('guestSelectedCount');
const totalAmountEl = document.getElementById('guestTotalAmount');
const submitBtn = document.getElementById('submitGuestOrder');
const messageModal = document.getElementById('guestMessageModal');

const quantities = new Map();
let menuItems = [];

async function api(url, options = {}) {
  const response = await fetch(url, {
    credentials: 'same-origin',
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    }
  });
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message || `Lỗi HTTP ${response.status}`);
  return body;
}

function readCookie(name) {
  const entry = document.cookie.split('; ').find(v => v.startsWith(`${name}=`));
  return entry ? decodeURIComponent(entry.slice(name.length + 1)) : '';
}

async function init() {
  try {
    if (!token) {
      if (menuList) menuList.innerHTML = '<p style="color:red; text-align:center; padding:20px;">⚠️ Liên kết QR không hợp lệ hoặc đã hết hạn.</p>';
      return;
    }

    await api('/api/auth/csrf');
    const data = await api(`/api/guest/qr/${encodeURIComponent(token)}`);

    if (tableLabel) tableLabel.textContent = `Bàn Số #${data.tableNumber}`;
    menuItems = data.items || [];
    if (countBadge) countBadge.textContent = `${menuItems.length} món`;

    renderMenuItems();
  } catch (error) {
    if (menuList) menuList.innerHTML = `<p style="color:red; text-align:center; padding:20px;">⚠️ Lỗi: ${escapeHtml(error.message)}</p>`;
  }
}

function renderMenuItems() {
  if (!menuList) return;
  if (menuItems.length === 0) {
    menuList.innerHTML = '<p style="text-align:center; color:#94a3b8; padding:20px;">Quán hiện chưa có món nào sẵn sàng.</p>';
    return;
  }

  menuList.innerHTML = menuItems.map(item => {
    const qty = quantities.get(item.id) || 0;
    const imgUrl = getProductImage(item.name);

    return `
      <div class="guest-menu-item">
        <div class="guest-item-img">
          ${imgUrl ? `<img src="${imgUrl}" alt="${escapeHtml(item.name)}" style="width:100%; height:100%; border-radius:10px; object-fit:cover;" onerror="this.outerHTML='☕'">` : '☕'}
        </div>
        <div class="guest-item-info">
          <div class="guest-item-title">${escapeHtml(item.name)}</div>
          <div class="guest-item-price">${formatVND(item.price)}</div>
          ${item.description ? `<div class="guest-item-desc">${escapeHtml(item.description)}</div>` : ''}
        </div>
        <div class="guest-stepper">
          <button type="button" class="btn-step-minus" data-id="${item.id}">−</button>
          <span id="qty-${item.id}">${qty}</span>
          <button type="button" class="btn-step-plus" data-id="${item.id}">+</button>
        </div>
      </div>
    `;
  }).join('');

  menuList.querySelectorAll('.btn-step-minus').forEach(btn => {
    btn.onclick = () => {
      const id = Number(btn.dataset.id);
      const cur = quantities.get(id) || 0;
      if (cur > 0) {
        quantities.set(id, cur - 1);
        updateCartState();
      }
    };
  });

  menuList.querySelectorAll('.btn-step-plus').forEach(btn => {
    btn.onclick = () => {
      const id = Number(btn.dataset.id);
      const cur = quantities.get(id) || 0;
      quantities.set(id, cur + 1);
      updateCartState();
    };
  });
}

function updateCartState() {
  let totalCount = 0;
  let totalAmount = 0;

  menuItems.forEach(item => {
    const qty = quantities.get(item.id) || 0;
    const span = document.getElementById(`qty-${item.id}`);
    if (span) span.textContent = qty;

    if (qty > 0) {
      totalCount += qty;
      totalAmount += (Number(item.price) || 0) * qty;
    }
  });

  if (selectedCountEl) selectedCountEl.textContent = `${totalCount} món`;
  if (totalAmountEl) totalAmountEl.textContent = formatVND(totalAmount);
  if (submitBtn) submitBtn.disabled = totalCount === 0;
}

if (submitBtn) {
  submitBtn.onclick = async () => {
    const items = [...quantities.entries()]
      .filter(([, qty]) => qty > 0)
      .map(([productId, quantity]) => ({ productId, quantity }));

    if (items.length === 0) return;

    const guestName = document.getElementById('guestName')?.value.trim();
    const guestPhone = document.getElementById('guestPhone')?.value.trim();

    submitBtn.disabled = true;
    submitBtn.textContent = '⏳ Đang gửi yêu cầu...';

    try {
      await api(`/api/guest/qr/${encodeURIComponent(token)}/orders`, {
        method: 'POST',
        headers: {
          'X-XSRF-TOKEN': readCookie('XSRF-TOKEN') || '',
        },
        body: JSON.stringify({ guestName, guestPhone, items })
      });

      if (messageModal) messageModal.style.display = 'flex';
    } catch (err) {
      alert(`Không thể gửi yêu cầu: ${err.message}`);
      submitBtn.disabled = false;
      submitBtn.textContent = '🚀 Gửi Món Ngay';
    }
  };
}

function escapeHtml(text) {
  return String(text || '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

init();
