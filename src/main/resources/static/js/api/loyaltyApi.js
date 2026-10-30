import { request } from './apiClient.js';

/**
 * API Chương Trình Điểm Thưởng & Quà Tặng Tri Ân (/api/customers, /api/rewards)
 */
export const loyaltyApi = {
  getCustomers: () => request('/api/customers'),
  getCustomerLoyalty: (customerId) => request(`/api/customers/${customerId}/loyalty`),
  redeemPoints: (customerId, points) => request(`/api/customers/${customerId}/loyalty/redeem`, { method: 'POST', body: JSON.stringify({ points }) }),

  getRewards: () => request('/api/rewards'),
  createReward: (data) => request('/api/rewards', { method: 'POST', body: JSON.stringify(data) }),
  updateReward: (id, data) => request(`/api/rewards/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  redeemReward: (customerId, rewardId) => request(`/api/customers/${customerId}/loyalty/redeem-reward`, { method: 'POST', body: JSON.stringify({ rewardId }) }),
};
