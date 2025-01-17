package pl.mateuszmarcyk.charity_donation_app.util.constraintannotations;

import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class SpringConstraintValidatorFactoryIntegrationTest {

    @Autowired
    private SpringConstraintValidatorFactory springConstraintValidatorFactory;

    @Test
    void givenSpringManagedUserEmailConstraintValidator_whenGetInstance_thenReturnsBean() {
        // Act
        UserEmailConstraintValidator validator = springConstraintValidatorFactory.getInstance(UserEmailConstraintValidator.class);

        // Assert
        assertAll(
                () -> assertThat(validator).isNotNull(),
                () -> assertThat(validator).isInstanceOf(UserEmailConstraintValidator.class)
        );
    }

    @Test
    void givenSpringManagedPasswordEqualConstraintValidator_whenGetInstance_thenReturnsBean() {
        // Act
        PasswordEqualConstraintValidator validator = springConstraintValidatorFactory.getInstance(PasswordEqualConstraintValidator.class);

        // Assert
        assertAll(
                () -> assertThat(validator).isNotNull(),
                () -> assertThat(validator).isInstanceOf(PasswordEqualConstraintValidator.class)
        );
    }

    @Test
    void givenSpringManagedUniqueEmailConstraintValidator_whenGetInstance_thenReturnsBean() {
        // Act
        UniqueEmailConstraintValidator validator = springConstraintValidatorFactory.getInstance(UniqueEmailConstraintValidator.class);

        // Assert
        assertAll(
                () -> assertThat(validator).isNotNull(),
                () -> assertThat(validator).isInstanceOf(UniqueEmailConstraintValidator.class)
        );

    }

    @Test
    void givenNonSpringManagedConstraint_whenGetInstance_thenReturnsBean() {
        // Act
        NotNullValidator validator = springConstraintValidatorFactory.getInstance(NotNullValidator.class);

        // Assert
        assertAll(
                () -> assertThat(validator).isNotNull(),
                () -> assertThat(validator).isInstanceOf(NotNullValidator.class)
        );
    }

}