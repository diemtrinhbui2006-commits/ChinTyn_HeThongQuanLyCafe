package com.example.BuiThiDiemTrinh_TH5.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.BuiThiDiemTrinh_TH5.dto.ApplyDiscountRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.CheckoutRequest;
import com.example.BuiThiDiemTrinh_TH5.dto.InvoiceResponse;
import com.example.BuiThiDiemTrinh_TH5.dto.OrderFoodRequest;
import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;
import com.example.BuiThiDiemTrinh_TH5.mapper.InvoiceMapper;
import com.example.BuiThiDiemTrinh_TH5.service.InvoiceService;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final InvoiceMapper invoiceMapper;

    public InvoiceController(InvoiceService invoiceService, InvoiceMapper invoiceMapper) {
        this.invoiceService = invoiceService;
        this.invoiceMapper = invoiceMapper;
    }

    /**
     * Nghiệp vụ Gọi món:
     * POST /api/invoices/order
     * Thêm món vào bàn, chuyển trạng thái bàn sang "Đang dùng", tính toán tạm tính.
     */
    @PostMapping("/order")
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponse orderFood(@RequestBody OrderFoodRequest request) {
        Invoice invoice = invoiceService.orderFood(request);
        return invoiceMapper.toResponse(invoice);
    }

    /**
     * Lấy hóa đơn đang mở (OPEN) của bàn:
     * GET /api/invoices/table/{tableId}
     */
    @GetMapping("/table/{tableId}")
    public InvoiceResponse getOpenInvoiceByTable(@PathVariable Long tableId) {
        Invoice invoice = invoiceService.getOpenInvoiceByTableId(tableId);
        return invoiceMapper.toResponse(invoice);
    }

    /**
     * Lấy chi tiết hóa đơn theo ID:
     * GET /api/invoices/{id}
     */
    @GetMapping("/{id}")
    public InvoiceResponse getInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        return invoiceMapper.toResponse(invoice);
    }

    /**
     * Lấy danh sách hóa đơn (có thể lọc theo trạng thái):
     * GET /api/invoices?status=OPEN
     */
    @GetMapping
    public List<InvoiceResponse> getInvoices(@RequestParam(required = false) String status) {
        return invoiceService.getInvoices(status)
                .stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    /**
     * Áp dụng chiết khấu / mã giảm giá:
     * POST /api/invoices/{id}/discount
     */
    @PostMapping("/{id}/discount")
    public InvoiceResponse applyDiscount(
            @PathVariable Long id,
            @RequestBody ApplyDiscountRequest request
    ) {
        Invoice invoice = invoiceService.applyDiscount(id, request);
        return invoiceMapper.toResponse(invoice);
    }

    /**
     * Tính hóa đơn & Thanh toán:
     * POST /api/invoices/{id}/checkout
     * Chốt hóa đơn sang PAID, chuyển trạng thái bàn về AVAILABLE.
     */
    @PostMapping("/{id}/checkout")
    public InvoiceResponse checkout(
            @PathVariable Long id,
            @RequestBody(required = false) CheckoutRequest request
    ) {
        Invoice invoice = invoiceService.checkout(id, request);
        return invoiceMapper.toResponse(invoice);
    }

    /**
     * Hủy món khỏi bàn (hoàn kho và tính lại tổng tiền):
     * DELETE /api/invoices/{id}/details/{productId}?quantity=1
     */
    @DeleteMapping("/{id}/details/{productId}")
    public InvoiceResponse removeFood(
            @PathVariable Long id,
            @PathVariable Long productId,
            @RequestParam(required = false) Integer quantity
    ) {
        Invoice invoice = invoiceService.removeFoodFromInvoice(id, productId, quantity);
        return invoiceMapper.toResponse(invoice);
    }

    /**
     * Hủy toàn bộ hóa đơn:
     * POST /api/invoices/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public InvoiceResponse cancelInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceService.cancelInvoice(id);
        return invoiceMapper.toResponse(invoice);
    }

}
