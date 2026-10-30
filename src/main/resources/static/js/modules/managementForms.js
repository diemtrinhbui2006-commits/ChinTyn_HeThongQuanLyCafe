import { getCurrentUser } from './authModule.js';
import { canViewPage } from '../permissions.js';
import { userApi } from '../api/userApi.js';
import { loadUsersAdmin } from './userModule.js';
import { showToast } from '../utils/toast.js';

const dialogs = new Map();
export function managementFormError(actionId, message) {
  const error = dialogs.get(actionId)?.querySelector('.management-error');
  if (error) error.textContent = message;
}

export function completeManagementForm(actionId) {
  const dialog = dialogs.get(actionId);
  if (!dialog) return;
  dialog.close();
  dialog.querySelector('form')?.reset();
}

function mountForm({ page, actionId, title, description, fields, beforeOpen }) {
  const action = document.getElementById(actionId);
  const section = document.getElementById(page + 'PageView');
  if (!action || !section) return;
  const originalParents = new Set();
  const dialog = document.createElement('dialog');
  dialog.id = actionId + 'Dialog'; dialog.className = 'management-dialog';
  dialog.setAttribute('aria-labelledby', actionId + 'Title');
  const header = document.createElement('header');
  header.innerHTML = `<div><p>CHINTYN COFFEE</p><h2 id="${actionId}Title"></h2></div>`;
  header.querySelector('h2').textContent = title;
  const close = document.createElement('button'); close.type = 'button'; close.className = 'management-close';
  close.textContent = '×'; close.setAttribute('aria-label', 'Đóng ' + title); close.onclick = () => dialog.close(); header.appendChild(close);
  const hint = document.createElement('p'); hint.className = 'management-description'; hint.textContent = description;
  const form = document.createElement('form');
  const grid = document.createElement('div'); grid.className = 'management-form-grid';
  fields.forEach(([id, label, required = false]) => {
    const input = document.getElementById(id);
    if (!input) return;
    originalParents.add(input.parentElement);
    input.removeAttribute('style'); input.required = required;
    const field = document.createElement('div'); field.className = 'management-field';
    const caption = document.createElement('label'); caption.htmlFor = id; caption.textContent = label + (required ? ' *' : '');
    field.append(caption, input); grid.appendChild(field);
  });
  originalParents.add(action.parentElement); action.type = 'button'; action.removeAttribute('style'); action.classList.add('management-save');
  action.addEventListener('click', event => {
    if (!form.reportValidity()) { event.preventDefault(); event.stopImmediatePropagation(); }
  }, true);
  form.addEventListener('submit', event => { event.preventDefault(); action.click(); });
  const footer = document.createElement('footer'); const cancel = document.createElement('button');
  cancel.type = 'button'; cancel.className = 'btn-action-sm'; cancel.textContent = 'Hủy'; cancel.onclick = () => dialog.close(); footer.append(cancel, action);
  const error = document.createElement('p'); error.className = 'management-error'; error.setAttribute('role', 'alert');
  form.append(grid, error, footer); dialog.append(header, hint, form); document.body.appendChild(dialog); dialogs.set(actionId, dialog);
  originalParents.forEach(parent => { if (!parent.children.length) parent.remove(); });
  const launcher = document.createElement('button'); launcher.type = 'button'; launcher.id = 'open-' + actionId;
  launcher.className = 'btn-primary management-launch'; launcher.dataset.adminOnly = ''; launcher.textContent = '+ ' + title;
  let actions = section.querySelector('.management-header-actions');
  if (!actions) { actions = document.createElement('div'); actions.className = 'management-header-actions'; section.querySelector('.data-card-header').appendChild(actions); }
  actions.appendChild(launcher);
  launcher.addEventListener('click', async () => {
    if (getCurrentUser().role !== 'ADMIN' || !canViewPage(getCurrentUser(), page)) return;
    try {
      launcher.disabled = true;
      if (beforeOpen) await beforeOpen();
      error.textContent = '';
      dialog.showModal();
    } catch (error) { showToast(error.message, 'error'); }
    finally { launcher.disabled = false; }
  });
}

function createField(id, type = 'text', options = {}) {
  const input = document.createElement(type === 'select' ? 'select' : 'input'); input.id = id;
  if (type !== 'select') input.type = type;
  Object.assign(input, options);
  if (options.value != null) input.defaultValue = options.value;
  document.getElementById('usersPageView').appendChild(input);
  return input;
}

function mountUserForms() {
  const section = document.getElementById('usersPageView');
  for (const [id, type, options] of [
    ['newEmployeeName', 'text', { maxLength: 100 }], ['newEmployeePhone', 'tel', { maxLength: 20 }],
    ['newEmployeeEmail', 'email', { maxLength: 100 }], ['newEmployeePosition', 'text', { maxLength: 100 }],
    ['newEmployeeSalary', 'number', { min: '0', step: '1000', value: '0' }],
    ['newAccountUsername', 'text', { maxLength: 50, autocomplete: 'off' }],
    ['newAccountPassword', 'password', { minLength: 12, maxLength: 100, autocomplete: 'new-password' }],
    ['newAccountRole', 'select', {}], ['newAccountEmployee', 'select', {}],
  ]) createField(id, type, options);
  document.getElementById('newAccountRole').innerHTML = '<option value="STAFF">Phục vụ</option><option value="CASHIER">Thu ngân</option><option value="ADMIN">Quản trị viên</option>';
  for (const [id, text] of [['saveNewEmployee', 'Lưu nhân viên'], ['saveNewAccount', 'Tạo tài khoản']]) {
    const button = document.createElement('button'); button.id = id; button.textContent = text; button.className = 'btn-primary'; section.appendChild(button);
  }
  mountForm({ page: 'users', actionId: 'saveNewEmployee', title: 'Thêm nhân viên', description: 'Tạo hồ sơ nhân viên trước khi cấp tài khoản đăng nhập.', fields: [
    ['newEmployeeName', 'Họ và tên', true], ['newEmployeePosition', 'Chức vụ', true], ['newEmployeePhone', 'Số điện thoại'],
    ['newEmployeeEmail', 'Email'], ['newEmployeeSalary', 'Lương cơ bản (đ)', true],
  ] });
  mountForm({ page: 'users', actionId: 'saveNewAccount', title: 'Thêm tài khoản', description: 'Gắn nhân viên và chọn đúng vai trò. Mật khẩu tối thiểu 12 ký tự.', fields: [
    ['newAccountUsername', 'Tên đăng nhập', true], ['newAccountPassword', 'Mật khẩu', true], ['newAccountRole', 'Vai trò', true], ['newAccountEmployee', 'Nhân viên', true],
  ], beforeOpen: async () => {
    const [employees, accounts] = await Promise.all([userApi.getAllEmployees(), userApi.getAllUsers()]);
    const select = document.getElementById('newAccountEmployee'); select.replaceChildren(new Option('-- Chọn nhân viên --', ''));
    employees.filter(e => !accounts.some(u => u.employeeId === e.id)).forEach(e => select.add(new Option(e.name, e.id)));
  } });
  const value = id => document.getElementById(id).value.trim();
  for (const [id, save] of [
    ['saveNewEmployee', () => userApi.createEmployee({ name: value('newEmployeeName'), position: value('newEmployeePosition'), phone: value('newEmployeePhone'), email: value('newEmployeeEmail'), salary: Number(value('newEmployeeSalary')) })],
    ['saveNewAccount', () => userApi.createUser({ username: value('newAccountUsername'), password: document.getElementById('newAccountPassword').value, role: value('newAccountRole'), employeeId: Number(value('newAccountEmployee')) })],
  ]) document.getElementById(id).addEventListener('click', async event => {
    const button = event.currentTarget; if (button.disabled) return;
    try { button.disabled = true; await save(); completeManagementForm(id); showToast('Đã lưu thành công', 'success'); await loadUsersAdmin(); }
    catch (error) { managementFormError(id, error.message); }
    finally { button.disabled = false; }
  });
}

export function initManagementForms() {
  const configs = [
    { page: 'categories', actionId: 'btnAddCategorySubmit', title: 'Thêm danh mục', description: 'Sắp xếp thực đơn thành các nhóm để tìm món nhanh hơn.', fields: [['addCategoryNameInput', 'Tên danh mục', true]] },
    { page: 'tables', actionId: 'btnAddTableSubmit', title: 'Thêm bàn', description: 'Nhập số bàn phục vụ. Ví dụ: 12 hoặc Bàn 12.', fields: [['addTableNameInput', 'Số bàn', true]] },
    { page: 'customers', actionId: 'btnAddCustomerSubmit', title: 'Thêm khách hàng', description: 'Lưu thông tin liên hệ để chăm sóc khách hàng.', fields: [['addCustomerName', 'Họ và tên', true], ['addCustomerPhone', 'Số điện thoại', true], ['addCustomerEmail', 'Email']] },
    { page: 'inventory', actionId: 'opAddIngredient', title: 'Thêm nguyên liệu', description: 'Tạo nguyên liệu và ngưỡng cảnh báo tồn kho.', fields: [['opIngredientName', 'Tên nguyên liệu', true], ['opIngredientUnit', 'Đơn vị tính', true], ['opIngredientThreshold', 'Ngưỡng cảnh báo']] },
    { page: 'inventory', actionId: 'opAdjustStock', title: 'Điều chỉnh kho', description: 'Nhập số dương để tăng tồn hoặc số âm để giảm tồn.', fields: [['opIngredientSelect', 'Nguyên liệu', true], ['opStockQuantity', 'Số lượng điều chỉnh', true]] },
    { page: 'promotions', actionId: 'opAddPromotion', title: 'Tạo mã khuyến mãi', description: 'Chỉ mã đã tạo và đang hiệu lực mới được áp dụng lên hóa đơn.', fields: [['opPromoCode', 'Mã khuyến mãi', true], ['opPromoName', 'Tên chương trình', true], ['opPromoType', 'Loại giảm giá', true], ['opPromoValue', 'Giá trị giảm', true], ['opPromoMinimum', 'Giá trị đơn tối thiểu'], ['opPromoLimit', 'Giới hạn lượt sử dụng'], ['opPromoStarts', 'Bắt đầu lúc'], ['opPromoEnds', 'Kết thúc lúc']] },
  ];
  configs.forEach(mountForm);
  mountUserForms();
  // Generate table QR on demand, while keeping the pending orders list visible.
  const card = document.getElementById('qrTableSelect')?.closest('.data-card');
  if (card) {
    const layout = card.parentElement; layout.classList.add('management-qr-layout');
    const opener = document.createElement('button'); opener.type = 'button'; opener.className = 'btn-primary'; opener.id = 'open-table-qr'; opener.textContent = '+ Tạo mã QR cho bàn';
    card.replaceWith(opener);
    const dialog = document.createElement('dialog'); dialog.id = 'tableQrManagerDialog'; dialog.className = 'management-dialog'; dialog.setAttribute('aria-label', 'Tạo mã QR cho bàn');
    const close = document.createElement('button'); close.type = 'button'; close.className = 'management-close'; close.textContent = '×'; close.setAttribute('aria-label', 'Đóng tạo mã QR'); close.onclick = () => dialog.close();
    dialog.append(close, card); document.body.appendChild(dialog);
    opener.onclick = () => { if (canViewPage(getCurrentUser(), 'qr-orders')) dialog.showModal(); };
  }
  document.querySelectorAll('.page-view .data-card > .data-table').forEach(table => {
    const scroll = document.createElement('div'); scroll.className = 'management-table-scroll';
    table.replaceWith(scroll); scroll.appendChild(table);
  });
}
