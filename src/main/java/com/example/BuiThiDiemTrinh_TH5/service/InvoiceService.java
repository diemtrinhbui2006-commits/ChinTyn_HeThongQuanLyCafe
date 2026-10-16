package com.example.BuiThiDiemTrinh_TH5.service;

import java.util.List;

import com.example.BuiThiDiemTrinh_TH5.dto.ApplyDiscountRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.CheckoutRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.OrderFoodRequest;
import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;

public interface InvoiceService {

    /**
     * Nghiệp vụ Gọi món:
     * - Thêm món vào bàn.
     * - Nếu bàn đang "Trống" (AVAILABLE): tạo hóa đơn mới (OPEN), tự động chuyển trạng thái bàn sang "Đang dùng" (OCCUPIED).
     * - Nếu bàn đã "Đang dùng" (OCCUPIED): lấy hóa đơn OPEN hiện tại và bổ sung món.
     * - Chốt giá bán tại thời điểm gọi món, tính lineTotal, tính lại subtotal và totalAmount.
     */
    Invoice orderFood(OrderFoodRequest request);

    /**
     * Lấy thông tin hóa đơn đang mở (OPEN) theo ID bàn.
     */
    Invoice getOpenInvoiceByTableId(Long tableId);

    /**
     * Lấy thông tin hóa đơn theo ID.
     */
    Invoice getInvoiceById(Long id);

    /**
     * Lấy danh sách hóa đơn, tùy chọn lọc theo trạng thái (OPEN, PAID, CANCELLED).
     */
    List<Invoice> getInvoices(String status);

    /**
     * Nghiệp vụ Chiết khấu / Giảm giá:
     * - Áp dụng theo mã voucher/coupon hoặc theo số tiền/phần trăm giảm.
     * - Tính toán lại tổng tiền khách cần trả: totalAmount = subtotal - discount (đảm bảo >= 0).
     */
    Invoice applyDiscount(Long invoiceId, ApplyDiscountRequest request);

    /**
     * Nghiệp vụ Tính hóa đơn & Thanh toán:
     * - Tính tổng tiền khách cần trả (áp dụng chiết khấu nếu có).
     * - Ghi nhận thời điểm thanh toán (paidAt) và phương thức thanh toán.
     * - Chuyển trạng thái hóa đơn sang PAID.
     * - Tự động chuyển trạng thái bàn từ "Đang dùng" (OCCUPIED) trở về "Trống" (AVAILABLE).
     */
    Invoice checkout(Long invoiceId, CheckoutRequest request);

    /**
     * Hủy / giảm số lượng món trên bàn: hoàn trả tồn kho và tính lại tiền.
     */
    Invoice removeFoodFromInvoice(Long invoiceId, Long productId, Integer quantity);

    /**
     * Hủy hóa đơn đang mở: hoàn trả kho hàng và chuyển bàn về "Trống" (AVAILABLE).
     */
    Invoice cancelInvoice(Long invoiceId);
}
