package pl.mateuszmarcyk.charity_donation_app.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.mateuszmarcyk.charity_donation_app.donation.Donation;
import pl.mateuszmarcyk.charity_donation_app.donation.DonationService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.util.FileUploadUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;
    private final DonationService donationService;


    @GetMapping("/profile")
    public String showUserDetails(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);


            return "user-details-info";
        }
        return "redirect:/";
    }

    @GetMapping("/profile/edit")
    public String displayProfileEditForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            return "user-profile-edit-form";
        }
        return "redirect:/";
    }

    @PostMapping("/profile/edit")
    public String processProfileEditForm(@Valid @ModelAttribute(name = "userProfile") UserProfile profileToEdit, BindingResult bindingResult, Model model,
                                         @RequestParam("image") MultipartFile image) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            if (bindingResult.hasErrors()) {
                return "user-profile-edit-form";
            }

            loggedUser.setProfile(profileToEdit);
            String imageName = "";
            if (!Objects.equals(image.getOriginalFilename(), "")) {
                imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
                profileToEdit.setProfilePhoto(imageName);
            }

            userService.updateUser(loggedUser);

            String imageUploadDir = "photos/users/" + loggedUser.getId();

            try {
                if (!Objects.equals(image.getOriginalFilename(), "")) {
                    FileUploadUtil.saveFile(imageUploadDir, imageName, image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "user-profile-edit-form";
        }
        return "redirect:/";
    }

    @GetMapping("/account/edit")
    public String showUserEditAccountForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            loggedUser.setPasswordRepeat(loggedUser.getPassword());
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);
            model.addAttribute("userToEdit", loggedUser);

            return "user-account-edit";
        }

        return "redirect:/";
    }

    @PostMapping("/account/change-password")
    public String processUserChangePasswordForm(@Valid @ModelAttribute(name = "userToEdit") User userToEdit, BindingResult bindingResult,
                                             Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

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
    public String processUserChangeEmail(@Valid @ModelAttribute(name = "userToEdit") User userToEdit, BindingResult bindingResult, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

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
    public String showAllDonations(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            String sortType = request.getParameter("sortType");


            List<Donation> donations = donationService.getDonationsForUserSortedBy(sortType, loggedUser);
            model.addAttribute("donations", donations);

            return "user-donations";

        }
        return "redirect:/";
    }

    @GetMapping("/donations/{id}")
    public String showDonationDetails(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            Donation donation = donationService.getDonationById(id);
            model.addAttribute("donation", donation);

            return "donation-details";
        }
        return "redirect:/";
    }

    @PostMapping("/donations/archive")
    public String archiveDonation(HttpServletRequest request, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            Long id = Long.parseLong(request.getParameter("donationId"));

            Donation donationToArchive = donationService.getDonationById(id);
            donationService.archiveDonation(donationToArchive);

            return "redirect:/donations";
        }

        return "redirect:/";
    }
}
