package pl.mateuszmarcyk.charity_donation_app.util;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;

@Component
public class LoggedUserModelHandler {

    public static User getUser(CustomUserDetails userDetails) {
        return userDetails.getUser();
    }

    public static void addUserToModel(User user, Model model) {
        UserProfile userProfile = user.getProfile();
        model.addAttribute("user", user);
        model.addAttribute("userProfile", userProfile);
    }
}
