package de.jugf.mfa;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import reactor.core.publisher.Mono;

@Controller
public class MfaController {

    private final MfaSetupHandler mfaSetupHandler;
    private final MfaRequiredSuccessHandler mfaHandler;

    public MfaController(MfaSetupHandler mfaSetupHandler, MfaRequiredSuccessHandler mfaHandler) {
        this.mfaSetupHandler = mfaSetupHandler;
        this.mfaHandler = mfaHandler;
    }

    



    @GetMapping("/setup-2fa")
    public String showQrCode(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String username = userDetails.getUsername();
        MfaSetupHandler.QrCodeData qrData = mfaSetupHandler.getQrCodeData(username);

        if (qrData != null) {
            model.addAttribute("qrCode", qrData.qrCode);
            model.addAttribute("secret", qrData.secret);
            model.addAttribute("username", username);
        }

        return "setup-2fa";
    }

    @PostMapping("/mfa/verify")
    @ResponseBody
    public Mono<Map<String, String>> verifyOtp(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {

        String username = userDetails.getUsername();
        String otp = request.get("otp");

        if (mfaHandler.validateOtp(username, otp)) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "MFA verified successfully");
            return Mono.just(response);
        }

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", "Invalid OTP");
        return Mono.just(errorResponse);
    }
}
