/**
 * Tiện ích định dạng dữ liệu (Tiền tệ, Ngày tháng)
 */

// Định dạng tiền tệ VNĐ (ví dụ: 35.000 ₫)
export function formatVND(num) {
  if (num == null) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(num);
}

// Định dạng thời gian Việt Nam (ví dụ: 22:30:00 22/10/2026)
export function formatDate(dateStr) {
  if (!dateStr) return '--';
  return new Date(dateStr).toLocaleString('vi-VN');
}
