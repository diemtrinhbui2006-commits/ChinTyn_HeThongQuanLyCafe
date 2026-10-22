import { showToast } from '../utils/toast.js';
import { request } from '../api/apiClient.js';

const GUEST = { username: '', name: 'Chưa đăng nhập', role: 'GUEST', avatar: '?' };
let currentUser = null;

/**
 * Lấy thông tin tài khoản hiện tại đang đăng nhập
 */
export function getCurrentUser() {
  return currentUser || GUEST;
}

/**
 * Cập nhật thông tin hiển thị lên Sidebar
 */
export function updateSidebarUserUI(user) {
  if (!user) user = GUEST;
  const avatarEl = document.getElementById('sidebarUserAvatar');
  const nameEl = document.getElementById('sidebarUserName');
  const roleEl = document.getElementById('sidebarUserRole');

  if (avatarEl) avatarEl.textContent = user.avatar || user.name?.charAt(0) || 'U';
  if (nameEl) nameEl.textContent = user.name || user.username || 'Người dùng';
  if (roleEl) {
    roleEl.textContent = user.role || 'STAFF';
    roleEl.className = 'user-role-badge';
  }
}

/**
 * Mở modal đăng nhập
 */
export function showLoginModal() {
  const modal = document.getElementById('loginModal');
  if (modal) modal.classList.add('show');
}

/**
 * Đóng modal đăng nhập
 */
export function hideLoginModal() {
  const modal = document.getElementById('loginModal');
  if (modal) modal.classList.remove('show');
}

/**
 * Xử lý đăng xuất
 */
export async function handleLogout() {
  const isConfirmed = confirm('Bạn có chắc chắn muốn đăng xuất khỏi hệ thống ChinTyn Coffee không?');
  if (!isConfirmed) return;

  try {
    await request('/api/auth/logout', { method: 'POST' });
    currentUser = null;
    updateSidebarUserUI(GUEST);
    showToast('Đã đăng xuất khỏi tài khoản thành công!', 'warning');
    showLoginModal();
  } catch (error) {
    showToast('Không thể đăng xuất: ' + error.message, 'error');
  }
}

/**
 * Khởi tạo sự kiện đăng nhập & đăng xuất
 */
export async function initAuthEvents(onAuthenticated = async () => {}) {
  updateSidebarUserUI(GUEST);

  // 2. Nút Đăng xuất ở Sidebar & Topbar
  const btnLogout = document.getElementById('btnLogout');
  if (btnLogout) {
    btnLogout.addEventListener('click', handleLogout);
  }

  const btnTopbarLogout = document.getElementById('btnTopbarLogout');
  if (btnTopbarLogout) {
    btnTopbarLogout.addEventListener('click', handleLogout);
  }

  // Xác thực phiên đã lưu phía máy chủ hoặc hiện form đăng nhập.
  try {
    await request('/api/auth/csrf');
    currentUser = await request('/api/auth/me');
    updateSidebarUserUI(currentUser);
    hideLoginModal();
    await onAuthenticated();
  } catch {
    currentUser = null;
    updateSidebarUserUI(GUEST);
    showLoginModal();
  }

  const btnSubmitLogin = document.getElementById('btnSubmitLogin');
  if (btnSubmitLogin) {
    btnSubmitLogin.addEventListener('click', async () => {
      const username = document.getElementById('loginUsername')?.value.trim().toLowerCase();
      const password = document.getElementById('loginPassword')?.value;

      if (!username || !password) {
        showToast('Vui lòng nhập tên đăng nhập và mật khẩu!', 'warning');
        return;
      }

      try {
        await request('/api/auth/csrf');
        await request('/api/auth/login', {
          method: 'POST',
          body: JSON.stringify({ username, password }),
        });
        await request('/api/auth/csrf');
        currentUser = await request('/api/auth/me');
        updateSidebarUserUI(currentUser);
        hideLoginModal();
        showToast(`Xin chào ${currentUser.name}! Đăng nhập thành công (${currentUser.role}).`, 'success');
        await onAuthenticated();
      } catch (error) {
        showToast(error.message || 'Tên đăng nhập hoặc mật khẩu không đúng', 'error');
      } finally {
        const passwordField = document.getElementById('loginPassword');
        if (passwordField) passwordField.value = '';
      }
    });
  }
}
