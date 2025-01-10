package pl.mateuszmarcyk.charity_donation_app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.Category;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.service.CategoryService;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;
import pl.mateuszmarcyk.charity_donation_app.util.MessageDTO;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Controller
@RequestMapping("/donate")
public class DonationController {

    private final InstitutionService institutionService;
    private final CategoryService categoryService;
    private final DonationService donationService;
    private final UserService userService;
    private final MessageSource messageSource;
    private final LoggedUserModelHandler loggedUserModelHandler;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @ModelAttribute(name = "message")
    public MessageDTO getMessage() {
        return new MessageDTO();
    }

    @GetMapping
    public String showDonationForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User loggedUser = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(loggedUser, model);

        List<Category> allCategories = categoryService.findAll();
        List<Institution> allInstitutions = institutionService.findAll();

        model.addAttribute("donation", new Donation());
        model.addAttribute("institutions", allInstitutions);
        model.addAttribute("allCategories", allCategories);

        return "user-donation-form";
    }

    @PostMapping
    private String processDonationForm(@Valid @ModelAttribute(name = "donation") Donation donation,
                                       BindingResult bindingResult,
                                       @AuthenticationPrincipal CustomUserDetails userDetails,
                                       Model model) {


        User loggedUser = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(loggedUser, model);

        if (bindingResult.hasErrors()) {
            List<Category> allCategories = categoryService.findAll();
            List<Institution> allInstitutions = institutionService.findAll();
            model.addAttribute("institutions", allInstitutions);
            model.addAttribute("allCategories", allCategories);

            String errorMessage = messageSource.getMessage("donation.form.error.message", null, Locale.getDefault());
            model.addAttribute("errorMessage", errorMessage);
            return "user-donation-form";
        }

        User user = userService.findUserById(userDetails.getUser().getId());
        donation.setUser(user);
        donationService.save(donation);
        return "form-confirmation";

    }
}
