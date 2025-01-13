package pl.mateuszmarcyk.charity_donation_app;

import org.springframework.ui.Model;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

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
                LocalDate.now().plusDays(5),
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

    public static void stubLoggedUserModelHandlerMethodsInvocation(LoggedUserModelHandler loggedUserModelHandler, User loggedInUser) {
        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));
    }

    public static void assertEmptyDonation(Donation donation) {
        assertAll(
                () -> assertThat(donation.getId()).isNull(),
                () -> assertThat(donation.getQuantity()).isNull(),
                () -> assertThat(donation.getStreet()).isNull(),
                () -> assertThat(donation.getCity()).isNull(),
                () -> assertThat(donation.getZipCode()).isNull(),
                () -> assertThat(donation.getPickUpDate()).isNull(),
                () -> assertThat(donation.getPickUpTime()).isNull(),
                () -> assertThat(donation.getPickUpComment()).isNull(),
                () -> assertThat(donation.getPhoneNumber()).isNull(),
                () -> assertThat(donation.getCategories()).isNull(),
                () -> assertThat(donation.getInstitution()).isNull(),
                () -> assertThat(donation.getUser()).isNull(),
                () -> assertThat(donation.getCreated()).isNull(),
                () -> assertThat(donation.isReceived()).isFalse(),
                () -> assertThat(donation.getDonationPassedTime()).isNull()
        );
    }
}
