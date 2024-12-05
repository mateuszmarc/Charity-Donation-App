package pl.mateuszmarcyk.charity_donation_app.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.donation.Donation;
import pl.mateuszmarcyk.charity_donation_app.donation.DonationService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.util.FileUploadUtil;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final DonationService donationService;
    private final FileUploadUtil fileUploadUtil;


    @GetMapping("/profile")
    public String showUserDetails(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            return "user-details-info";
        }
        return "redirect:/";
    }

    @GetMapping("/profile/edit")
    public String displayProfileEditForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            return "user-profile-edit-form";
        }
        return "redirect:/";
    }

    @PostMapping("/profile/edit")
    public String processProfileEditForm(@Valid @ModelAttribute(name = "userProfile") UserProfile profileToEdit,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model,
                                         @RequestParam("image") MultipartFile image) {

        if (userDetails != null) {
            User loggedUser = LoggedUserModelHandler.getUser(userDetails, model);

            if (bindingResult.hasErrors()) {
                return "user-profile-edit-form";
            }

            loggedUser.setProfile(profileToEdit);

            fileUploadUtil.saveImage(profileToEdit, image, loggedUser);

            return "user-profile-edit-form";
        }
        return "redirect:/";
    }

    @GetMapping("/account/edit")
    public String showUserEditAccountForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails != null) {
            User loggedUser = LoggedUserModelHandler.getUser(userDetails, model);
            loggedUser.setPasswordRepeat(loggedUser.getPassword());
            model.addAttribute("userToEdit", loggedUser);

            return "user-account-edit";
        }

        return "redirect:/";
    }

    @PostMapping("/account/change-password")
    public String processUserChangePasswordForm(@Valid @ModelAttribute(name = "userToEdit") User userToEdit,
                                                BindingResult bindingResult,
                                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                                Model model) {
        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            if (bindingResult.hasErrors()) {
                bindingResult.getAllErrors().forEach(System.out::println);
                return "user-account-edit";
            }

            userService.changePassword(userToEdit);

            return "redirect:/profile";
        }
        return "redirect:/";
    }

    @PostMapping("/account/change-email")
    public String processUserChangeEmail(@Valid @ModelAttribute(name = "userToEdit") User userToEdit,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            if (bindingResult.hasErrors()) {
                bindingResult.getAllErrors().forEach(System.out::println);
                return "user-account-edit";
            }
            System.out.println(userToEdit.getEmail());
            System.out.println(userToEdit.getPassword());
            User updatedUser = userService.updateUserEmail(userToEdit);

            UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(
                    updatedUser.getEmail(),
                    authentication.getCredentials(),
                    authentication.getAuthorities()
            );

            SecurityContextHolder.getContext().setAuthentication(newAuthentication);
            return "redirect:/profile";
        }
        return "redirect:/";
    }

    @GetMapping("/donations")
    public String showAllDonations(@AuthenticationPrincipal CustomUserDetails userDetails, Model model, HttpServletRequest request) {
        if (userDetails != null) {
            User loggedUser =  LoggedUserModelHandler.getUser(userDetails, model);

            String sortType = request.getParameter("sortType");


            List<Donation> donations = donationService.getDonationsForUserSortedBy(sortType, loggedUser);
            model.addAttribute("donations", donations);

            return "user-donations";

        }
        return "redirect:/";
    }

    @GetMapping("/donations/{id}")
    public String showDonationDetails(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            Donation donation = donationService.getDonationById(id);
            model.addAttribute("donation", donation);

            return "user-donation-details";
        }
        return "redirect:/";
    }

    @PostMapping("/donations/archive")
    public String archiveDonation(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request, Model model) {
        if (userDetails != null) {
            LoggedUserModelHandler.getUser(userDetails, model);

            Long id = Long.parseLong(request.getParameter("donationId"));

            Donation donationToArchive = donationService.getDonationById(id);
            donationService.archiveDonation(donationToArchive);

            return "redirect:/donations";
        }

        return "redirect:/";
    }

    @PostMapping("/account/delete")
    public String deleteUser(@RequestParam(name = "id") Long id,
                             Model model,
                             HttpServletRequest request,
                             HttpServletResponse response) {

        System.out.println(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String userEmail = authentication.getName();
            User loggedUser = userService.findUserByEmail(userEmail);

            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", loggedUser.getProfile());

            userService.deleteUser(id, loggedUser);

            new SecurityContextLogoutHandler().logout(request, response, authentication);

            return "redirect:/admins/users";
        }
        return "redirect:/";
    }
}
