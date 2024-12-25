package pl.mateuszmarcyk.charity_donation_app.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import pl.mateuszmarcyk.charity_donation_app.entity.Category;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class DonationValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithNullQuantity_thenShouldGetViolation() {

        Donation donation = getDonation();
        donation.setQuantity(null);

        checkViolations(donation, "quantity", "interface jakarta.validation.constraints.NotNull");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithInvalidQuantity_thenShouldGetViolation() {

        Donation donation = getDonation();
        donation.setQuantity(0);

        checkViolations(donation, "quantity", "interface jakarta.validation.constraints.Min");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithNegativeQuantity_thenShouldGetViolation() {

        Donation donation = getDonation();
        donation.setQuantity(-1);

        checkViolations(donation, "quantity", "interface jakarta.validation.constraints.Min");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithNullStreet_thenShouldGetViolation() {

        Donation donation = getDonation();
        donation.setStreet(null);

        checkViolations(donation, "street", "interface jakarta.validation.constraints.NotNull");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithNullCity_thenShouldGetViolation() {

        Donation donation = getDonation();
        donation.setCity(null);
        checkViolations(donation, "city", "interface jakarta.validation.constraints.NotNull");
    }

    @ParameterizedTest(name = "zipcode = {0}")
    @CsvFileSource(resources = "/donationparameters/invalid-postal-codes.csv")
    void givenDonationConstructor_whenCreatingNewInstanceWithInvalidZipCode_thenShouldGetViolation(String zipCode) {

        Donation donation = getDonation();
        donation.setZipCode(zipCode);

        checkViolations(donation, "zipCode", "interface jakarta.validation.constraints.Pattern");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithValidZipCode_thenShouldGetNoViolation() {

        Donation donation = getDonation();
        donation.setZipCode("25-026");

        Set<ConstraintViolation<Donation>> violations = validator.validate(donation);
        assertThat(violations).isEmpty();
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithNullPickUpDate_thenShouldGetViolation() {

        Donation donation = getDonation();
        donation.setPickUpDate(null);

        checkViolations(donation, "pickUpDate", "interface jakarta.validation.constraints.NotNull");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithTodayPickUpDate_thenShouldGetViolation() {
        Donation donation = getDonation();
        LocalDate date = LocalDate.now();
        donation.setPickUpDate(date);

        checkViolations(donation, "pickUpDate", "interface jakarta.validation.constraints.Future");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithPastPickUpDate_thenShouldGetViolation() {
        Donation donation = getDonation();
        LocalDate date = LocalDate.now().minusDays(1);
        donation.setPickUpDate(date);

        checkViolations(donation, "pickUpDate", "interface jakarta.validation.constraints.Future");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithNullPickUpTime_thenShouldGetViolation() {

        Donation donation = getDonation();
        donation.setPickUpTime(null);

        checkViolations(donation, "pickUpTime", "interface jakarta.validation.constraints.NotNull");
    }

    @ParameterizedTest(name = "number = {0}")
    @CsvFileSource(resources = "/donationparameters/invalid-phone-numbers.csv")
    void givenDonationConstructor_whenCreatingNewInstanceWithInvalidPhoneNumber_thenShouldGetViolation(String number) {

        Donation donation = getDonation();
        donation.setPhoneNumber(number);

        checkViolations(donation, "phoneNumber", "interface jakarta.validation.constraints.Pattern");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithValidPhoneNumber_thenShouldGetNoViolation() {

        Donation donation = getDonation();
        donation.setPhoneNumber("777888999");

        Set<ConstraintViolation<Donation>> violations = validator.validate(donation);
        assertThat(violations).isEmpty();
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithNullCategories_thenShouldGetViolation() {

        Donation donation = getDonation();
        donation.setCategories(null);

        checkViolations(donation, "categories", "interface jakarta.validation.constraints.NotEmpty");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithEmptyCategories_thenShouldGetViolation() {

        Donation donation = getDonation();
        donation.setCategories(new ArrayList<>());

        Set<ConstraintViolation<Donation>> violations = validator.validate(donation);

        checkViolations(donation, "categories", "interface jakarta.validation.constraints.NotEmpty");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithNullInstitution_thenShouldGetViolation() {

        Donation donation = getDonation();
        donation.setInstitution(null);

        checkViolations(donation, "institution", "interface jakarta.validation.constraints.NotNull");
    }

    @Test
    void givenDonationConstructor_whenCreatingNewInstanceWithValidParameters_thenShouldGetNoViolation() {

        Donation donation = getDonation();

        Set<ConstraintViolation<Donation>> violations = validator.validate(donation);
        assertThat(violations).isEmpty();
    }


    private void checkViolations(Donation donation, String invalidProperyField, String annotation) {
        Set<ConstraintViolation<Donation>> violations = validator.validate(donation);

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


    private static Donation getDonation() {
        User user = new User();
        Institution institution = new Institution();
        Category category = new Category();
        List<Category> categories = List.of(category);

        return new Donation(
                null,
                false,
                user,
                institution,
                categories,
                "123456789",
                "Pickup Comment",
                LocalTime.of(10, 0),
                LocalDate.of(2024, 12, 31),
                "12-345",
                "Sample City",
                "Sample Street",
                5
        );
    }



}