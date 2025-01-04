package pl.mateuszmarcyk.charity_donation_app.util;

import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.entity.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;

@Component
public class TokenFactory {

    public PasswordResetVerificationToken getPasswordResetVerificationToken(String token, User user, int validTime) {
        return new PasswordResetVerificationToken(token, user, validTime);
    }

    public VerificationToken getVerificationToken(String token, User user, int validTime) {
        return new VerificationToken(token, user, validTime);
    }
}
