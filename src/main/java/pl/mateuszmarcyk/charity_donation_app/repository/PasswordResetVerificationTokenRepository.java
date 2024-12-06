package pl.mateuszmarcyk.charity_donation_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.mateuszmarcyk.charity_donation_app.entity.PasswordResetVerificationToken;

import java.util.Optional;

public interface PasswordResetVerificationTokenRepository extends JpaRepository<PasswordResetVerificationToken, Long> {

    @Query("SELECT t FROM PasswordResetVerificationToken t WHERE t.token=:token")
    Optional<PasswordResetVerificationToken> findByToken(String token);
}
