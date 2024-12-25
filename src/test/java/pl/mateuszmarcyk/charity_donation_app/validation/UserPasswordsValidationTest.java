package pl.mateuszmarcyk.charity_donation_app.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
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
public class UserPasswordsValidationTest {

    @Autowired
    private Validator validator;

    @MockBean
    UserRepository userRepository;

    @BeforeEach
    void beforeEach() {
        String email = "example@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    }

    @Test
    void givenUserWithNullPasswordAndNullPasswordRepeat_thenGetNotNullPasswordViolation() {

//        1. combination
        User user = getUserInstance();
        user.setPassword(null);
        user.setPasswordRepeat(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(2);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            if (!propertyPath.equals("password")) {
                assertThat(propertyPath).isEqualTo("passwordRepeat");
            } else {
                assertThat(propertyPath).isEqualTo("password");
            }
            assertThat(annotationType).isEqualTo("interface jakarta.validation.constraints.NotNull");
        });
    }

    @Test
    void givenUserWithNullPassword_thenGetNotNullAndPasswordEqualViolations() {
//        2. combination

        User user = getUserInstance();
        user.setPassword(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(2);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            if (!annotationType.equals("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual")) {
                assertThat(annotationType).isEqualTo("interface jakarta.validation.constraints.NotNull");
                assertThat(propertyPath).isEqualTo("password");
            } else {
                assertThat(annotationType).isEqualTo("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual");
            }
        });
    }

    @Test
    void givenUserWithPasswordNullAndInvalidPasswordRepeat_thenGetThreeViolations() {
//        3. combination
        User user = getUserInstance();
        user.setPassword(null);
        user.setPasswordRepeat("Invalid");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(3);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            if (annotationType.equals("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual")) {
                assertThat(propertyPath).isEqualTo("");
            } else if (annotationType.equals("interface jakarta.validation.constraints.NotNull")) {
                assertThat(propertyPath).isEqualTo("password");
            } else {
                assertAll(
                        () -> assertThat(annotationType).isEqualTo("interface jakarta.validation.constraints.Pattern"),
                        () -> assertThat(propertyPath).isEqualTo("passwordRepeat")
                );

            }
        });

    }

    @Test
    void givenUserWithNullPasswordRepeat_thenGetPasswordEqualViolation() {
//        4. combination

        User user = getUserInstance();
        user.setPasswordRepeat(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(2);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            if (!annotationType.equals("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual")) {
                assertAll(
                        () -> assertThat(propertyPath).isEqualTo("passwordRepeat"),
                        () -> assertThat(annotationType).isEqualTo("interface jakarta.validation.constraints.NotNull")
                );
            } else {
                assertThat(annotationType).isEqualTo("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual");

            }
        });
    }

    @Test
    void givenUserWithIncorrectPasswordRepeat_thenGetTwoViolations() {
//        5. combination

        User user = getUserInstance();
        user.setPasswordRepeat("Incorrect");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(2);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            if (!annotationType.equals("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual")) {
                assertThat(propertyPath).isEqualTo("passwordRepeat");
                assertThat(annotationType).isEqualTo("interface jakarta.validation.constraints.Pattern");
            } else {
                assertThat(annotationType).isEqualTo("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual");
            }
        });
    }

    @Test
    void givenUserWithDifferentPasswordAndPasswordRepeat_thenGetPasswordEqualViolation() {
//        6. combination

        User user = getUserInstance();
        user.setPassword("DifferentPass12!");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(1);

        violations.forEach(violation -> {
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();
            assertThat(annotationType).isEqualTo("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual");
        });
    }

    @ParameterizedTest(name = "password = {0}")
    @CsvFileSource(resources = "/constraintparameters/invalid-passwords.csv")
    void givenUserWithInvalidPasswordAndPasswordRepeat_thenGetPatternViolation(String password) {
//        7. combination

        User user = getUserInstance();
        user.setPassword(password);
        user.setPasswordRepeat(password);

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).hasSize(2);

        for (ConstraintViolation<User> violation : violations) {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            if (!propertyPath.equals("password")) {
                assertThat(propertyPath).isEqualTo("passwordRepeat");
            } else {
                assertThat(propertyPath).isEqualTo("password");
            }

            assertThat(annotationType).isEqualTo("interface jakarta.validation.constraints.Pattern");
        }
    }

    @Test
    void givenUserWithInvalidPasswordAndDifferentInvalidPasswordRepeat_thenGetThreeViolations() {
//        8. combination
        User user = getUserInstance();
        user.setPassword("Incorrect");
        user.setPasswordRepeat("Different");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(3);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            if (annotationType.equals("interface jakarta.validation.constraints.Pattern")) {
                if (!propertyPath.equals("password")) {
                    assertThat(propertyPath).isEqualTo("passwordRepeat");
                } else {
                    assertThat(propertyPath).isEqualTo("password");
                }
            } else {
                assertThat(annotationType).isEqualTo("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual");
            }

        });
    }

    @Test
    void givenUserWithInvalidPasswordAndValidPasswordRepeat_thenGetTwoViolations() {
        User user = getUserInstance();
        user.setPassword("Invalid");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(2);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            if (annotationType.equals("interface jakarta.validation.constraints.Pattern")) {
                assertThat(propertyPath).isEqualTo("password");
            } else {
                assertThat(annotationType).isEqualTo("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.PasswordEqual");
            }
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
