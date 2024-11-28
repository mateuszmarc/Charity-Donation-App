package pl.mateuszmarcyk.charity_donation_app.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfileService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/admins")
public class AdminController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

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

    @GetMapping("/all-admins/edit/{id}")
    public String showAdminEditForm(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();


            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();
            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            User userToEdit = userService.findUserById(id);
            userToEdit.setPasswordRepeat(userToEdit.getPassword());
            model.addAttribute("userToEdit", userToEdit);

            return "admin-form";
        }

        return "redirect:/";
    }

    @PostMapping("/all-admins/edit")
    public String processAdminEditForm(@Valid @ModelAttribute(name = "userToEdit") User userToEdit, BindingResult bindingResult, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            System.out.println("Password " + userToEdit.getPassword());
            System.out.println("Password repeat " + userToEdit.getPasswordRepeat());

            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            if (bindingResult.hasErrors()) {
                bindingResult.getAllErrors().forEach(System.out::println);
                return "admin-form";
            }

            userService.updateUserEmail(userToEdit);
            return "redirect:/admins/all-admins/%d".formatted(userToEdit.getId());
        }

        return "redirect:/";
    }

    @GetMapping("/all-admins/change-password/{id}")
    public String showChangePasswordForm(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();


            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            User userToEdit = userService.findUserById(id);
            model.addAttribute("userToEdit", userToEdit);

            return "admin-change-password-form";
        }

        return "redirect:/";
    }

    @PostMapping("/all-admins/change-password")
    public String processChangePasswordForm(@Valid @ModelAttribute(name = "userToEdit") User userToEdit, BindingResult bindingResult, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();


            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            if (bindingResult.hasErrors()) {
                bindingResult.getAllErrors().forEach(System.out::println);
                return "admin-change-password-form";
            }

            userService.changePassword(userToEdit);

            return "redirect:/admins/all-admins/%d".formatted(userToEdit.getId());
        }
        return "redirect:/";
    }

    @GetMapping("/all-admins/profiles/edit/{id}")
    private String showAdminDetailsEditForm(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();


            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            UserProfile userProfileToEdit = userProfileService.findById(id);

            model.addAttribute("profile", userProfileToEdit);

            return "user-profile-form";
        }
        return "redirect:/";
    }
}