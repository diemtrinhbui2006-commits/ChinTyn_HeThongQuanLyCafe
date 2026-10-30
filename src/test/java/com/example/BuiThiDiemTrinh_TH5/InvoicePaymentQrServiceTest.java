package com.example.BuiThiDiemTrinh_TH5;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import com.example.BuiThiDiemTrinh_TH5.entity.*;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.example.BuiThiDiemTrinh_TH5.service.InvoicePaymentQrService;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

class InvoicePaymentQrServiceTest {
    private final InvoicePaymentQrService service = new InvoicePaymentQrService();

    @Test
    void bankAndWalletQrEncodeExactDiscountedBillAmountReferenceAndValidChecksum() throws Exception {
        for (PaymentMethod method : new PaymentMethod[] {PaymentMethod.BANK_TRANSFER, PaymentMethod.E_WALLET}) {
            Invoice invoice = new Invoice(); invoice.setId(1234L); invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaymentMethod(method); invoice.setSubtotal(new BigDecimal("180000"));
            invoice.setDiscount(new BigDecimal("45000")); invoice.setTotalAmount(new BigDecimal("135000.00"));
            byte[] png = service.generate(invoice, method);
            var source = new BufferedImageLuminanceSource(ImageIO.read(new ByteArrayInputStream(png)));
            String payload = new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(source))).getText();
            assertTrue(payload.contains("5406135000"));
            assertTrue(payload.contains("0806HD1234"));
            assertTrue(payload.contains("010212"));
            String resource = "static/images/payments/" + (method == PaymentMethod.BANK_TRANSFER ? "techcombank-qr.png" : "wallet-qr.png");
            try (var input = new org.springframework.core.io.ClassPathResource(resource).getInputStream()) {
                String original = new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(
                        new BufferedImageLuminanceSource(ImageIO.read(input))))).getText();
                assertEquals(field(original, "38"), field(payload, "38"), "Recipient must be unchanged");
            }
            int crc = 0xffff;
            for (byte value : payload.substring(0, payload.length() - 4).getBytes(java.nio.charset.StandardCharsets.UTF_8)) {
                crc ^= (value & 255) << 8;
                for (int bit = 0; bit < 8; bit++) crc = ((crc & 0x8000) != 0 ? (crc << 1) ^ 0x1021 : crc << 1) & 0xffff;
            }
            assertEquals(String.format("%04X", crc), payload.substring(payload.length() - 4));
        }
    }

    private static String field(String payload, String tag) {
        for (int offset = 0; offset < payload.length();) {
            int length = Integer.parseInt(payload.substring(offset + 2, offset + 4));
            if (payload.substring(offset, offset + 2).equals(tag)) return payload.substring(offset + 4, offset + 4 + length);
            offset += 4 + length;
        }
        return null;
    }

    @Test
    void refusesCancelledCashZeroAndMismatchedPaidMethod() {
        Invoice invoice = new Invoice(); invoice.setId(9L); invoice.setTotalAmount(new BigDecimal("12000"));
        invoice.setStatus(InvoiceStatus.CANCELLED);
        assertThrows(BadRequestException.class, () -> service.generate(invoice, PaymentMethod.BANK_TRANSFER));
        invoice.setStatus(InvoiceStatus.OPEN);
        assertThrows(BadRequestException.class, () -> service.generate(invoice, PaymentMethod.CASH));
        invoice.setTotalAmount(BigDecimal.ZERO);
        assertThrows(BadRequestException.class, () -> service.generate(invoice, PaymentMethod.BANK_TRANSFER));
        invoice.setTotalAmount(BigDecimal.ONE); invoice.setStatus(InvoiceStatus.PAID); invoice.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        assertThrows(BadRequestException.class, () -> service.generate(invoice, PaymentMethod.E_WALLET));
    }
}
