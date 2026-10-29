const token = new URLSearchParams(location.search).get('token');
const menuNode = document.getElementById('guestMenu');
const message = document.getElementById('guestMessage');
const quantities = new Map();

async function api(url, options = {}) {
  const response = await fetch(url, { credentials: 'same-origin', ...options, headers: {
    'Content-Type': 'application/json', ...(options.headers || {})
  }});
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.message || `Lỗi ${response.status}`);
  return body;
}

async function init() {
  try {
    if (!token) throw new Error('Liên kết QR không hợp lệ.');
    await api('/api/auth/csrf');
    const menu = await api(`/api/guest/qr/${encodeURIComponent(token)}`);
    document.getElementById('tableLabel').textContent = `#${menu.tableNumber}`;
    menuNode.replaceChildren();
    menu.items.forEach(item => {
      const row = document.createElement('label');
      row.style.cssText = 'display:flex;justify-content:space-between;gap:12px;padding:12px;border-bottom:1px solid #ddd';
      row.innerHTML = `<span>${escapeHtml(item.name)}<br><small>${Number(item.price).toLocaleString('vi-VN')} ₫</small></span>`;
      const input = document.createElement('input');
      input.type = 'number'; input.min = '0'; input.value = '0'; input.style.width = '72px';
      input.addEventListener('change', () => quantities.set(item.id, Math.max(0, Number(input.value) || 0)));
      row.append(input); menuNode.append(row);
    });
  } catch (error) {
    message.textContent = error.message;
  }
}

document.getElementById('submitGuestOrder').addEventListener('click', async () => {
  const items = [...quantities].filter(([, quantity]) => quantity > 0)
    .map(([productId, quantity]) => ({ productId, quantity }));
  if (!items.length) { message.textContent = 'Vui lòng chọn ít nhất một món.'; return; }
  try {
    await api(`/api/guest/qr/${encodeURIComponent(token)}/orders`, {
      method: 'POST',
      headers: { 'X-XSRF-TOKEN': readCookie('XSRF-TOKEN') || '' },
      body: JSON.stringify({ guestName: document.getElementById('guestName').value.trim(),
        guestPhone: document.getElementById('guestPhone').value.trim(), items })
    });
    message.textContent = 'Đã gửi yêu cầu. Nhân viên sẽ xác nhận món trước khi tính tiền.';
    document.getElementById('submitGuestOrder').disabled = true;
  } catch (error) { message.textContent = error.message; }
});

function readCookie(name) {
  const entry = document.cookie.split('; ').find(value => value.startsWith(`${name}=`));
  return entry ? decodeURIComponent(entry.slice(name.length + 1)) : '';
}
function escapeHtml(text) {
  return String(text || '').replace(/[&<>"']/g, char => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
}
init();
