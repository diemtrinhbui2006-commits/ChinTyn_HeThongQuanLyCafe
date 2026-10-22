import { request } from './apiClient.js';

/**
 * API Quản lý Danh Mục Món (/api/categories)
 */
export const categoryApi = {
  // Lấy toàn bộ danh mục
  getAll: () => request('/api/categories'),

  // Thêm danh mục mới (DTO: CategoryRequest)
  create: (data) => request('/api/categories', { method: 'POST', body: JSON.stringify(data) }),
};
