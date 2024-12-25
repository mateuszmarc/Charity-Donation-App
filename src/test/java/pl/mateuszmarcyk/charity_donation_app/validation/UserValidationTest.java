package pl.mateuszmarcyk.charity_donation_app.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
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
class UserValidationTest {

    @Autowired
    private Validator validator;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        String email = "example@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
    }

    @Test
    void givenUserConstructor_whenCreateUserWithProperFields_thenGetNoViolations() {
        User user = getUserInstance();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
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

    private void checkViolations(User user, String invalidProperyField, String annotation) {
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).hasSize(1);
        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();


            assertAll(
                    () -> assertThat(propertyPath).isEqualTo(invalidProperyField),
                    () -> assertThat(annotationType).isEqualTo(annotation)
            );
        });
    }
}