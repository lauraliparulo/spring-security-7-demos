package de.jugf.mfa;

import java.util.Arrays;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import de.jugf.mfa.google.GoogleAuthService;
import de.jugf.mfa.google.QRCodeService;
import reactor.core.publisher.Mono;


@Controller
public class MustacheController {

    private final MfaRequiredSuccessHandler mfaHandler;


    private final GoogleAuthService totpService;
    private final QRCodeService qrCodeService;



    public MustacheController(MfaRequiredSuccessHandler mfaHandler, GoogleAuthService totpService, QRCodeService qrCodeService) {
        this.totpService = totpService;
        this.qrCodeService = qrCodeService;
        this.mfaHandler = mfaHandler;
    }


    
  @GetMapping("/login-mfa")
    public Mono<String> firstLogin(Model model, Authentication authentication) {

        model.addAttribute("username", authentication.getName());

        return Mono.just("login-mfa"); // Mustache template name
    }


     @GetMapping("/google-auth-qr")
    public Mono<String> qrCodeForGoogleAuthenticator(Model model, Authentication authentication) {


        model.addAttribute("username", authentication.getName());

        String secret = totpService.generateSecret();

        String uri = totpService.buildOtpAuthUrl(authentication.getName(), secret);

        try {
            model.addAttribute("qrCode",  Arrays.toString(qrCodeService.generateQrCode(uri)));
        } catch (Exception ex) {
            System.getLogger(MustacheController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        model.addAttribute("secret", totpService.generateSecret());

        return Mono.just("google-auth-qr"); // Mustache template name
    }

}

    