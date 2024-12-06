package pl.mateuszmarcyk.charity_donation_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

}
