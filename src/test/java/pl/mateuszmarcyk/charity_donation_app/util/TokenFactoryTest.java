package pl.mateuszmarcyk.charity_donation_app.util;

import org.junit.jupiter.api.Test;
import pl.mateuszmarcyk.charity_donation_app.entity.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class TokenFactoryTest {

    private final TokenFactory tokenFactory = new TokenFactory();

    @Test
    void givenAllNeededArguments_whenGetPasswordResetVerificationToken_thenVerificationTokenIsInstantiated() {
        String token = "1234";
        User user = new User();
        int validTime = 15;

        PasswordResetVerificationToken passwordResetVerificationToken = tokenFactory.getPasswordResetVerificationToken(token, user, validTime);

        assertAll(
                () -> assertThat(passwordResetVerificationToken.getToken()).isEqualTo(token),
                () -> assertThat(passwordResetVerificationToken.getUser()).isEqualTo(user),
                () -> assertThat(passwordResetVerificationToken.getCreated().plusMinutes(validTime)).isEqualTo(passwordResetVerificationToken.getExpirationTime())
        );
    }

    @Test
    void givenAllNeededArguments_whenGetVerificationToken_thenVerificationTokenIsInstantiated() {
        String token = "1234";
        User user = new User();
        int validTime = 15;

        VerificationToken verificationToken = tokenFactory.getVerificationToken(token, user, validTime);

        assertAll(
                () -> assertThat(verificationToken.getToken()).isEqualTo(token),
                () -> assertThat(verificationToken.getUser()).isEqualTo(user),
                () -> assertThat(verificationToken.getCreated().plusMinutes(validTime)).isEqualTo(verificationToken.getExpirationTime())
        );
    }
}