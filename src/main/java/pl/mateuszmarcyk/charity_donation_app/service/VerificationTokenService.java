package pl.mateuszmarcyk.charity_donation_app.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.VerificationTokenRepository;

import java.util.Locale;


@RequiredArgsConstructor
@Service
public class VerificationTokenService {

    private final MessageSource messageSource;
    private final VerificationTokenRepository verificationTokenRepository;


    @Transactional
    public void saveToken(VerificationToken token) {
        verificationTokenRepository.save(token);
    }

    public VerificationToken findByToken(String token) {
        String tokenErrorTitle = messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault());
        String tokenNotFoundMessage = messageSource.getMessage("error.tokennotfound.message", null, Locale.getDefault());

        return verificationTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException(tokenErrorTitle , tokenNotFoundMessage));
    }
}
