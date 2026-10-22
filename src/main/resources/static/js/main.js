/**
 * MAIN ENTRYPOINT - Tích hợp toàn bộ các module giao diện
 */

import { initPosModule, loadPosData } from './modules/posModule.js';
import { loadDashboard } from './modules/dashboardModule.js';
import { loadProductsAdmin, initProductModalEvents } from './modules/productModule.js';
import { loadTablesAdmin, initTableAdminEvents } from './modules/tableModule.js';
import { loadCategoriesAdmin, initCategoryAdminEvents } from './modules/categoryModule.js';
import { loadOrdersHistory } from './modules/orderModule.js';
import { loadCustomersAdmin, initCustomerAdminEvents } from './modules/customerModule.js';
import { loadUsersAdmin } from './modules/userModule.js';
import { initReceiptModalEvents } from './modules/receiptModal.js';
import { initAuthEvents } from './modules/authModule.js';
import { showToast } from './utils/toast.js';

document.addEventListener('DOMContentLoaded', async () => {
  initClock();
  initNavigation();
  initGlobalRefresh();

  // Khởi tạo xác thực & đăng xuất
  await initAuthEvents(async () => {
    await initPosModule();
  });

  // Khởi tạo các sự kiện của Modal
  initReceiptModalEvents();
  initProductModalEvents();
  initTableAdminEvents();
  initCategoryAdminEvents();
  initCustomerAdminEvents();

});

// Đồng hồ thời gian thực
function initClock() {
  const clockEl = document.getElementById('realtimeClock');
  if (!clockEl) return;
  const tick = () => {
    const d = new Date();
    clockEl.textContent = d.toLocaleDateString('vi-VN') + ' ' + d.toLocaleTimeString('vi-VN');
  };
  tick();
  setInterval(tick, 1000);
}

// Chuyển trang qua thanh Sidebar
function initNavigation() {
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    item.addEventListener('click', () => {
      navItems.forEach(i => i.classList.remove('active'));
      item.classList.add('active');

      const target = item.dataset.page;

      // Ẩn tất cả trang và hiện trang được chọn
      document.querySelectorAll('.page-view').forEach(view => view.classList.remove('active'));
      const activeView = document.getElementById(`${target}PageView`);
      if (activeView) activeView.classList.add('active');

      // Cập nhật tiêu đề Topbar
      const titles = {
        pos: '☕ Bán Hàng & Gọi Món (POS)',
        orders: '📜 Lịch Sử Đơn Hàng',
        tables: '🪑 Quản Lý Bàn Ăn',
        products: '☕ Quản Lý Thực Đơn',
        categories: '📁 Quản Lý Danh Mục',
        dashboard: '📊 Tổng Quan Hệ Thống (Dashboard)',
        customers: '👥 Danh Sách Khách Hàng',
        users: '👨‍💼 Quản Lý Nhân Viên & Tài Khoản',
      };
      const titleEl = document.getElementById('topbarPageTitle');
      if (titleEl) titleEl.textContent = titles[target] || 'Hệ Thống Quản Lý Cafe';

      // Gọi module tương ứng nạp dữ liệu
      if (target === 'pos') loadPosData();
      if (target === 'dashboard') loadDashboard();
      if (target === 'orders') loadOrdersHistory();
      if (target === 'products') loadProductsAdmin();
      if (target === 'categories') loadCategoriesAdmin();
      if (target === 'tables') loadTablesAdmin();
      if (target === 'customers') loadCustomersAdmin();
      if (target === 'users') loadUsersAdmin();
    });
  });
}

// Nút làm mới dữ liệu
function initGlobalRefresh() {
  const refreshBtn = document.getElementById('btnRefreshAll');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', async () => {
      await loadPosData();
      showToast('Đã làm mới dữ liệu hệ thống!', 'success');
    });
  }
}
