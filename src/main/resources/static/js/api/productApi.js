import { request } from './apiClient.js';

/**
 * API Quản lý Món Ăn & Thực Đơn (/api/products)
 */
export const productApi = {
  // Lấy toàn bộ món
  getAll: () => request('/api/products'),

  // Tìm kiếm món ăn theo tên (realtime)
  search: (keyword) => request(`/api/products/search?keyword=${encodeURIComponent(keyword)}`),

  // Thêm món mới (DTO: ProductRequest)
  create: (data) => request('/api/products', { method: 'POST', body: JSON.stringify(data) }),

  // Cập nhật thông tin món
  update: (id, data) => request(`/api/products/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  delete: (id) => request(`/api/products/${id}`, { method: 'DELETE' }),

  // Bật/Tắt trạng thái bán (Soft Toggle)
  setAvailability: (id, available) => request(`/api/products/${id}/availability?available=${available}`, { method: 'PATCH' }),
};
