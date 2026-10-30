import { request } from './apiClient.js';

/**
 * API Quản lý Tài Khoản & Nhân Viên (/api/users & /api/employees)
 */
export const userApi = {
  // Lấy danh sách tài khoản
  getAllUsers: () => request('/api/users'),

  // Tạo tài khoản mới (mã hóa mật khẩu ở backend)
  createUser: (data) => request('/api/users', { method: 'POST', body: JSON.stringify(data) }),

  // Lấy danh sách nhân sự
  getAllEmployees: () => request('/api/employees'),
  createEmployee: (data) => request('/api/employees', { method: 'POST', body: JSON.stringify(data) }),
};
