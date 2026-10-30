const pages = {
  ADMIN: null,
  CASHIER: new Set(['pos', 'operations', 'connections', 'orders', 'shifts', 'customer-hub', 'customers']),
  STAFF: new Set(['pos', 'operations', 'connections']),
};
export function canViewPage(user, page) {
  return !!user && !user.passwordChangeRequired && (user.role === 'ADMIN' || !!pages[user.role]?.has(page));
}
export function canTakePayment(user) { return !user?.passwordChangeRequired && ['ADMIN', 'CASHIER'].includes(user?.role); }
export function applyRoleVisibility(user) {
  if (user?.passwordChangeRequired) user = null;
  document.body.classList.toggle('auth-required', !pages.hasOwnProperty(user?.role));
  document.querySelectorAll('[data-page], [data-section]').forEach(element => {
    element.hidden = !canViewPage(user, element.dataset.page || element.dataset.section);
  });
  document.querySelectorAll('[data-admin-only]').forEach(element => element.hidden = user?.role !== 'ADMIN');
  document.querySelectorAll('[data-cashier-only]').forEach(element => element.hidden = !canTakePayment(user));
  document.getElementById('staffOrderHint').hidden = user?.role !== 'STAFF';
  const count = [...document.querySelectorAll('.bottom-tab')].filter(tab => !tab.hidden).length;
  document.querySelector('.bottom-navigation').style.setProperty('--visible-tabs', Math.max(count, 1));
}
