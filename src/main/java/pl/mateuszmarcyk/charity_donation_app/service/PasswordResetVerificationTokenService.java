package pl.mateuszmarcyk.charity_donation_app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.entity.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.PasswordResetVerificationTokenRepository;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PasswordResetVerificationTokenService {

    private final PasswordResetVerificationTokenRepository passwordResetVerificationTokenRepository;
    private final MessageSource messageSource;

    public void save(PasswordResetVerificationToken passwordResetVerificationToken) {
        passwordResetVerificationTokenRepository.save(passwordResetVerificationToken);
    }

    public PasswordResetVerificationToken findByToken(String token) {
        String tokenErrorTitle = messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault());
        String tokenNotFoundMessage = messageSource.getMessage("error.tokennotfound.message", null, Locale.getDefault());
        return passwordResetVerificationTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(tokenErrorTitle , tokenNotFoundMessage));
    }
}
