import { request } from './apiClient.js';

/**
 * API Quản Lý Đặt Bàn (/api/reservations)
 */
export const reservationApi = {
  getAll: () => request('/api/reservations'),
  create: (data) => request('/api/reservations', { method: 'POST', body: JSON.stringify(data) }),
  cancel: (id) => request(`/api/reservations/${id}/cancel`, { method: 'POST' }),
};
