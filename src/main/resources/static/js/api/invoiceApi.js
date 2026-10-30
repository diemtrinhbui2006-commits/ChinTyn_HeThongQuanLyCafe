import { request } from './apiClient.js';

/**
 * API Quản lý Hóa Đơn & Nghiệp Vụ Bán Hàng POS (/api/invoices)
 */
export const invoiceApi = {
  // 1. Gọi món: thêm món vào bàn, chuyển trạng thái bàn sang OCCUPIED
  orderFood: (payload) => request('/api/invoices/order', { method: 'POST', body: JSON.stringify(payload) }),

  // 2. Lấy hóa đơn đang mở của bàn
  getOpenByTable: (tableId) => request(`/api/invoices/table/${tableId}`),

  // 3. Lấy chi tiết hóa đơn theo ID
  getById: (id) => request(`/api/invoices/${id}`),

  // 4. Lấy tất cả hóa đơn (có thể lọc status = OPEN, PAID, CANCELLED)
  getAll: (status) => request(status ? `/api/invoices?status=${status}` : '/api/invoices'),

  // 5. Áp dụng giảm giá / chiết khấu (%)
  applyDiscount: (id, payload) => request(`/api/invoices/${id}/discount`, { method: 'POST', body: JSON.stringify(payload) }),

  // 6. Chốt thanh toán: chuyển trạng thái sang PAID, giải phóng bàn về AVAILABLE
  checkout: (id, payload) => request(`/api/invoices/${id}/checkout`, { method: 'POST', body: JSON.stringify(payload || {}) }),

  // 7. Xóa hoặc giảm bớt số lượng món khỏi bàn
  removeFood: (id, productId, qty, detailId) => request(`/api/invoices/${id}/details/${productId}?quantity=${qty || 1}${detailId ? '&detailId=' + detailId : ''}`, { method: 'DELETE' }),

  // 8. Hủy toàn bộ hóa đơn của bàn
  cancel: (id) => request(`/api/invoices/${id}/cancel`, { method: 'POST' }),
};
