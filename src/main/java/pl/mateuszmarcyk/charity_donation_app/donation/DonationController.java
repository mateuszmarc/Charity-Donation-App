package pl.mateuszmarcyk.charity_donation_app.donation;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.mateuszmarcyk.charity_donation_app.category.Category;
import pl.mateuszmarcyk.charity_donation_app.category.CategoryService;
import pl.mateuszmarcyk.charity_donation_app.institution.Institution;
import pl.mateuszmarcyk.charity_donation_app.institution.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/donate")
public class DonationController {

    private final UserService userService;
    private final InstitutionService institutionService;
    private final CategoryService categoryService;

    @GetMapping
    public String displayDonationForm(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String username = authentication.getName();

            User user = userService.findUserByEmail(username);
            UserProfile userProfile = user.getProfile();

            List<Category> allCategories = categoryService.findAll();
            List<Institution> allInstitutions = institutionService.findAll();

            model.addAttribute("userProfile", userProfile);
            model.addAttribute("user", user);
            model.addAttribute("donation", new Donation());
            model.addAttribute("institutions", allInstitutions);
            model.addAttribute("categories", allCategories);
        }

        return "donation-form";
    }
}