package pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetVerificationTokenRepository extends JpaRepository<PasswordResetVerificationToken, Long> {
}
