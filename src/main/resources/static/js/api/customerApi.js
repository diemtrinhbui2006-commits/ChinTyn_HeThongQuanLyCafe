import { request } from './apiClient.js';

/**
 * API Quản lý Khách Hàng (/api/customers)
 */
export const customerApi = {
  // Lấy danh sách khách hàng
  getAll: () => request('/api/customers'),

  // Thêm khách hàng mới
  create: (data) => request('/api/customers', { method: 'POST', body: JSON.stringify(data) }),
};
