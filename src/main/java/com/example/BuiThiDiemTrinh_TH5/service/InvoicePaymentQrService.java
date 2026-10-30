package com.example.BuiThiDiemTrinh_TH5.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.example.BuiThiDiemTrinh_TH5.entity.Invoice;
import com.example.BuiThiDiemTrinh_TH5.entity.InvoiceStatus;
import com.example.BuiThiDiemTrinh_TH5.entity.PaymentMethod;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class InvoicePaymentQrService {
    public byte[] generate(Invoice invoice, PaymentMethod requestedMethod) throws Exception {
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) throw new BadRequestException("Hóa đơn đã hủy không thể tạo QR thanh toán");
        PaymentMethod method = requestedMethod == null ? invoice.getPaymentMethod() : requestedMethod;
        if (method != PaymentMethod.BANK_TRANSFER && method != PaymentMethod.E_WALLET)
            throw new BadRequestException("Chỉ tạo QR cho chuyển khoản hoặc ví điện tử");
        if (invoice.getStatus() == InvoiceStatus.PAID && method != invoice.getPaymentMethod())
            throw new BadRequestException("Phương thức QR không khớp hóa đơn đã thanh toán");
        BigDecimal amount = invoice.getTotalAmount();
        if (amount == null || amount.signum() <= 0) throw new BadRequestException("Số tiền chuyển khoản phải lớn hơn 0");
        String value = amount.stripTrailingZeros().toPlainString();
        if (value.length() > 13) throw new BadRequestException("Số tiền vượt giới hạn của QR thanh toán");
        String resource = method == PaymentMethod.BANK_TRANSFER ? "techcombank-qr.png" : "wallet-qr.png";
        String original;
        try (var input = new ClassPathResource("static/images/payments/" + resource).getInputStream()) {
            var source = new BufferedImageLuminanceSource(ImageIO.read(input));
            original = new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(source))).getText();
        }
        if (!original.endsWith(crc(original.substring(0, original.length() - 4))))
            throw new IllegalStateException("QR người nhận không hợp lệ");
        var fields = parse(original);
        if (!"704".equals(fields.get("53")) || !fields.containsKey("38"))
            throw new IllegalStateException("QR người nhận không phải VietQR VND");
        fields.remove("63");
        fields.put("01", "12");
        fields.put("54", value);
        var additional = parse(fields.getOrDefault("62", ""));
        additional.put("08", "HD" + invoice.getId());
        fields.put("62", encode(additional));
        // EMV fields are ordered by ID; the checksum must always be the last field.
        var ordered = new LinkedHashMap<String, String>();
        fields.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(e -> ordered.put(e.getKey(), e.getValue()));
        String payload = encode(ordered) + "6304";
        payload += crc(payload);
        var matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 360, 360);
        var output = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", output);
        return output.toByteArray();
    }

    private static LinkedHashMap<String, String> parse(String payload) {
        var fields = new LinkedHashMap<String, String>();
        for (int offset = 0; offset < payload.length();) {
            if (offset + 4 > payload.length()) throw new IllegalStateException("Dữ liệu QR bị thiếu");
            String tag = payload.substring(offset, offset + 2);
            int length = Integer.parseInt(payload.substring(offset + 2, offset + 4));
            int end = offset + 4 + length;
            if (end > payload.length()) throw new IllegalStateException("Dữ liệu QR bị thiếu");
            fields.put(tag, payload.substring(offset + 4, end));
            offset = end;
        }
        return fields;
    }

    private static String encode(java.util.Map<String, String> fields) {
        var result = new StringBuilder();
        fields.forEach((tag, text) -> {
            if (text.length() > 99) throw new IllegalStateException("Trường QR quá dài");
            result.append(tag).append(String.format(Locale.ROOT, "%02d", text.length())).append(text);
        });
        return result.toString();
    }

    private static String crc(String payload) {
        int result = 0xFFFF;
        for (byte value : payload.getBytes(StandardCharsets.UTF_8)) {
            result ^= (value & 0xFF) << 8;
            for (int bit = 0; bit < 8; bit++) result = ((result & 0x8000) != 0 ? (result << 1) ^ 0x1021 : result << 1) & 0xFFFF;
        }
        return String.format(Locale.ROOT, "%04X", result);
    }
}
