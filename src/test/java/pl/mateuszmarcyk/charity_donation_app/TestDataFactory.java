package pl.mateuszmarcyk.charity_donation_app;

import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
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
                "testPW1!",
                LocalDateTime.of(2023, 11, 11, 12, 25, 11),
                "testPW1!",
                new HashSet<>(Set.of(userType)),
                userProfile,
                null,
                null,
                new ArrayList<>()
        );

        userProfile.setUser(user);
        return user;
    }
}
