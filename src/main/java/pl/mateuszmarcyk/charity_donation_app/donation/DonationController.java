package pl.mateuszmarcyk.charity_donation_app.donation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.mateuszmarcyk.charity_donation_app.category.Category;
import pl.mateuszmarcyk.charity_donation_app.category.CategoryService;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.institution.Institution;
import pl.mateuszmarcyk.charity_donation_app.institution.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/donate")
public class DonationController {

    private final InstitutionService institutionService;
    private final CategoryService categoryService;
    private final DonationService donationService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping
    public String displayDonationForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            List<Category> allCategories = categoryService.findAll();
            List<Institution> allInstitutions = institutionService.findAll();

            model.addAttribute("donation", new Donation());
            model.addAttribute("institutions", allInstitutions);
            model.addAttribute("allCategories", allCategories);

            return "donation-form";
        }
        return "redirect:/";
    }

    @PostMapping
    private String processDonationForm(@Valid @ModelAttribute(name = "donation") Donation donation,
                                       BindingResult bindingResult,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       Model model) {

        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails, model);
            donation.setUser(user);


            if (bindingResult.hasErrors()) {
                List<Category> allCategories = categoryService.findAll();
                List<Institution> allInstitutions = institutionService.findAll();
                model.addAttribute("institutions", allInstitutions);
                model.addAttribute("allCategories", allCategories);

                return "donation-form";
            }

            donationService.save(donation);

            return "form-confirmation";
        }
        return "redirect:/";
    }
}
