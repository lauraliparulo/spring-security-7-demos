package de.jugf.mfa.google;

import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;

@Service
public class GoogleAuthService {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();


   public String generateSecret() {
        return gAuth.createCredentials().getKey();
    }

    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }

    public String buildOtpAuthUrl(String username, String secret) {
        return "otpauth://totp/MyApp:" + username +
                "?secret=" + secret +
                "&issuer=MyApp";
    }

}