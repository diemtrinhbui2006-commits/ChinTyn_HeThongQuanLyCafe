/**
 * MAIN ENTRYPOINT - Tích hợp toàn bộ các module giao diện
 */

import { initPosModule, loadPosData, showTableSelection } from './modules/posModule.js';
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
import { loadOperations, initOperationsEvents } from './modules/operationsModule.js';

const operationsPages = new Set([
  'inventory',
  'reservations',
  'shifts',
  'suppliers',
  'promotions',
  'qr-orders',
  'loyalty',
]);

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
  initOperationsEvents();
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


const pageTitles = {
  pos: 'Bán hàng (POS)', operations: 'Vận hành & Hệ thống',
  'customer-hub': 'Khách hàng', 'menu-hub': 'Quản trị thực đơn',
  orders: 'Lịch sử đơn hàng', tables: 'Quản lý bàn', products: 'Quản lý món',
  categories: 'Danh mục món', dashboard: 'Tổng quan (Dashboard)', customers: 'Khách hàng',
  users: 'Nhân viên & Tài khoản', inventory: 'Kho nguyên liệu', reservations: 'Đặt bàn',
  shifts: 'Ca & đối soát', suppliers: 'Nhà cung cấp & chi phí', promotions: 'Khuyến mãi',
  'qr-orders': 'Gọi món QR', loyalty: 'Điểm thưởng',
};
let currentPage = 'pos';
const parentSection = page => ['products', 'categories'].includes(page) ? 'menu-hub'
  : ['customers', 'loyalty'].includes(page) ? 'customer-hub'
  : page === 'pos' ? 'pos' : 'operations';
const pageLoaders = {
  pos: loadPosData, dashboard: loadDashboard, orders: loadOrdersHistory,
  products: loadProductsAdmin, categories: loadCategoriesAdmin, tables: loadTablesAdmin,
  customers: loadCustomersAdmin, users: loadUsersAdmin,
};
async function refreshPage(page) {
  const loader = pageLoaders[page] || (operationsPages.has(page) ? loadOperations : null);
  if (loader) await loader();
}
async function navigateTo(target) {
  const view = document.getElementById(target + 'PageView');
  if (!view) return;
  currentPage = target;
  if (target === 'pos') showTableSelection();
  document.querySelectorAll('.page-view').forEach(page => page.classList.toggle('active', page === view));
  const section = ['operations', 'customer-hub', 'menu-hub', 'pos'].includes(target) ? target : parentSection(target);
  document.querySelectorAll('.bottom-tab').forEach(tab => {
    const active = tab.dataset.section === section;
    tab.classList.toggle('active', active);
    if (active) tab.setAttribute('aria-current', 'page');
    else tab.removeAttribute('aria-current');
  });
  document.querySelectorAll('.nav-item').forEach(item => item.classList.toggle('active', item.dataset.page === target));
  document.getElementById('topbarPageTitle').textContent = pageTitles[target];
  document.querySelector('.section-trail').hidden = section === target;
  document.getElementById('currentSectionName').textContent = pageTitles[section];
  try { await refreshPage(target); }
  catch (error) { showToast('Không thể tải ' + pageTitles[target] + ': ' + error.message, 'error'); }
}
function initNavigation() {
  document.querySelectorAll('[data-page]').forEach(item => {
    item.addEventListener('click', () => navigateTo(item.dataset.page));
  });
  document.querySelectorAll('.bottom-tab').forEach(tab => {
    tab.addEventListener('click', () => navigateTo(tab.dataset.section));
  });
  document.getElementById('btnBackToHub').addEventListener('click', () => navigateTo(parentSection(currentPage)));
  document.querySelector('.bottom-tab.active').setAttribute('aria-current', 'page');
}

// Nút làm mới dữ liệu
function initGlobalRefresh() {
  const refreshBtn = document.getElementById('btnRefreshAll');
  if (refreshBtn) {
    refreshBtn.addEventListener('click', async () => {
      try {
        await refreshPage(currentPage);
        showToast('Đã làm mới dữ liệu hệ thống!', 'success');
      } catch (error) { showToast(error.message, 'error'); }
    });
  }

  const btnRefreshOrders = document.getElementById('btnRefreshOrdersList');
  if (btnRefreshOrders) {
    btnRefreshOrders.addEventListener('click', async () => {
      await loadOrdersHistory();
      showToast('Đã cập nhật danh sách đơn hàng mới nhất!', 'success');
    });
  }
}
