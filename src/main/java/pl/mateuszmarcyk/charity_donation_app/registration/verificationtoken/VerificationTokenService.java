package pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;


@RequiredArgsConstructor
@Service
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public void saveToken(VerificationToken token) {
        verificationTokenRepository.save(token);
    }

    public VerificationToken findByToken(String token) {
        return verificationTokenRepository.findByToken(token).orElseThrow(() -> new TokenNotFoundException("Invalid token provided"));
    }
}
