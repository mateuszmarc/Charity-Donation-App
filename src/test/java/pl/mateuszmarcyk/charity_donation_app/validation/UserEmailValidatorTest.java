package pl.mateuszmarcyk.charity_donation_app.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import pl.mateuszmarcyk.charity_donation_app.config.ValidatorConfig;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.Email;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@SpringBootTest
@Import(ValidatorConfig.class)
class UserEmailValidatorTest {

    @Autowired
    private Validator validator;

    @MockBean
    private UserRepository userRepository;

    @Test
    void givenEmailWithNullEmail_whenValidateEmail_thenGetNotNullViolation() {
        Email email = new Email(null);

        Set<ConstraintViolation<Email>> violations = validator.validate(email);

        assertThat(violations).hasSize(1);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            assertAll(
                    () -> assertThat(propertyPath).isEqualTo("addressEmail"),
                    () -> assertThat(annotationType).isEqualTo("interface jakarta.validation.constraints.NotNull")
            );
        });
    }

    @Test
    void givenEmailWithInvalidEmailNotInDatabase_whenValidateEmail_thenGetEmailAndUserEmailViolations() {
        Email email = new Email("invalid");

        when(userRepository.findByEmail(email.getAddressEmail())).thenReturn(Optional.empty());

        Set<ConstraintViolation<Email>> violations = validator.validate(email);
        assertThat(violations).hasSize(2);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            if (annotationType.equals("interface jakarta.validation.constraints.Email")) {
                assertThat(propertyPath).isEqualTo("addressEmail");
            } else {
               assertThat(annotationType).isEqualTo("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.UserEmail");
            }
        });
    }

    @Test
    void givenEmailWithInvalidEmailInDatabase_whenValidateEmail_thenGetEmailViolation() {
        Email email = new Email("invalid");

        when(userRepository.findByEmail(email.getAddressEmail())).thenReturn(Optional.of(new User()));

        Set<ConstraintViolation<Email>> violations = validator.validate(email);

        assertThat(violations).hasSize(1);

        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            assertAll(
                    () -> assertThat(propertyPath).isEqualTo("addressEmail"),
                    () -> assertThat(annotationType).isEqualTo("interface jakarta.validation.constraints.Email")
            );
        });
    }

    @Test
    void givenEmailWithValidEmailButEmailNotInDatabase_whenValidateEmail_thenGetUserEmailViolation() {
        Email email = new Email("example@gmail.com");

        when(userRepository.findByEmail(email.getAddressEmail())).thenReturn(Optional.empty());

        Set<ConstraintViolation<Email>> violations = validator.validate(email);

        violations.forEach(violation -> {
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();

            assertThat(annotationType).isEqualTo("interface pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.UserEmail");
        });
    }

    @Test
    void givenEmailWithValidEmailAndEmailInDatabase_whenValidateEmail_thenNoViolations() {

        Email email = new Email("example@gmail.com");
        when(userRepository.findByEmail(email.getAddressEmail())).thenReturn(Optional.of(new User()));

        Set<ConstraintViolation<Email>> violations = validator.validate(email);

        assertThat(violations).isEmpty();
    }

}