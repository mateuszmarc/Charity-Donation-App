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
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.util.FileUploadUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/admins")
public class AdminController {

    private final UserService userService;

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

            System.out.println(user.getUserTypes());
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
            System.out.println(admin.getProfile());
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

            User profileOwner = userService.findUserById(id);
            UserProfile profile = profileOwner.getProfile();

            model.addAttribute("profile", profile);

            return "user-profile-form";
        }
        return "redirect:/";
    }

    @PostMapping("/all-admins/profiles/edit")
    public String processProfileEditForm(@Valid @ModelAttribute(name = "profile") UserProfile profile, BindingResult bindingResult, Model model,
                                         @RequestParam("image") MultipartFile image) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            User profileOwner = userService.findUserByProfileId(profile.getId());
            profileOwner.setProfile(profile);

            if (bindingResult.hasErrors()) {
                bindingResult.getAllErrors().forEach(System.out::println);
                return "user-profile-form";
            }


            String imageName = "";
            if (!Objects.equals(image.getOriginalFilename(), "")) {
                imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
                profile.setProfilePhoto(imageName);
            }

            userService.updateUser(profileOwner);

            String imageUploadDir = "photos/users/" + profileOwner.getId();

            try {
                if (!Objects.equals(image.getOriginalFilename(), "")) {
                    FileUploadUtil.saveFile(imageUploadDir, imageName, image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            return "redirect:/admins/all-admins/%d".formatted(profileOwner.getId());

        }
        return "redirect:/";
    }

    @GetMapping("/all-admins/downgrade/{id}")
    public String removeAdminAuthority(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            User userToRemoveAuthorityFrom = userService.findUserById(id);

            userService.removeAuthority(userToRemoveAuthorityFrom, "ROLE_ADMIN");

            return "redirect:/admins/all-admins";
        }

        return "redirect:/";
    }

    @GetMapping("/all-admins/delete/{id}")
    public String removeAdminUser(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            User userToDelete = userService.findUserById(id);
            userService.deleteAdmin(userToDelete, loggedUser);
            return "redirect:/all-admins";
        }

        return "redirect:/";
    }


    @GetMapping("/users")
    public String getAllUsers(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            List<User> allUsers = userService.findAllUsers(loggedUser);

            model.addAttribute("users", allUsers);

            return "users-all";
        }

        return "redirect:/";
    }

    @GetMapping("/users/{id}")
    public String showUserById(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            User searchedUser = userService.findUserById(id);
            System.out.println(searchedUser.getUserTypes());
            model.addAttribute("searchedUser", searchedUser);

            return "user-account-details";
        }
        return "redirect:/";
    }

    @GetMapping("/users/profiles/{id}")
    public String showUserProfileDetails(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            User searchedUser = userService.findUserById(id);
            model.addAttribute("profile", searchedUser.getProfile());

            return "user-profile-details";
        }

        return "redirect:/";
    }

    @GetMapping("/users/profiles/edit/{id}")
    private String showUserProfileDetailsForm(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            User searchedUser = userService.findUserById(id);
            model.addAttribute("profile", searchedUser.getProfile());

            return "user-profile-details-form";
        }

        return "redirect:/";
    }

    @PostMapping("/users/profiles/edit")
    public String processUserProfileDetailsForm(@Valid @ModelAttribute(name = "profile") UserProfile profile, BindingResult bindingResult, Model model,
                                                @RequestParam("image") MultipartFile image) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            User profileOwner = userService.findUserByProfileId(profile.getId());
            profileOwner.setProfile(profile);

            if (bindingResult.hasErrors()) {
                bindingResult.getAllErrors().forEach(System.out::println);
                return "user-profile-details-form";
            }


            String imageName = "";
            if (!Objects.equals(image.getOriginalFilename(), "")) {
                imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
                profile.setProfilePhoto(imageName);
            }

            userService.updateUser(profileOwner);

            String imageUploadDir = "photos/users/" + profileOwner.getId();

            try {
                if (!Objects.equals(image.getOriginalFilename(), "")) {
                    FileUploadUtil.saveFile(imageUploadDir, imageName, image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            return "redirect:/admins/users/profiles/%d".formatted(profileOwner.getId());

        }
        return "redirect:/";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            User userToEdit = userService.findUserById(id);
            userToEdit.setPasswordRepeat(userToEdit.getPassword());
            model.addAttribute("userToEdit", userToEdit);

            return "user-account-edit-form";
        }

        return "redirect:/";
    }

    @PostMapping("/users/change-email")
    public String processChangeEmailForm(@Valid @ModelAttribute(name = "userToEdit") User userToEdit, BindingResult bindingResult, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            if (bindingResult.hasErrors()) {
                return "user-account-edit-form";
            }

            userService.updateUserEmail(userToEdit);
            return "redirect:/admins/users/%d".formatted(userToEdit.getId());
        }
        return "redirect:/";
    }

    @PostMapping("/users/change-password")
    public String processUserChangePasswordForm(@Valid @ModelAttribute(name = "userToEdit") User userToEdit, BindingResult bindingResult, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User loggedUser = userService.findUserByEmail(email);
            UserProfile userProfile = loggedUser.getProfile();
            model.addAttribute("user", loggedUser);
            model.addAttribute("userProfile", userProfile);

            if (bindingResult.hasErrors()) {
                return "user-account-edit-form";
            }
            userService.changePassword(userToEdit);
            return "redirect:/admins/users/%d".formatted(userToEdit.getId());
        }
        return "redirect:/";
    }
}