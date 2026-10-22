import { showToast } from '../utils/toast.js';

const USER_STORAGE_KEY = 'chintyn_current_user';

const ROLE_PROFILES = {
  admin: { username: 'admin', name: 'Nguyễn Quản Trị', role: 'ADMIN', avatar: 'QT' },
  cashier: { username: 'cashier', name: 'Trần Thu Ngân', role: 'CASHIER', avatar: 'TN' },
  staff: { username: 'staff', name: 'Lê Phục Vụ', role: 'STAFF', avatar: 'PV' },
};

/**
 * Lấy thông tin tài khoản hiện tại đang đăng nhập
 */
export function getCurrentUser() {
  try {
    const raw = localStorage.getItem(USER_STORAGE_KEY);
    if (raw) return JSON.parse(raw);
  } catch (e) {
    // fallback
  }
  return ROLE_PROFILES.admin;
}

/**
 * Cập nhật thông tin hiển thị lên Sidebar
 */
export function updateSidebarUserUI(user) {
  if (!user) user = ROLE_PROFILES.admin;
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
export function handleLogout() {
  const isConfirmed = confirm('Bạn có chắc chắn muốn đăng xuất khỏi hệ thống ChinTyn Coffee không?');
  if (!isConfirmed) return;

  localStorage.removeItem(USER_STORAGE_KEY);
  showToast('Đã đăng xuất khỏi tài khoản thành công!', 'warning');

  // Cập nhật lại UI tạm thời và hiện form đăng nhập
  updateSidebarUserUI({ username: 'Chưa đăng nhập', name: 'Vui lòng đăng nhập', role: 'GUEST', avatar: '?' });
  showLoginModal();
}

/**
 * Khởi tạo sự kiện đăng nhập & đăng xuất
 */
export function initAuthEvents() {
  // 1. Cập nhật giao diện ban đầu từ storage
  const activeUser = getCurrentUser();
  updateSidebarUserUI(activeUser);

  // 2. Nút Đăng xuất ở Sidebar & Topbar
  const btnLogout = document.getElementById('btnLogout');
  if (btnLogout) {
    btnLogout.addEventListener('click', handleLogout);
  }

  const btnTopbarLogout = document.getElementById('btnTopbarLogout');
  if (btnTopbarLogout) {
    btnTopbarLogout.addEventListener('click', handleLogout);
  }

  // 3. Các nút chọn nhanh tài khoản Demo
  document.querySelectorAll('.btn-quick-role').forEach(btn => {
    btn.addEventListener('click', () => {
      const u = btn.dataset.user;
      const userField = document.getElementById('loginUsername');
      const passField = document.getElementById('loginPassword');
      if (userField) userField.value = u;
      if (passField) passField.value = '123456';
    });
  });

  // 4. Nút bấm Xác nhận Đăng Nhập
  const btnSubmitLogin = document.getElementById('btnSubmitLogin');
  if (btnSubmitLogin) {
    btnSubmitLogin.addEventListener('click', () => {
      const username = document.getElementById('loginUsername')?.value.trim().toLowerCase();
      const password = document.getElementById('loginPassword')?.value;

      if (!username || !password) {
        showToast('Vui lòng nhập tên đăng nhập và mật khẩu!', 'warning');
        return;
      }

      // Nhận diện profile hoặc tạo thông tin mới
      let profile = ROLE_PROFILES[username];
      if (!profile) {
        profile = {
          username: username,
          name: username.toUpperCase(),
          role: 'STAFF',
          avatar: username.substring(0, 2).toUpperCase(),
        };
      }

      // Lưu session vào localStorage
      localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(profile));
      updateSidebarUserUI(profile);
      hideLoginModal();
      showToast(`Xin chào ${profile.name}! Đăng nhập thành công (${profile.role}).`, 'success');
    });
  }
}
