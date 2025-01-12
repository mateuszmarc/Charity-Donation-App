package pl.mateuszmarcyk.charity_donation_app;

import pl.mateuszmarcyk.charity_donation_app.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestDataFactory {

    public static User getUser() {
        UserProfile userProfile = new UserProfile(2L, null, "Mateusz", "Marcykiewicz", "Kielce",
                "Poland", null, "555666777");
        UserType userType = new UserType(2L, "ROLE_USER", new ArrayList<>());
        User user = new User(
                1L,
                "test@email.com",
                true,
                false,
                "testPW123!!",
                LocalDateTime.of(2023, 11, 11, 12, 25, 11),
                "testPW123!!",
                new HashSet<>(Set.of(userType)),
                userProfile,
                null,
                null,
                new ArrayList<>()
        );

        userProfile.setUser(user);
        return user;
    }

    public static UserProfile getUserProfile() {
        return new UserProfile(2L, null, "Mateusz", "Marcykiewicz", "Kielce",
                "Poland", null, "555666777");
    }

    public static Donation getDonation() {
        Institution institution = new Institution(1L, "Pomocna Dłoń", "Description", new ArrayList<>());
        User user = new User();
        user.setDonations(new ArrayList<>());
        Category category = new Category(1L, "Jedzenie", new ArrayList<>());

        Donation donationOne = new Donation(
                LocalDateTime.parse("2024-12-24T12:00:00"),
                false,
                user,
                institution,
                new ArrayList<>(List.of(category)),
                "123456789",
                "Please call on arrival.",
                LocalTime.parse("10:30:00"),
                LocalDate.parse("2024-12-31"),
                "12-345",
                "Kindness City",
                "123 Charity Lane",
                10
        );
        donationOne.setId(1L);

        institution.getDonations().add(donationOne);
        donationOne.setInstitution(institution);
        donationOne.setCreated(LocalDateTime.now());

        user.getDonations().add(donationOne);
        donationOne.setUser(user);

        category.getDonations().add(donationOne);
        donationOne.getCategories().add(category);

        return donationOne;
    }

    public static Category getCategory() {
        return new Category(1L, "CategoryName", new ArrayList<>());
    }

    public static Institution getInstitution() {
        return new Institution(1L, "test name", "test description", new ArrayList<>());
    }
}
