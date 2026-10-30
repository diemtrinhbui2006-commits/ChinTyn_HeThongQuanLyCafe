import { request } from '../api/apiClient.js';
import { showToast } from '../utils/toast.js';

function showSelectedConnection() {
  const url = document.getElementById('connectionAddressSelect').value;
  const image = document.getElementById('posConnectionQr');
  document.getElementById('posConnectionUrl').value = url;
  document.getElementById('btnCopyPosUrl').disabled = !url;
  image.hidden = !url;
  image.removeAttribute('src');
  if (url) {
    document.getElementById('connectionStatus').textContent = 'Quét mã để mở POS trên điện thoại';
    image.src = '/api/pos-connection/qr?url=' + encodeURIComponent(url);
  }
}

export async function loadConnections() {
  const select = document.getElementById('connectionAddressSelect');
  const status = document.getElementById('connectionStatus');
  const previous = select.value;
  select.replaceChildren();
  showSelectedConnection();
  status.textContent = 'Đang tìm địa chỉ kết nối...';
  try {
    const addresses = await request('/api/pos-connection');
    for (const address of addresses) {
      const option = document.createElement('option');
      option.value = address.url;
      option.textContent = address.label;
      select.appendChild(option);
    }
    select.hidden = !addresses.length;
    if (!addresses.length) {
      status.textContent = 'Chưa tìm thấy địa chỉ Wi-Fi/LAN. Hãy kết nối máy POS vào mạng rồi bấm Tải lại QR.';
      return;
    }
    const preferred = addresses.find(address => address.url === previous)
      || addresses.find(address => new URL(address.url).hostname === location.hostname);
    if (preferred) select.value = preferred.url;
    showSelectedConnection();
  } catch (error) {
    status.textContent = 'Không thể tải kết nối: ' + error.message;
  }
}

export function initConnectionEvents() {
  document.getElementById('connectionAddressSelect').addEventListener('change', showSelectedConnection);
  document.getElementById('btnRefreshConnection').addEventListener('click', loadConnections);
  document.getElementById('posConnectionQr').addEventListener('error', () => {
    document.getElementById('posConnectionQr').hidden = true;
    document.getElementById('connectionStatus').textContent = 'Không tải được mã QR. Hãy bấm Tải lại QR hoặc đăng nhập lại.';
  });
  document.getElementById('btnCopyPosUrl').addEventListener('click', async () => {
    const input = document.getElementById('posConnectionUrl');
    try {
      await navigator.clipboard.writeText(input.value);
      showToast('Đã sao chép địa chỉ POS', 'success');
    } catch {
      input.focus();
      input.select();
      showToast('Đã chọn địa chỉ. Nhấn giữ để sao chép.', 'info');
    }
  });
}
