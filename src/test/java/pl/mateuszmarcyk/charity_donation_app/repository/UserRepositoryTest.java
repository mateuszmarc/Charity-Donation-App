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
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Transactional
@Sql(scripts = "classpath:setup-data.sql")
@DataJpaTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    TestEntityManager testEntityManager;

    @Autowired
    UserRepository userRepository;

    @Test
    void givenUserRepository_whenFindByEmail_thenUserIsPresent() {
        String emailFromSqlScript = "testuser@example.com";
        Long idFromScript = 2L;
        Optional<User> optionalUser = userRepository.findByEmail(emailFromSqlScript);

        assertAll(
                () -> assertThat(optionalUser).isPresent(),
                () -> assertThat(optionalUser.get().getId()).isEqualTo(idFromScript),
                () -> assertThat(optionalUser.get().getEmail()).isEqualTo(emailFromSqlScript)
        );
    }

    @Test
    void givenUserRepository_whenFindByEmail_thenUserIsEmpty() {
        String email = "wrong.email@example.com";
        Optional<User> optionalUser = userRepository.findByEmail(email);

        assertThat(optionalUser).isEmpty();
    }

    @Test
    void givenUserRepository_whenFindByNullEmail_thenUserIsEmpty() {
        String email = null;
        Optional<User> optionalUser = userRepository.findByEmail(email);

        assertThat(optionalUser).isEmpty();
    }


    @Test
    void givenUserRepository_whenFindUserByVerificationToken_thenUserIsPresent() {
        String verificationTokenFromSqlScript = "81c34626-3a06-4813-a292-c57dcdd6e04a";
        String userEmailFromSqlScript = "testuser@example.com";
        Long userIdFromScript = 2L;

        Optional<User> optionalUser = userRepository.findUserByVerificationToken_Token(verificationTokenFromSqlScript);

        assertAll(
                () -> assertThat(optionalUser).isPresent(),
                () -> assertThat(optionalUser.get().getId()).isEqualTo(userIdFromScript),
                () -> assertThat(optionalUser.get().getEmail()).isEqualTo(userEmailFromSqlScript)
        );
    }

    @Test
    void givenUserRepository_whenFindUserByVerificationToken_thenUserIsEmpty() {
        String verificationTokenFromSqlScript = "wrong token";

        Optional<User> optionalUser = userRepository.findUserByVerificationToken_Token(verificationTokenFromSqlScript);
        assertThat(optionalUser).isEmpty();
    }

    @Test
    void givenUserRepository_whenFindUserByNullVerificationToken_thenUserIsEmpty() {
        String verificationTokenFromSqlScript = null;

        Optional<User> optionalUser = userRepository.findUserByVerificationToken_Token(verificationTokenFromSqlScript);
        assertThat(optionalUser).isEmpty();
    }

    @Test
    void givenUserRepository_whenFindUsersByRoleNative_ThenListIsNotEmpty() {
        UserType userType = testEntityManager.find(UserType.class, 1L);
        List<User> users = userRepository.findUsersByRoleNative(userType.getRole());
        User foundUser = testEntityManager.find(User.class, 2L);

        assertAll(
                () -> assertThat(users).hasSize(1),
                () -> assertThat(users.get(0)).isEqualTo(foundUser)
        );

        users.forEach(user -> assertThat(user.getUserTypes()).contains(userType));
    }

    @Test
    void givenUserRepository_whenFindUsersByRoleNative_ThenListIsEmpty() {
        UserType userType = testEntityManager.find(UserType.class, 2L);
        List<User> users = userRepository.findUsersByRoleNative(userType.getRole());
        assertThat(users).isEmpty();
    }

    @Test
    void givenUserRepository_whenFindUsersByNullRoleNative_ThenListIsEmpty() {
        String userType = null;
        List<User> users = userRepository.findUsersByRoleNative(userType);
        assertThat(users).isEmpty();
    }

    @Test
    void givenUserRepository_whenFindUsersByWrongRoleNative_ThenListIsEmpty() {
        String userType = "ROLE_WRONG";
        List<User> users = userRepository.findUsersByRoleNative(userType);
        assertThat(users).isEmpty();
    }

    @Test
    void givenUserRepository_whenFindUsersWithTwoRolesByRoleNative_ThenListIsNotEmpty() {
        UserType userType = testEntityManager.find(UserType.class, 2L);
        User foundUser = testEntityManager.find(User.class, 2L);
        foundUser.getUserTypes().add(userType);
        testEntityManager.merge(foundUser);

        List<User> users = userRepository.findUsersByRoleNative(userType.getRole());

        assertAll(
                () -> assertThat(users).hasSize(1),
                () -> assertThat(users.get(0)).isEqualTo(foundUser)
        );

        users.forEach(user -> assertThat(user.getUserTypes()).contains(userType));
    }

    @Test
    void givenUserRepository_whenFindUsersWithOneRoleByRoleNative_ThenListIsNotEmpty() {
        UserType userType = testEntityManager.find(UserType.class, 2L);
        User foundUser = testEntityManager.find(User.class, 2L);
        foundUser.getUserTypes().clear();
        foundUser.getUserTypes().add(userType);
        testEntityManager.merge(foundUser);

        List<User> users = userRepository.findUsersByRoleNative(userType.getRole());

        assertAll(
                () -> assertThat(users).hasSize(1),
                () -> assertThat(users.get(0)).isEqualTo(foundUser)
        );

        users.forEach(user -> assertThat(user.getUserTypes()).contains(userType));
    }

    @Test
    void givenUserRepository_whenFindUsersWithOneRoleByRoleNative_ThenListIsEmpty() {
        UserType userUserType = testEntityManager.find(UserType.class, 1L);
        UserType adminUserType = testEntityManager.find(UserType.class, 2L);
        User foundUser = testEntityManager.find(User.class, 2L);
        foundUser.getUserTypes().clear();
        foundUser.getUserTypes().add(adminUserType);
        testEntityManager.merge(foundUser);

        List<User> users = userRepository.findUsersByRoleNative(userUserType.getRole());

        assertThat(users).isEmpty();
    }


    @Test
    void givenUserRepository_whenFindByProfileId_thenUserIsPresent() {
        Long profileIdFromScript = 1L;
        String userEmailFromSqlScript = "testuser@example.com";
        Long userIdFromScript = 2L;

        Optional<User> optionalUser = userRepository.findByProfileId(profileIdFromScript);

        assertAll(
                () -> assertThat(optionalUser).isPresent(),
                () -> assertThat(optionalUser.get().getProfile().getId()).isEqualTo(profileIdFromScript),
                () -> assertThat(optionalUser.get().getEmail()).isEqualTo(userEmailFromSqlScript),
                () -> assertThat(optionalUser.get().getId()).isEqualTo(userIdFromScript)
        );
    }

    @Test
    void givenUserRepository_whenFindByProfileId_thenUserIsEmpty() {
        Long profileId = null;

        Optional<User> optionalUser = userRepository.findByProfileId(profileId);
        assertThat(optionalUser).isEmpty();
    }

    @Test
    void givenUserRepository_whenFindByWrongProfileId_thenUserIsEmpty() {
        Long profileId = 111L;

        Optional<User> optionalUser = userRepository.findByProfileId(profileId);
        assertThat(optionalUser).isEmpty();
    }

    @Test
    void givenUserRepository_whenFindUserByPasswordResetVerificationToken_thenUserIsEmpty() {
        String passwordResetToken = "09c66d01-8430-43f4-b859-981f92995521";

        Optional<User> optionalUser = userRepository.findUserByPasswordResetVerificationToken(passwordResetToken);

        assertThat(optionalUser).isEmpty();
    }

    @Test
    void givenUserRepository_whenFindUserByNullPasswordResetVerificationToken_thenUserIsEmpty() {
        String passwordResetToken = null;

        Optional<User> optionalUser = userRepository.findUserByPasswordResetVerificationToken(passwordResetToken);

        assertThat(optionalUser).isEmpty();
    }

    @Test
    void givenUserRepository_whenFindUserByPasswordResetVerificationToken_thenUserIsPresent() {

        User user = testEntityManager.find(User.class, 2L);
        PasswordResetVerificationToken token = new PasswordResetVerificationToken(UUID.randomUUID().toString(), user, 15);

        user.setPasswordResetVerificationToken(token);
        testEntityManager.merge(user);

        Optional<User> optionalUser = userRepository.findUserByPasswordResetVerificationToken(token.getToken());


        assertAll(
                () -> assertThat(optionalUser).isPresent(),
                () -> assertThat(optionalUser.get().getPasswordResetVerificationToken().getToken()).isEqualTo(token.getToken())
        );
    }
}