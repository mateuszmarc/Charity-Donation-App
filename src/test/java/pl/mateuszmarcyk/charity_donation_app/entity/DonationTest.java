package pl.mateuszmarcyk.charity_donation_app.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class DonationTest {

    @Test
    void givenDonation_whenRemoveCategory_thenCategoryRemoved() {

        Category category = new Category(1L, "Test", new ArrayList<>());

        Donation donation = getDonation(new ArrayList<>(List.of(category)));

        donation.removeCategory(category);

        assertAll(
                () -> assertThat(donation.getCategories()).isEmpty(),
                () -> assertThat(donation.getCategories()).doesNotContain(category)
        );
    }

    @Test
    void givenDonation_whenRemoveNullCategory_thenCategoryRemoved() {

        Category category = new Category(1L, "Test", new ArrayList<>());
        Category toRemove = null;

        Donation donation = getDonation(new ArrayList<>(List.of(category)));

        donation.removeCategory(toRemove);

        assertAll(
                () -> assertThat(donation.getCategories()).hasSize(1),
                () -> assertThat(donation.getCategories()).doesNotContain(toRemove)
        );
    }

    @ParameterizedTest(name = "dateTimeString={1}, expected={2}")
    @CsvFileSource(resources = "/donationparameters/datetime-data.csv")
    void givenDonation_whenGetCreatedDateTime_thenStringIsFormatted(String dateTimeString, String expected) {
        Donation donation = getDonation(new ArrayList<>());

        LocalDateTime testDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        donation.setCreated(testDateTime);

        String transformedTime = donation.getCreatedDateTime();

        assertThat(transformedTime).isEqualTo(expected);

    }

    @ParameterizedTest(name = "dateTimeString={1}, expected={2}")
    @CsvFileSource(resources = "/donationparameters/datetime-data.csv")
    void givenDonation_whenGetDonationPassedDateTime_thenStringIsFormatted(String dateTimeString, String expected) {
        Donation donation = getDonation(new ArrayList<>());

        LocalDateTime testDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        donation.setCreated(testDateTime);

        String transformedTime = donation.getCreatedDateTime();

        assertThat(transformedTime).isEqualTo(expected);
    }

    @Test
    void givenDonation_whenGetCategoriesString_thenStringFormatted() {
        List<Category> categories = getCategories();

        Donation donation = getDonation(categories);

        String expectedCategoriesString = "Jedzenie, Zabawki, Ubrania, Książki, Elektronika, Meble, Produkty Higieniczne, Przybory Szkolne, Produkty dla Dzieci, Sprzęt Sportowy";
        String formattedString = donation.getCategoriesString();

        assertThat(formattedString).isEqualTo(expectedCategoriesString);
    }

    private static ArrayList<Category> getCategories() {
        return new ArrayList<>(List.of(
                new Category(1L, "Jedzenie", null),
                new Category(2L, "Zabawki", null),
                new Category(3L, "Ubrania", null),
                new Category(4L, "Książki", null),
                new Category(5L, "Elektronika", null),
                new Category(6L, "Meble", null),
                new Category(7L, "Produkty Higieniczne", null),
                new Category(8L, "Przybory Szkolne", null),
                new Category(9L, "Produkty dla Dzieci", null),
                new Category(10L, "Sprzęt Sportowy", null)
        ));
    }

    @Test
    void givenDonation_whenGetCategoriesString_thenStringIsEmpty() {
        List<Category> categories = null;

        Donation donation = getDonation(categories);

        String expectedCategoriesString = "";
        String formattedString = donation.getCategoriesString();

        assertThat(formattedString).isEqualTo(expectedCategoriesString);
    }

    @Test
    void givenDonation_whenGetCategoriesStringFromEmptyCategories_thenStringIsEmpty() {
        List<Category> categories = new ArrayList<>();

        Donation donation = getDonation(categories);

        String expectedCategoriesString = "";
        String formattedString = donation.getCategoriesString();

        assertThat(formattedString).isEqualTo(expectedCategoriesString);
    }

    private static Donation getDonation(List<Category> categories) {
        return new Donation(
                LocalDateTime.parse("2024-12-24T12:00:00"),
                false,
                null,
                null,
                categories,
                "123456789",
                "Please call on arrival.",
                LocalTime.parse("10:30:00"),
                LocalDate.parse("2024-12-31"),
                "12-345",
                "Kindness City",
                "123 Charity Lane",
                5
        );
    }
}