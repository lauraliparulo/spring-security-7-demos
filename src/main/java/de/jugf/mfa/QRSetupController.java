package de.jugf.mfa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.jugf.mfa.google.GoogleAuthService;
import de.jugf.mfa.google.QRCodeService;
import de.jugf.model.User;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/2fa")
public class QRSetupController {

    private final GoogleAuthService totpService;
    private final QRCodeService qrCodeService;

    // simulate DB
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public QRSetupController(GoogleAuthService totpService, QRCodeService qrCodeService) {
        this.totpService = totpService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping(value = "/setup", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<byte[]> setup(Authentication auth) {

        String username = auth.getName();

        User user = users.computeIfAbsent(username, u -> new User());

        // ✅ generate secret once
        if (user.totpSecret() == null) {
            String secret = totpService.generateSecret();
            user.
        }

        String uri = totpService.buildOtpAuthUrl(username, user.getTotpSecret());

        try {
            return Mono.just(qrCodeService.generateQrCode(uri));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
