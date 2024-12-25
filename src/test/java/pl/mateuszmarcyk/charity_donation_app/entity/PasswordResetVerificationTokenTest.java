package pl.mateuszmarcyk.charity_donation_app.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PasswordResetVerificationTokenTest {


    @Test
    void givenPasswordResetVerificationTokenConstructor_whenCreateNewInstance_thenExpirationTimeIsCorrectlySet() {

        int tokenValidPeriodTimeInMinutes = 15;

        PasswordResetVerificationToken token = new PasswordResetVerificationToken(null, null, tokenValidPeriodTimeInMinutes);
        LocalDateTime createdTime = token.getCreated();
        LocalDateTime expectedExpirationTime = createdTime.plusMinutes(tokenValidPeriodTimeInMinutes);
        LocalDateTime expirationTime = token.getExpirationTime();

        assertAll(
                () -> assertThat(createdTime).isNotEqualTo(expirationTime),
                () -> assertThat(expirationTime).isEqualTo(expectedExpirationTime)
        );
    }
}