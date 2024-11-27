package pl.mateuszmarcyk.charity_donation_app.institution;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admins/institutions")
public class InstitutionController {

    private final UserService userService;
    private final InstitutionService institutionService;

    @GetMapping
    public String showAllInstitutions(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            List<Institution> institutions = institutionService.findAll();
            model.addAttribute("institutions", institutions);
            return "institutions-all";
        }
        return "redirect:/index";
    }

    @GetMapping("/{id}")
    public String showInstitutionDetails(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();
            Institution institution = institutionService.findById(id);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);
            model.addAttribute("institution", institution);
            return "institution-details";
        }

        return "redirect:/";
    }

    @GetMapping("/add")
    public String showInstitutionForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);
            model.addAttribute("institution", new Institution());

            return "institution-form";
        }
        return "redirect:/";
    }

    @PostMapping("/add")
    public String processInstitutionForm(@ModelAttribute Institution institution, BindingResult bindingResult, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            if (bindingResult.hasErrors()) {
                return "institution-form";
            }

            institutionService.saveInstitution(institution);
            return "redirect:/admins/institutions";
        }
        return "redirect:/";
    }
}
