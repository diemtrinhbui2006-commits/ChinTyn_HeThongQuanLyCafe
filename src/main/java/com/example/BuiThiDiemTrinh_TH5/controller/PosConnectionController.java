package com.example.BuiThiDiemTrinh_TH5.controller;

import java.io.ByteArrayOutputStream;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletRequest;

/** Shares only the POS address. Employees still authenticate with their own accounts. */
@RestController
@RequestMapping("/api/pos-connection")
public class PosConnectionController {
    private final String publicUrl;

    public PosConnectionController(@Value("${app.pos.public-url:}") String publicUrl) {
        this.publicUrl = publicUrl;
    }

    @GetMapping
    public List<ConnectionAddress> addresses(HttpServletRequest request) throws Exception {
        if (!publicUrl.isBlank()) {
            URI uri = URI.create(publicUrl.trim());
            if (!List.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
                throw new BadRequestException("POS_PUBLIC_URL phải là địa chỉ HTTP hoặc HTTPS hợp lệ");
            }
            return List.of(new ConnectionAddress("Địa chỉ POS đã cấu hình", publicUrl.trim().replaceAll("/+$", "") + "/pos"));
        }
        List<ConnectionAddress> result = new ArrayList<>();
        var interfaces = NetworkInterface.getNetworkInterfaces();
        if (interfaces == null) return result;
        for (var network : Collections.list(interfaces)) {
            if (!network.isUp() || network.isLoopback() || network.isVirtual()) continue;
            String adapter = network.getDisplayName().toLowerCase(java.util.Locale.ROOT);
            if (adapter.contains("vmware") || adapter.contains("virtualbox") || adapter.contains("docker")) continue;
            for (var address : Collections.list(network.getInetAddresses())) {
                if (!(address instanceof Inet4Address) || address.isLoopbackAddress() || address.isLinkLocalAddress()
                        || !address.isSiteLocalAddress()) continue;
                String port = (request.getScheme().equals("http") && request.getServerPort() == 80)
                        || (request.getScheme().equals("https") && request.getServerPort() == 443)
                        ? "" : ":" + request.getServerPort();
                result.add(new ConnectionAddress(network.getDisplayName() + " · " + address.getHostAddress(),
                        request.getScheme() + "://" + address.getHostAddress() + port + request.getContextPath() + "/pos"));
            }
        }
        result.sort(Comparator.comparing(ConnectionAddress::label));
        return result;
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qr(@RequestParam String url, HttpServletRequest request) throws Exception {
        if (addresses(request).stream().noneMatch(address -> address.url().equals(url))) {
            throw new BadRequestException("Địa chỉ kết nối POS không hợp lệ. Hãy tải lại danh sách kết nối.");
        }
        var image = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 320, 320);
        var output = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(image, "PNG", output);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(output.toByteArray());
    }

    public record ConnectionAddress(String label, String url) { }
}
