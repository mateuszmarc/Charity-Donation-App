package pl.mateuszmarcyk.charity_donation_app.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import pl.mateuszmarcyk.charity_donation_app.config.ValidatorConfig;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(ValidatorConfig.class)
public class UniqueEmailValidationTest {

    @Autowired
    private Validator validator;

    @MockBean
    private UserRepository userRepository;

    @Test
    void givenUserWithNullEmail_thenGetNotNullViolation() {
        User user = getUserInstance();
        user.setEmail(null);

        checkViolations(user, "email", "interface jakarta.validation.constraints.NotNull");
    }

    @Test
    void givenUserWithInvalidEmail_thenGetEmailViolation() {
        User user = getUserInstance();
        user.setEmail("wrong.email");

        checkViolations(user, "email", "interface jakarta.validation.constraints.Email");
    }

    @Test
    void givenAlreadyPersistedUserWithEmailInDatabase_whenValidate_thenGetUniQueEmailViolation() {

        User user = getUserInstance();
        user.setId(1L);
        User userToReturn = getUserInstance();
        userToReturn.setId(2L);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(userToReturn));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);

        violations.forEach(violation -> {
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            assertThat(annotationType).isEqualTo("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.UniqueEmail");
        });
    }

    @Test
    void givenNewUserWithAlreadyUsedEmail_whenValidate_thenGetUniqueEmailViolation() {

        User user = getUserInstance();
        User userToReturn = getUserInstance();
        userToReturn.setId(2L);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(userToReturn));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);

        violations.forEach(violation -> {
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            assertThat(annotationType).isEqualTo("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.UniqueEmail");
        });
    }

    @Test
    void givenAlreadyPersistedUserWithAlreadyUsedEmail_whenValidate_thenGetNoViolation() {
        User user = getUserInstance();
        user.setId(1L);
        User userToReturn = getUserInstance();
        userToReturn.setId(1L);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(userToReturn));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    void givenNewUserWithEmailNotInDatabase_whenValidateUser_thenGetNoViolation() {
        User user = getUserInstance();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    void givenAlreadyPersistedUserWithEmailNotInDatabase_whenValidateUser_thenGetNoViolation() {
        User user = getUserInstance();
        user.setId(1L);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    private void checkViolations(User user, String propertyField, String annotation) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            assertAll(
                    () -> assertThat(propertyPath).isEqualTo(propertyField),
                    () -> assertThat(annotationType).isEqualTo(annotation)
            );
        });
    }

    public static User getUserInstance() {
        Set<UserType> userTypes = new HashSet<>();
        UserType userType = new UserType();
        userType.setId(1L);
        userType.setRole("ROLE_USER");
        userTypes.add(userType);

        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName("John");
        userProfile.setLastName("Doe");

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken("sampleVerificationToken");

        PasswordResetVerificationToken passwordResetToken = new PasswordResetVerificationToken();
        passwordResetToken.setToken("samplePasswordResetToken");

        List<Donation> donations = new ArrayList<>();
        Donation donation = new Donation();
        donation.setQuantity(5);
        donations.add(donation);

        return new User(
                "example@example.com",
                true,
                false,
                "StrongP@ssword1",
                "StrongP@ssword1",
                userTypes,
                userProfile,
                verificationToken,
                passwordResetToken,
                donations
        );
    }
}
