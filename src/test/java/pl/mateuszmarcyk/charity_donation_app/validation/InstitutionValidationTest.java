package pl.mateuszmarcyk.charity_donation_app.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;

import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class InstitutionValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void givenInstitutionConstructor_whenCreateNewInstanceWithNullName_thenGetNotNullViolation() {
        Institution institution = getInstitution();
        institution.setName(null);

        checkViolations(institution, "name", "interface jakarta.validation.constraints.NotNull" );
    }

    @Test
    void givenInstitutionConstructor_whenCreateNewInstanceWithNullDescription_thenGetNotNullViolation() {
        Institution institution = getInstitution();
        institution.setDescription(null);

        checkViolations(institution, "description", "interface jakarta.validation.constraints.NotNull");
    }

    @Test
    void givenInstitutionConstructor_whenCreateNewInstanceWithValidParameters_thenGetNoViolations() {
        Institution institution = getInstitution();

        Set<ConstraintViolation<Institution>> violations = validator.validate(institution);

        assertThat(violations).isEmpty();
    }

    private Institution getInstitution() {
        return  new Institution(1L, "Pomocna Dłoń", "Description", new ArrayList<>());
    }

    private void checkViolations(Institution institution, String invalidProperyField, String annotation) {
        Set<ConstraintViolation<Institution>> violations = validator.validate(institution);

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