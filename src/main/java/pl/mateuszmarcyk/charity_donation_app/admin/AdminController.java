package pl.mateuszmarcyk.charity_donation_app.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admins")
public class AdminController {

    private final UserService userService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();
            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            return "admin-dashboard";
        }
        return "index";
    }
}
