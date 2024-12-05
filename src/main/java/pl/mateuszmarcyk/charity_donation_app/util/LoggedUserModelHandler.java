package pl.mateuszmarcyk.charity_donation_app.util;

import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;

@Component
public class LoggedUserModelHandler {

    public static User getUser(CustomUserDetails userDetails, Model model) {
        User user = userDetails.getUser();
        UserProfile userProfile = user.getProfile();

        model.addAttribute("user", user);
        model.addAttribute("userProfile", userProfile);
        return user;
    }
}
