


## Step 1. Login


POST localhost:8080/login
{
  "username": "user",
  "password": "password"
}
``


## Step 2.  Submit OTP


POST localhost:8080/mfa
{
  "username": "user",
  "otp": "123456"
}

-------------
TODO  - IMPROVE response after login!!

--------------------------------------------


This example is simplified. In production you should:
🔐 Security

Use TOTP (Google Authenticator) via libraries:

com.eatthepath:otp-java


Store OTP in:

Redis (reactive)
Expiring store (TTL)


Never store OTP in memory map!!!

🧠 Session / Token

Issue JWT after MFA succeeds
Store authentication state between factors

📱 Delivery

Send OTP via:

SMS (Twilio)
Email
Authenticator app



✅ Summary
This implementation shows how to:

Plug into Spring Security WebFlux pipeline
Customize authentication flow
Insert MFA step after password verification
Keep things reactive (Mono/Flux)

✅ JWT-based MFA
✅ TOTP with QR code (Google Authenticator)
✅ Full production-ready architecture (Redis + stateless sessions)
