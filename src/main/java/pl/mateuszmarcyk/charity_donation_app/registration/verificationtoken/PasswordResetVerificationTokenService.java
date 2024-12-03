package pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;

@Service
@RequiredArgsConstructor
public class PasswordResetVerificationTokenService {

    private final PasswordResetVerificationTokenRepository passwordResetVerificationTokenRepository;

    @Value("${error.tokennotfound.title}")
    private String tokenErrorTitle;

    @Value("${error.tokennotfound.message}")
    private String tokenNotFoundMessage;

    public void save(PasswordResetVerificationToken passwordResetVerificationToken) {
        passwordResetVerificationTokenRepository.save(passwordResetVerificationToken);
    }

    public PasswordResetVerificationToken findByToken(String token) {
        return passwordResetVerificationTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(tokenErrorTitle , tokenNotFoundMessage));
    }
}
