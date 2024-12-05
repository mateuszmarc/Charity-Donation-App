package pl.mateuszmarcyk.charity_donation_app.institution;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admins/institutions")
public class InstitutionController {

    private final UserService userService;
    private final InstitutionService institutionService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping
    public String showAllInstitutions(@AuthenticationPrincipal CustomUserDetails userDetails,  Model model) {

        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            List<Institution> institutions = institutionService.findAll();
            model.addAttribute("institutions", institutions);
            return "admin-institutions-all";
        }
        return "redirect:/index";
    }

    @GetMapping("/{id}")
    public String showInstitutionDetails(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            Institution institution = institutionService.findById(id);
            model.addAttribute("institution", institution);
            return "admin-institution-details";
        }

        return "redirect:/";
    }

    @GetMapping("/add")
    public String showInstitutionForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);
            model.addAttribute("institution", new Institution());

            return "admin-institution-form";
        }
        return "redirect:/";
    }

    @PostMapping("/add")
    public String processInstitutionForm(@Valid @ModelAttribute Institution institution,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model) {
        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            if (bindingResult.hasErrors()) {
                bindingResult.getAllErrors().forEach(System.out::println);
                return "admin-institution-form";
            }

            institutionService.saveInstitution(institution);
            return "redirect:/admins/institutions";
        }
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String showEditInstitutionForm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            Institution institution = institutionService.findById(id);
            model.addAttribute("institution", institution);

            return "admin-institution-form";
        }
        return "redirect:/";
    }

    @GetMapping("/delete/{id}")
    public String deleteInstitutionById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            institutionService.deleteById(id);

            return "redirect:/admins/institutions";

        }
        return "redirect:/";
    }

}