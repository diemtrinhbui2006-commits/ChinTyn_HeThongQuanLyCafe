package com.example.BuiThiDiemTrinh_TH5.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;
import com.example.BuiThiDiemTrinh_TH5.service.InvoicePaymentQrService;
import com.example.BuiThiDiemTrinh_TH5.service.InvoiceService;

@RestController
@RequestMapping("/api/invoices")
public class InvoicePaymentQrController {
    private final InvoiceService invoices;
    private final InvoicePaymentQrService qr;
    public InvoicePaymentQrController(InvoiceService invoices, InvoicePaymentQrService qr) {
        this.invoices = invoices; this.qr = qr;
    }
    @GetMapping(value = "/{id}/payment-qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@PathVariable Long id, @RequestParam(required = false) PaymentMethod method) throws Exception {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(qr.generate(invoices.getInvoiceById(id), method));
    }
}
