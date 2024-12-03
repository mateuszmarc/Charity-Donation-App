package pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetVerificationTokenService {

    private final PasswordResetVerificationTokenRepository passwordResetVerificationTokenRepository;

    public void save(PasswordResetVerificationToken passwordResetVerificationToken) {
        passwordResetVerificationTokenRepository.save(passwordResetVerificationToken);
    }
}
