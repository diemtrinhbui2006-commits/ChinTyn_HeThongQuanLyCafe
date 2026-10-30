package com.example.BuiThiDiemTrinh_TH5;

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import com.example.BuiThiDiemTrinh_TH5.controller.PosConnectionController;
import com.example.BuiThiDiemTrinh_TH5.exception.BadRequestException;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

class PosConnectionControllerTest {
    @Test
    void qrOpensConfiguredPosAndContainsNoCredentials() throws Exception {
        var controller = new PosConnectionController("http://192.168.1.8:8080/");
        var request = new MockHttpServletRequest();
        var url = controller.addresses(request).get(0).url();
        assertEquals("http://192.168.1.8:8080/pos", url);
        var response = controller.qr(url, request);
        var image = ImageIO.read(new ByteArrayInputStream(response.getBody()));
        var result = new QRCodeReader().decode(new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image))));
        assertEquals(url, result.getText());
        assertEquals("no-store", response.getHeaders().getCacheControl());
    }

    @Test
    void rejectsQrForUnrelatedAddress() {
        var controller = new PosConnectionController("http://192.168.1.8:8080");
        assertThrows(BadRequestException.class, () -> controller.qr("http://other.example/pos", new MockHttpServletRequest()));
    }
}
