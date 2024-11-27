package pl.mateuszmarcyk.charity_donation_app.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;

import java.util.List;

@Slf4j
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

    @GetMapping("/all-admins")
    public String showAllAdmins(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();


            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();
            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            List<User> admins = userService.findAllAdmins(user);

            model.addAttribute("admins", admins);

            return "admins-all";
        }
        return "redirect:/";
    }

    @GetMapping("/all-admins/{id}")
    private String showAdminDetails(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();


            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();
            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            User admin = userService.findUserById(id);
            model.addAttribute("admin", admin);

            return "admin-details";
        }
        return "redirect:/";
    }
}
