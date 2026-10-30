package com.example.BuiThiDiemTrinh_TH5.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.BuiThiDiemTrinh_TH5.dto.InvoiceDetailResponse;
import com.example.BuiThiDiemTrinh_TH5.dto.InvoiceResponse;
import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;
import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceDetail;

@Component
public class InvoiceMapper {

    public InvoiceDetailResponse toDetailResponse(InvoiceDetail detail) {
        if (detail == null) {
            return null;
        }
        return new InvoiceDetailResponse(
                detail.getId(),
                detail.getProduct() != null ? detail.getProduct().getId() : null,
                detail.getProduct() != null ? detail.getProduct().getName() : null,
                detail.getQuantity(),
                detail.getUnitPrice(),
                detail.getLineTotal(),
                detail.getSugarPercent() == null ? 100 : detail.getSugarPercent(),
                detail.getIceOption() == null ? "NORMAL" : detail.getIceOption(),
                detail.getNote() == null ? "" : detail.getNote()
        );
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        List<InvoiceDetailResponse> detailResponses = invoice.getDetails() == null
                ? Collections.emptyList()
                : invoice.getDetails().stream().map(this::toDetailResponse).toList();

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCafeTable() != null ? invoice.getCafeTable().getId() : null,
                invoice.getCafeTable() != null ? invoice.getCafeTable().getTableNumber() : null,
                invoice.getEmployee() != null ? invoice.getEmployee().getId() : null,
                invoice.getEmployee() != null ? invoice.getEmployee().getName() : null,
                invoice.getCustomer() != null ? invoice.getCustomer().getId() : null,
                invoice.getCustomer() != null ? invoice.getCustomer().getName() : null,
                invoice.getCreatedAt(),
                invoice.getPaidAt(),
                invoice.getSubtotal(),
                invoice.getDiscount(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getPaymentMethod(),
                detailResponses
        );
    }
}
