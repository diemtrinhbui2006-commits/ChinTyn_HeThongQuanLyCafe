import { request } from './apiClient.js';

/**
 * API Quản Lý Nhà Cung Cấp & Chi Phí Hoạt Động (/api/suppliers, /api/expenses)
 */
export const supplierExpenseApi = {
  getSuppliers: () => request('/api/suppliers'),
  createSupplier: (data) => request('/api/suppliers', { method: 'POST', body: JSON.stringify(data) }),
  updateSupplier: (id, data) => request(`/api/suppliers/${id}`, { method: 'PUT', body: JSON.stringify(data) }),

  getExpenses: () => request('/api/expenses'),
  createExpense: (data) => request('/api/expenses', { method: 'POST', body: JSON.stringify(data) }),

  getIngredients: () => request('/api/inventory/ingredients'),
};
