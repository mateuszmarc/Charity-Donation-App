package pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    public void saveToken(VerificationToken token) {
        verificationTokenRepository.save(token);
    }
}
