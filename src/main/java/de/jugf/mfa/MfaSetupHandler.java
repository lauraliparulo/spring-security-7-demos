package de.jugf.mfa;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;

import reactor.core.publisher.Mono;

@Component
public class MfaSetupHandler implements ServerAuthenticationSuccessHandler {

    private final MfaRequiredSuccessHandler mfaHandler;
    private final Map<String, QrCodeData> qrCodeCache = new ConcurrentHashMap<>();

    public MfaSetupHandler(MfaRequiredSuccessHandler mfaHandler) {
        this.mfaHandler = mfaHandler;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
            Authentication authentication) {

        String username = authentication.getName();

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();

        mfaHandler.storeSecret(username, secret);

        String qrData = String.format("otpauth://totp/MyApp:%s?secret=%s&issuer=MyApp", username, secret);

        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(qrData, BarcodeFormat.QR_CODE, 200, 200);
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, "PNG", stream);

                String base64Qr = Base64.getEncoder().encodeToString(stream.toByteArray());

                qrCodeCache.put(username, new QrCodeData(base64Qr, secret));

                ServerHttpResponse httpResponse = webFilterExchange.getExchange().getResponse();
                httpResponse.setStatusCode(HttpStatus.FOUND);
                httpResponse.getHeaders().setLocation(java.net.URI.create("/setup-2fa"));

                return httpResponse.setComplete();
            }
        } catch (WriterException | IOException e) {
            ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
    }

    public QrCodeData getQrCodeData(String username) {
        return qrCodeCache.get(username);
    }

    public static class QrCodeData {
        public final String qrCode;
        public final String secret;

        public QrCodeData(String qrCode, String secret) {
            this.qrCode = qrCode;
            this.secret = secret;
        }
    }
}
