package pl.mateuszmarcyk.charity_donation_app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.FileUploadUtil;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;
import pl.mateuszmarcyk.charity_donation_app.util.LogoutHandler;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final DonationService donationService;
    private final FileUploadUtil fileUploadUtil;
    private final LoggedUserModelHandler loggedUserModelHandler;
    private final LogoutHandler logoutHandler;

    @GetMapping("/profile")
    public String showUserDetails(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User loggedUser = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(loggedUser, model);

        return "user-profile-details-info";
    }

    @GetMapping("/profile/edit")
    public String showUserProfileEditForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User loggedUser = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(loggedUser, model);

        return "user-profile-edit-form";
    }


    @PostMapping("/profile/edit")
    public String processUserProfileEditForm(@ModelAttribute(name = "userProfile") UserProfile profileToEdit,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         @RequestParam("image") MultipartFile image) throws IOException {

        User loggedUser = loggedUserModelHandler.getUser(userDetails);

        fileUploadUtil.saveImage(profileToEdit, image, loggedUser);

        return "redirect:/profile";
    }

    @GetMapping("/account/edit")
    public String showUserAccountEditForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User loggedUser = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(loggedUser, model);
        loggedUser.setPasswordRepeat(loggedUser.getPassword());

        return "user-account-edit-form";
    }

    @PostMapping("/account/change-password")
    public String processUserChangePasswordForm(@Valid @ModelAttribute(name = "user") User userToEdit,
                                                BindingResult bindingResult,
                                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                                Model model) {

        User loggedUser = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(loggedUser, model);

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> log.info("{}", error));
            return "user-account-edit-form";
        }

        userService.changePassword(userToEdit);

        return "redirect:/profile";
    }

    @PostMapping("/account/change-email")
    public String processUserChangeEmailForm(@Valid @ModelAttribute(name = "user") User userToEdit,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        User loggedUser = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(loggedUser, model);

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> log.info("{}", error));
            return "user-account-edit-form";
        }
        log.info(userToEdit.getEmail());
        log.info(userToEdit.getPassword());

        userService.changeEmail(userToEdit);

        logoutHandler.performLogout(request, response, authentication);

        return "redirect:/";
    }


    @GetMapping("/donations")
    public String showAllDonations(@AuthenticationPrincipal CustomUserDetails userDetails, Model model, HttpServletRequest request) {

        User loggedUser = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(loggedUser, model);

        String sortType = request.getParameter("sortType");

        List<Donation> donations = donationService.getDonationsForUserSortedBy(sortType, loggedUser);
        model.addAttribute("donations", donations);

        return "user-donations";
    }

    @GetMapping("/donations/{id}")
    public String showDonationDetails(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        User loggedUser = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(loggedUser, model);

        Donation donation = donationService.getUserDonationById(loggedUser, id);
        model.addAttribute("donation", donation);

        return "user-donation-details";
    }


    @PostMapping("/donations/archive")
    public String archiveDonation(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(name = "donationId") Long id) {

        User loggedUser = loggedUserModelHandler.getUser(userDetails);

        Donation donationToArchive = donationService.getUserDonationById(loggedUser, id);
        donationService.archiveDonation(donationToArchive);

        return "redirect:/donations";
    }

    @PostMapping("/account/delete")
    public String deleteYourself(HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String userEmail = authentication.getName();
        User loggedUser = userService.findUserByEmail(userEmail);

        userService.deleteUser(loggedUser.getId());

        logoutHandler.performLogout(request, response, authentication);


        return "redirect:/";
    }


    @PostMapping("/account/downgrade")
    public String downgradeYourself(HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User loggedUser = userService.findUserByEmail(email);

        userService.removeAdminRole(loggedUser.getId());

        logoutHandler.performLogout(request, response, authentication);

        return "redirect:/";
    }
}
