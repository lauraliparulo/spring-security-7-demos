package de.jugf.model;

public record User(

    String username,
    String password,
    String totpSecret,
    boolean mfaEnabled
) {



    public User withTotpSecret(String secret) {
        return new User(username, password, secret, mfaEnabled);
    }

    public User withMfaEnabled(boolean enabled) {
        return new User(username, password, totpSecret, enabled);
    }


   
}
