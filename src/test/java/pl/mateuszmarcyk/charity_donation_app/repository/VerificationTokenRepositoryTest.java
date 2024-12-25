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
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@Transactional
@Sql(scripts = "classpath:setup-data.sql")
@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class VerificationTokenRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Test
    void givenVerificationTokenRepository_whenFindByToken_thenTokenIsEmpty() {
        String token = "ac0596ff-958d-481a-bf11-39ac6874248f";

        Optional<VerificationToken> optionalVerificationToken = verificationTokenRepository.findByToken(token);

        assertThat(optionalVerificationToken).isEmpty();
    }

    @Test
    void givenVerificationTokenRepository_whenFindByNullToken_thenTokenIsEmpty() {
        String token = null;

        Optional<VerificationToken> optionalVerificationToken = verificationTokenRepository.findByToken(token);

        assertThat(optionalVerificationToken).isEmpty();
    }

    @Test
    void givenVerificationTokenRepository_whenFindByToken_thenTokenIsPresent() {
        String tokenFromSqlScript = "81c34626-3a06-4813-a292-c57dcdd6e04a";
        Long tokenIdFromScript = 1L;

        Optional<VerificationToken> optionalVerificationToken = verificationTokenRepository.findByToken(tokenFromSqlScript);

        assertAll(
                () -> assertThat(optionalVerificationToken).isPresent(),
                () -> assertThat(optionalVerificationToken.get().getId()).isEqualTo(tokenIdFromScript)
        );

    }
}