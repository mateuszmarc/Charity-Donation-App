package pl.mateuszmarcyk.charity_donation_app.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.util.FileUploadUtil;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/admins")
public class AdminController {

    private final UserService userService;
    private final FileUploadUtil fileUploadUtil;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            return "admin-dashboard";
        }
        return "index";
    }


    @GetMapping("/all-admins")
    public String showAllAdmins(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails, model);

            List<User> admins = userService.findAllAdmins(user);

            model.addAttribute("users", admins);

            return "admin-users-all";
        }
        return "redirect:/";
    }

    @GetMapping("/users")
    public String getAllUsers(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails, model);

            List<User> allUsers = userService.findAllUsers(user);

            model.addAttribute("users", allUsers);

            return "admin-users-all";
        }

        return "redirect:/";
    }

    @GetMapping("/users/{id}")
    public String showUserById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            User searchedUser = userService.findUserById(id);
            System.out.println(searchedUser.getUserTypes());
            model.addAttribute("searchedUser", searchedUser);

            return "admin-user-account-details";
        }
        return "redirect:/";
    }

    @GetMapping("/users/profiles/{id}")
    public String showUserProfileDetails(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            User searchedUser = userService.findUserById(id);
            model.addAttribute("profile", searchedUser.getProfile());

            return "admin-user-profile-details";
        }

        return "redirect:/";
    }

    @GetMapping("/users/profiles/edit/{id}")
    public String showUserProfileDetailsForm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            User searchedUser = userService.findUserById(id);
            model.addAttribute("profile", searchedUser.getProfile());

            return "admin-user-profile-details-form";
        }

        return "redirect:/";
    }

    @PostMapping("/users/profiles/edit")
    public String processUserProfileDetailsForm(@Valid @ModelAttribute(name = "profile") UserProfile profile,
                                                BindingResult bindingResult,
                                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                                Model model,
                                                @RequestParam("image") MultipartFile image) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            User profileOwner = userService.findUserByProfileId(profile.getId());
            profileOwner.setProfile(profile);

            if (bindingResult.hasErrors()) {
                bindingResult.getAllErrors().forEach(System.out::println);
                return "admin-user-profile-details-form";
            }


            fileUploadUtil.saveImage(profile, image, profileOwner);


            return "redirect:/admins/users/profiles/%d".formatted(profileOwner.getId());

        }
        return "redirect:/";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            User userToEdit = userService.findUserById(id);
            userToEdit.setPasswordRepeat(userToEdit.getPassword());
            model.addAttribute("userToEdit", userToEdit);

            return "admin-user-account-edit-form";
        }

        return "redirect:/";
    }

    @PostMapping("/users/change-email")
    public String processChangeEmailForm(@Valid @ModelAttribute(name = "userToEdit") User userToEdit,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            if (bindingResult.hasErrors()) {
                return "admin-user-account-edit-form";
            }

            userService.updateUserEmail(userToEdit);
            return "redirect:/admins/users/%d".formatted(userToEdit.getId());
        }
        return "redirect:/";
    }

    @PostMapping("/users/change-password")
    public String processUserChangePasswordForm(@Valid @ModelAttribute(name = "userToEdit") User userToEdit,
                                                BindingResult bindingResult,
                                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                                Model model) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            if (bindingResult.hasErrors()) {
                return "admin-user-account-edit-form";
            }
            userService.changePassword(userToEdit);
            return "redirect:/admins/users/%d".formatted(userToEdit.getId());
        }
        return "redirect:/";
    }

    @GetMapping("/users/block/{id}")
    public String blockUser(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            User userToBlock = userService.findUserById(id);

            userService.blockUser(userToBlock);
            return "redirect:/admins/users/%d".formatted(userToBlock.getId());

        }

        return "redirect:/";
    }

    @GetMapping("/users/unblock/{id}")
    public String unblockUser(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);
            User userToUnblock = userService.findUserById(id);

            userService.unblockUser(userToUnblock);
            return "redirect:/admins/users/%d".formatted(userToUnblock.getId());

        }

        return "redirect:/";
    }

    @GetMapping("/users/upgrade/{id}")
    public String addAdminRole(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            LoggedUserModelHandler.getUser(userDetails, model);

            User userToUpgrade = userService.findUserById(id);

            userService.addAdminRole(userToUpgrade);
            return "redirect:/admins/users/%d".formatted(userToUpgrade.getId());
        }
        return "redirect:/";
    }

    @GetMapping("/users/downgrade/{id}")
    public String removeAdminRole(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {
           User loggedUser = LoggedUserModelHandler.getUser(userDetails, model);

            User userToDowngrade = userService.findUserById(id);

            userService.removeAdminRole(userToDowngrade, loggedUser);
            return "redirect:/admins/users/%d".formatted(userToDowngrade.getId());
        }
        return "redirect:/";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(name = "id") Long id, Model model) {
        if (userDetails != null) {

            User loggedUser = LoggedUserModelHandler.getUser(userDetails, model);

            userService.deleteUser(id, loggedUser);

            return "redirect:/admins/users";
        }
        return "redirect:/";
    }
}