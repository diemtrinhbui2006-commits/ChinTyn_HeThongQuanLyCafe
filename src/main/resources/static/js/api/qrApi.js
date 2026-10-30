import { request } from './apiClient.js';

/**
 * API Gọi Món Qua Mã QR Tại Bàn (/api/qr)
 */
export const qrApi = {
  createToken: (tableId) => request(`/api/qr/tables/${tableId}/token`, { method: 'POST' }),
  getPendingOrders: () => request('/api/qr/orders/pending'),
  approveOrder: (id, employeeId) => request(`/api/qr/orders/${id}/approve`, { method: 'POST', body: JSON.stringify({ employeeId }) }),
  getQrImageUrl: (token) => `/api/qr/tokens/${encodeURIComponent(token)}/image`,
};
