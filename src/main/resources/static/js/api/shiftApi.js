import { request } from './apiClient.js';

/**
 * API Quản Lý Ca Làm Việc & Đối Soát Tiền Mặt (/api/shifts)
 */
export const shiftApi = {
  getAll: () => request('/api/shifts'),
  open: (data) => request('/api/shifts/open', { method: 'POST', body: JSON.stringify(data) }),
  close: (id, data) => request(`/api/shifts/${id}/close`, { method: 'POST', body: JSON.stringify(data) }),
  dailyReport: (id) => request(`/api/shifts/${id}/daily-report`),
};
