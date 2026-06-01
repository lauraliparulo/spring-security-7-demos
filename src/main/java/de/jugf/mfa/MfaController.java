package de.jugf.mfa;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class MfaController {

    private final MfaRequiredSuccessHandler mfaHandler;

    public MfaController(MfaRequiredSuccessHandler mfaHandler) {
        this.mfaHandler = mfaHandler;
    }



    
//     @GetMapping("/setup-2fa")
//     public String showQrCode(@AuthenticationPrincipal CustomUserDetails user, Model model) {
//         // Verwende das Secret des Users (In der Praxis wird dies einmalig generiert und in der DB gespeichert)
//         String qrCodeUrl = googleAuthService.getQRUrl(user.getUsername(), user.getSecret());
        
//         model.addAttribute("qrUrl", qrCodeUrl);
//         return "setup-2fa"; // Verweist auf setup-2fa.html (Thymeleaf)
//     }

    
//     @GetMapping("/setup")
// public ResponseEntity<Map<String, String>> setupMfa(@AuthenticationPrincipal UserDetails userDetails) {
//     GoogleAuthenticator gAuth = new GoogleAuthenticator();
//     GoogleAuthenticatorKey key = gAuth.createCredentials();

//     String email = userDetails.getUsername(); // or any unique ID
//     String secret = key.getKey();

//     // Save `secret` to your database, linked to the current user

//         String qrData = String.format("otpauth://totp/MyApp:%s?secret=%s&issuer=MyApp", email, secret);
//         try {
//             BitMatrix bitMatrix = null;
//             try {
//                 bitMatrix = new QRCodeWriter().encode(qrData, BarcodeFormat.QR_CODE, 200, 200);
//             } catch (WriterException ex) {
//                 System.getLogger(MfaController.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
//             }
//             try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
//                 MatrixToImageWriter.writeToStream(bitMatrix, "PNG", stream);

//     String base64Qr = Base64.getEncoder().encodeToString(stream.toByteArray());

//     Map<String, String> response = new HashMap<>();
//     response.put("secret", secret);
//     response.put("qrCode", "data:image/png;base64," + base64Qr);

//     return ResponseEntity.ok(response);
// }

//     // @PostMapping("/otp-verify")
//     // public Mono<ResponseEntity<String>> verify(@RequestBody Map<String, String> request) {

//         String username = request.get("username");
//         String otp = request.get("otp");

//     //     if (mfaHandler.validateOtp(username, otp)) {
//     //         return Mono.just(ResponseEntity.ok("MFA SUCCESS"));
//     //     }

//         return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
//                 .body("INVALID OTP"));
//     }
}
