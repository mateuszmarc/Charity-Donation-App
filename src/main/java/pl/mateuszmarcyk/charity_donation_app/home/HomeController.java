package pl.mateuszmarcyk.charity_donation_app.home;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.mateuszmarcyk.charity_donation_app.donation.DonationService;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.institution.Institution;
import pl.mateuszmarcyk.charity_donation_app.institution.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/")
public class HomeController {

    private final UserService userService;
    private final DonationService donationService;
    private final InstitutionService institutionService;


    @GetMapping
    public String index(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("No user", "Could not find the user"));
            model.addAttribute("userProfile", user.getProfile());
            model.addAttribute("user", user);

            System.out.println(user.getUserTypes());
        }

        List<Institution> institutions = institutionService.findAll();
        long allDonations = donationService.countAllDonations();
        long allDonationBags = donationService.countAllBags();

        model.addAttribute("institutions", institutions);
        model.addAttribute("allDonations", allDonations);
        model.addAttribute("allDonationBags", allDonationBags);


        return "index";
    }

}
