import { productApi } from '../api/productApi.js';
import { categoryApi } from '../api/categoryApi.js';
import { formatVND } from '../utils/formatters.js';
import { showToast } from '../utils/toast.js';
import { getCurrentUser } from './authModule.js';

let cachedProducts = [];
let cachedCategories = [];

// Trạng thái phân trang quản lý món
const paginationState = {
  currentPage: 1,
  pageSize: 7,
  searchKeyword: '',
};

/**
 * Module Quản Lý Thực Đơn (Products)
 */
export async function loadProductsAdmin() {
  const tbody = document.getElementById('productsAdminBody');
  if (!tbody) return;
  tbody.innerHTML = '<tr><td colspan="6" align="center">Đang tải danh sách món...</td></tr>';

  try {
    const [products, categories] = await Promise.all([
      productApi.getAll().catch(() => []),
      categoryApi.getAll().catch(() => []),
    ]);

    cachedProducts = products || [];
    cachedCategories = categories || [];

    renderProductsTable();
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" style="color:red;">Lỗi tải dữ liệu: ${err.message}</td></tr>`;
  }
}

/**
 * Render bảng danh sách sản phẩm theo trang và từ khóa tìm kiếm
 */
export function renderProductsTable() {
  const tbody = document.getElementById('productsAdminBody');
  const paginationEl = document.getElementById('productsPagination');
  if (!tbody) return;

  // Lập từ điển danh mục
  const catMap = {};
  cachedCategories.forEach(c => {
    catMap[c.id] = c.name;
  });

  // 1. Lọc theo từ khóa tìm kiếm
  const kw = paginationState.searchKeyword.toLowerCase().trim();
  const filtered = cachedProducts.filter(p => {
    if (!kw) return true;
    const catName = (catMap[p.categoryId] || '').toLowerCase();
    const prodName = (p.name || '').toLowerCase();
    return prodName.includes(kw) || catName.includes(kw);
  });

  const totalItems = filtered.length;
  const totalPages = Math.ceil(totalItems / paginationState.pageSize) || 1;

  // Giới hạn trang hiện tại trong phạm vi hợp lệ
  if (paginationState.currentPage > totalPages) paginationState.currentPage = totalPages;
  if (paginationState.currentPage < 1) paginationState.currentPage = 1;

  const startIdx = (paginationState.currentPage - 1) * paginationState.pageSize;
  const endIdx = Math.min(startIdx + paginationState.pageSize, totalItems);
  const pageItems = filtered.slice(startIdx, endIdx);

  tbody.innerHTML = '';

  if (pageItems.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" align="center" style="color:var(--text-secondary); padding:20px;">
      ${kw ? `Không tìm thấy món nào khớp với từ khóa "${kw}"` : 'Chưa có món nào trong thực đơn'}
    </td></tr>`;
    if (paginationEl) paginationEl.innerHTML = '';
    return;
  }

  // 2. Render từng dòng món ăn
  pageItems.forEach(p => {
    const isAvail = p.available !== false;
    const catName = catMap[p.categoryId] || (p.category?.name) || 'Chung';
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td><strong>#${p.id}</strong></td>
      <td><strong>${p.name}</strong></td>
      <td><span style="background:#f1f5f9; padding:2px 8px; border-radius:4px; font-size:0.82rem;">${catName}</span></td>
      <td style="font-weight:700; color:#d97706;">${formatVND(p.price)}</td>
      <td><span class="badge-tag ${isAvail ? 'active' : 'disabled'}">${isAvail ? 'Đang bán' : 'Ngừng bán'}</span></td>
      <td>
        <button class="btn-action-sm btn-edit-prod" data-id="${p.id}" style="color:#2563eb; border-color:#93c5fd;">
          ✏️ Sửa
        </button>
        <button class="btn-action-sm btn-toggle-avail" data-id="${p.id}" data-status="${!isAvail}">
          ${isAvail ? 'Ngừng bán' : 'Bật bán'}
        </button>
        ${getCurrentUser().role === 'ADMIN' ? `<button type="button" class="btn-action-sm btn-delete-prod" data-id="${p.id}" style="color:#b91c1c; border-color:#fecaca;">🗑 Xóa</button>` : ''}
      </td>
    `;
    tbody.appendChild(tr);
  });

  // Gán sự kiện click nút Sửa
  tbody.querySelectorAll('.btn-delete-prod').forEach(button => {
    button.addEventListener('click', async () => {
      if (getCurrentUser().role !== 'ADMIN' || button.disabled) return;
      const product = cachedProducts.find(p => p.id === Number(button.dataset.id));
      if (!product || !confirm(`Xóa món "${product.name}" khỏi thực đơn? Hóa đơn cũ vẫn được giữ lại.`)) return;
      button.disabled = true;
      try {
        await productApi.delete(product.id);
        showToast('Đã xóa món khỏi thực đơn', 'success');
        await loadProductsAdmin();
      } catch (error) {
        showToast('Không thể xóa món: ' + error.message, 'error');
        button.disabled = false;
      }
    });
  });

  tbody.querySelectorAll('.btn-edit-prod').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = parseInt(btn.dataset.id);
      const prod = cachedProducts.find(x => x.id === id);
      if (prod) openEditProductModal(prod);
    });
  });

  // Gán sự kiện click Bật/Tắt bán
  tbody.querySelectorAll('.btn-toggle-avail').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.dataset.id;
      const newStatus = btn.dataset.status === 'true';
      try {
        await productApi.setAvailability(id, newStatus);
        showToast('Cập nhật trạng thái thành công', 'success');
        loadProductsAdmin();
      } catch (e) {
        showToast('Lỗi cập nhật: ' + e.message, 'error');
      }
    });
  });

  // 3. Render Thanh Phân Trang
  renderPaginationBar(totalItems, totalPages, startIdx, endIdx);
}

/**
 * Hiển thị thanh điều khiển phân trang
 */
function renderPaginationBar(totalItems, totalPages, startIdx, endIdx) {
  const container = document.getElementById('productsPagination');
  if (!container) return;

  const current = paginationState.currentPage;

  let pagesHtml = '';
  for (let i = 1; i <= totalPages; i++) {
    pagesHtml += `
      <button class="pagination-btn ${i === current ? 'active' : ''}" data-page="${i}">
        ${i}
      </button>
    `;
  }

  container.innerHTML = `
    <div class="pagination-info">
      Hiển thị <strong>${totalItems > 0 ? startIdx + 1 : 0} - ${endIdx}</strong> / tổng <strong>${totalItems}</strong> món
      <span style="margin-left: 10px;">| Số món/trang:</span>
      <select class="page-size-selector" id="adminPageSizeSelect">
        <option value="5" ${paginationState.pageSize === 5 ? 'selected' : ''}>5</option>
        <option value="7" ${paginationState.pageSize === 7 ? 'selected' : ''}>7</option>
        <option value="10" ${paginationState.pageSize === 10 ? 'selected' : ''}>10</option>
        <option value="15" ${paginationState.pageSize === 15 ? 'selected' : ''}>15</option>
      </select>
    </div>

    <div class="pagination-controls">
      <button class="pagination-btn" id="btnFirstPage" ${current === 1 ? 'disabled' : ''} title="Trang đầu">« Đầu</button>
      <button class="pagination-btn" id="btnPrevPage" ${current === 1 ? 'disabled' : ''} title="Trang trước">‹ Trước</button>
      ${pagesHtml}
      <button class="pagination-btn" id="btnNextPage" ${current === totalPages ? 'disabled' : ''} title="Trang sau">Sau ›</button>
      <button class="pagination-btn" id="btnLastPage" ${current === totalPages ? 'disabled' : ''} title="Trang cuối">Cuối »</button>
    </div>
  `;

  // Gán sự kiện cho các nút phân trang
  container.querySelectorAll('.pagination-btn[data-page]').forEach(btn => {
    btn.addEventListener('click', () => {
      paginationState.currentPage = parseInt(btn.dataset.page);
      renderProductsTable();
    });
  });

  const btnFirst = document.getElementById('btnFirstPage');
  if (btnFirst) btnFirst.addEventListener('click', () => {
    paginationState.currentPage = 1;
    renderProductsTable();
  });

  const btnPrev = document.getElementById('btnPrevPage');
  if (btnPrev) btnPrev.addEventListener('click', () => {
    paginationState.currentPage--;
    renderProductsTable();
  });

  const btnNext = document.getElementById('btnNextPage');
  if (btnNext) btnNext.addEventListener('click', () => {
    paginationState.currentPage++;
    renderProductsTable();
  });

  const btnLast = document.getElementById('btnLastPage');
  if (btnLast) btnLast.addEventListener('click', () => {
    paginationState.currentPage = totalPages;
    renderProductsTable();
  });

  const sizeSelect = document.getElementById('adminPageSizeSelect');
  if (sizeSelect) {
    sizeSelect.addEventListener('change', (e) => {
      paginationState.pageSize = parseInt(e.target.value);
      paginationState.currentPage = 1;
      renderProductsTable();
    });
  }
}

/**
 * Mở modal chỉnh sửa món và điền sẵn thông tin
 */
function openEditProductModal(prod) {
  const modal = document.getElementById('editProductModal');
  if (!modal) return;

  document.getElementById('editProductId').value = prod.id;
  document.getElementById('editProductName').value = prod.name || '';
  document.getElementById('editProductPrice').value = prod.price || '';
  document.getElementById('editProductQty').value = prod.quantity ?? 100;
  document.getElementById('editProductDesc').value = prod.description || '';

  // Đổ danh mục vào select
  const select = document.getElementById('editProductCategorySelect');
  if (select) {
    select.innerHTML = '';
    cachedCategories.forEach(c => {
      const isSelected = (c.id === prod.categoryId) ? 'selected' : '';
      select.innerHTML += `<option value="${c.id}" ${isSelected}>${c.name}</option>`;
    });
  }

  modal.classList.add('show');
}

/**
 * Khởi tạo sự kiện cho các modal Thêm món, Sửa món và Ô tìm kiếm phân trang
 */
export function initProductModalEvents() {
  // === TÌM KIẾM MÓN ĂN TRÊN BẢNG ===
  const searchInput = document.getElementById('adminProductSearch');
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      paginationState.searchKeyword = e.target.value;
      paginationState.currentPage = 1;
      renderProductsTable();
    });
  }

  // === THÊM MÓN MỚI ===
  const openAddBtn = document.getElementById('btnOpenAddProductModal');
  if (openAddBtn) {
    openAddBtn.addEventListener('click', async () => {
      const select = document.getElementById('addProductCategorySelect');
      if (select) {
        select.innerHTML = '';
        const cats = await categoryApi.getAll().catch(() => cachedCategories);
        cats.forEach(c => {
          select.innerHTML += `<option value="${c.id}">${c.name}</option>`;
        });
      }
      document.getElementById('addProductModal')?.classList.add('show');
    });
  }

  const closeAddBtn = document.getElementById('btnCloseAddProductModal');
  if (closeAddBtn) {
    closeAddBtn.addEventListener('click', () => {
      document.getElementById('addProductModal')?.classList.remove('show');
    });
  }

  const submitAddBtn = document.getElementById('btnSubmitAddProduct');
  if (submitAddBtn) {
    submitAddBtn.addEventListener('click', async () => {
      const name = document.getElementById('addProductName')?.value.trim();
      const price = parseFloat(document.getElementById('addProductPrice')?.value);
      const categoryId = parseInt(document.getElementById('addProductCategorySelect')?.value);
      const quantity = parseInt(document.getElementById('addProductQty')?.value) || 100;
      const description = document.getElementById('addProductDesc')?.value.trim() || '';

      if (!name) {
        showToast('Tên món không được để trống!', 'warning');
        return;
      }
      if (isNaN(price) || price <= 0) {
        showToast('Giá món phải lớn hơn 0!', 'warning');
        return;
      }
      if (!categoryId) {
        showToast('Vui lòng chọn danh mục cho món!', 'warning');
        return;
      }

      try {
        await productApi.create({ name, price, categoryId, quantity, description });
        showToast(`Đã thêm món "${name}" thành công!`, 'success');

        // Xóa trắng form sau khi thêm
        document.getElementById('addProductName').value = '';
        document.getElementById('addProductPrice').value = '';
        document.getElementById('addProductDesc').value = '';
        document.getElementById('addProductQty').value = '100';

        document.getElementById('addProductModal')?.classList.remove('show');
        loadProductsAdmin();
      } catch (err) {
        showToast('Lỗi thêm món: ' + err.message, 'error');
      }
    });
  }

  // === CHỈNH SỬA MÓN ===
  const closeEditBtn = document.getElementById('btnCloseEditProductModal');
  if (closeEditBtn) {
    closeEditBtn.addEventListener('click', () => {
      document.getElementById('editProductModal')?.classList.remove('show');
    });
  }

  const submitEditBtn = document.getElementById('btnSubmitEditProduct');
  if (submitEditBtn) {
    submitEditBtn.addEventListener('click', async () => {
      const id = parseInt(document.getElementById('editProductId')?.value);
      const name = document.getElementById('editProductName')?.value.trim();
      const price = parseFloat(document.getElementById('editProductPrice')?.value);
      const categoryId = parseInt(document.getElementById('editProductCategorySelect')?.value);
      const quantity = parseInt(document.getElementById('editProductQty')?.value) || 0;
      const description = document.getElementById('editProductDesc')?.value.trim() || '';

      if (!id) {
        showToast('Mã món không hợp lệ!', 'error');
        return;
      }
      if (!name) {
        showToast('Tên món không được để trống!', 'warning');
        return;
      }
      if (isNaN(price) || price <= 0) {
        showToast('Giá món phải lớn hơn 0!', 'warning');
        return;
      }
      if (!categoryId) {
        showToast('Vui lòng chọn danh mục cho món!', 'warning');
        return;
      }

      try {
        await productApi.update(id, { name, price, categoryId, quantity, description });
        showToast(`Đã cập nhật món "${name}" thành công!`, 'success');
        document.getElementById('editProductModal')?.classList.remove('show');
        loadProductsAdmin();
      } catch (err) {
        showToast('Lỗi cập nhật món: ' + err.message, 'error');
      }
    });
  }
}
