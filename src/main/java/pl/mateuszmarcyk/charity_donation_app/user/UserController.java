package pl.mateuszmarcyk.charity_donation_app.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public String showUserDetails(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            return "user-details-info";
        }
        return "redirect:/";
    }
}
