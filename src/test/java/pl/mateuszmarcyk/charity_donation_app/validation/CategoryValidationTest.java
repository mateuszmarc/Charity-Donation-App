package pl.mateuszmarcyk.charity_donation_app.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.mateuszmarcyk.charity_donation_app.entity.Category;

import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class CategoryValidationTest {


    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void whenCreateCategory_thenTestNotNullNameValidator_thenGetViolation() {
        Category category = new Category(1L, null, new ArrayList<>());

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertThat(violations).hasSize(1);
        violations.forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String annotationType = violation.getConstraintDescriptor().getAnnotation().annotationType().toString();


            assertAll(
                    () -> assertThat(propertyPath).isEqualTo("name"),
                    () -> assertThat(annotationType).isEqualTo("interface jakarta.validation.constraints.NotNull")
            );
        });
    }

    @Test
    public void whenCreateCategory_thenTestNotNullNameValidator_thenValidName() {
        Category category = new Category(1L, "Valid CategoryName", new ArrayList<>());

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertThat(violations).isEmpty();
    }
}