package pl.mateuszmarcyk.charity_donation_app.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.config.ValidatorConfig;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
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
        User user = TestDataFactory.getUserInstance();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }
}