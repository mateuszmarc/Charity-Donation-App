package pl.mateuszmarcyk.charity_donation_app.repository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.mateuszmarcyk.charity_donation_app.entity.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
@Sql(scripts = "classpath:setup-data.sql")
@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class PasswordResetVerificationTokenRepositoryTest {

    @Autowired
    private PasswordResetVerificationTokenRepository passwordResetVerificationTokenRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void givenPasswordResetVerificationTokenRepository_whenFindByToken_thenTokenIsFound() {
        User user = testEntityManager.find(User.class, 2L);
        PasswordResetVerificationToken token = new PasswordResetVerificationToken(UUID.randomUUID().toString(), user, 15);

        PasswordResetVerificationToken persistedToken = testEntityManager.persist(token);

        Optional<PasswordResetVerificationToken> optionalToken = passwordResetVerificationTokenRepository.findByToken(token.getToken());

        assertAll(
                () -> assertThat(optionalToken).isPresent(),
                () ->  assertThat(optionalToken.get().getToken()).isEqualTo(persistedToken.getToken())
        );
    }

    @Test
    void givenPasswordResetVerificationTokenRepository_whenFindByToken_thenTokenIsNotFound() {
        String token = UUID.randomUUID().toString();
        Optional<PasswordResetVerificationToken> foundToken = passwordResetVerificationTokenRepository.findByToken(token);

        assertThat(foundToken).isEmpty();
    }

    @Test
    void givenPasswordResetVerificationTokenRepository_whenFindByTokenByNullToken_thenTokenIsNotFound() {
        String token = null;
        Optional<PasswordResetVerificationToken> foundToken = passwordResetVerificationTokenRepository.findByToken(token);

        assertThat(foundToken).isEmpty();
    }

    @Test
    void givenPasswordResetVerificationTokenRepository_whenFindByEmptyToken_thenTokenIsNotFound() {
        String token = "";
        Optional<PasswordResetVerificationToken> foundToken = passwordResetVerificationTokenRepository.findByToken(token);

        assertThat(foundToken).isEmpty();
    }
}