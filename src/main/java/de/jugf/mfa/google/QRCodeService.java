package de.jugf.mfa.google;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;


@Component
public class QRCodeService {

    public byte[] generateQrCode(String text) throws Exception {
        int width = 250;
        int height = 250;

        BitMatrix matrix = new MultiFormatWriter().encode(
                text,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", stream);

        return stream.toByteArray();
    }
}
