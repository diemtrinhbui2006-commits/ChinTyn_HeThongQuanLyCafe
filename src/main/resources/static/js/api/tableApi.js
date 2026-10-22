import { request } from './apiClient.js';

/**
 * API Quản lý Bàn Ăn (/api/tables)
 */
export const tableApi = {
  // Lấy danh sách bàn (có thể lọc status = AVAILABLE hoặc OCCUPIED)
  getAll: (status) => request(status ? `/api/tables?status=${status}` : '/api/tables'),

  // Tạo bàn ăn mới
  create: (data) => request('/api/tables', { method: 'POST', body: JSON.stringify(data) }),

  // Cập nhật thông tin bàn
  update: (id, data) => request(`/api/tables/${id}`, { method: 'PUT', body: JSON.stringify(data) }),

  // Xóa bàn
  delete: (id) => request(`/api/tables/${id}`, { method: 'DELETE' }),
};
