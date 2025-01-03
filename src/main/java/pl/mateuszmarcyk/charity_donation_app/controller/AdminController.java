package pl.mateuszmarcyk.charity_donation_app.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.service.CategoryService;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
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
    private final DonationService donationService;
    private final CategoryService categoryService;
    private final InstitutionService institutionService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            return "admin-dashboard";
        }
        return "index";
    }


    @GetMapping("/all-admins")
    public String showAllAdmins(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            List<User> admins = userService.findAllAdmins(user);

            model.addAttribute("users", admins);

            return "admin-users-all";
        }
        return "redirect:/";
    }

    @GetMapping("/users")
    public String getAllUsers(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            List<User> allUsers = userService.findAllUsers(user);

            model.addAttribute("users", allUsers);

            return "admin-users-all";
        }

        return "redirect:/";
    }

    @GetMapping("/users/{id}")
    public String showUserById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
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

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            User searchedUser = userService.findUserById(id);
            model.addAttribute("profile", searchedUser.getProfile());

            return "admin-user-profile-details";
        }

        return "redirect:/";
    }

    @GetMapping("/users/profiles/edit/{id}")
    public String showUserProfileDetailsForm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
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

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            User profileOwner = userService.findUserByProfileId(profile.getId());

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

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
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

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
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

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
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

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            User userToBlock = userService.findUserById(id);

            userService.blockUser(userToBlock);
            return "redirect:/admins/users/%d".formatted(userToBlock.getId());

        }

        return "redirect:/";
    }

    @GetMapping("/users/unblock/{id}")
    public String unblockUser(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            User userToUnblock = userService.findUserById(id);

            userService.unblockUser(userToUnblock);
            return "redirect:/admins/users/%d".formatted(userToUnblock.getId());

        }

        return "redirect:/";
    }

    @GetMapping("/users/upgrade/{id}")
    public String addAdminRole(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            User userToUpgrade = userService.findUserById(id);

            userService.addAdminRole(userToUpgrade);
            return "redirect:/admins/users/%d".formatted(userToUpgrade.getId());
        }
        return "redirect:/";
    }

    @GetMapping("/users/downgrade/{id}")
    public String removeAdminRole(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            User userToDowngrade = userService.findUserById(id);

            userService.removeAdminRole(userToDowngrade);
            return "redirect:/admins/users/%d".formatted(userToDowngrade.getId());
        }
        return "redirect:/";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam(name = "id") Long id, Model model) {
        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            userService.deleteUser(id);

            return "redirect:/admins/users";
        }
        return "redirect:/";
    }

    @GetMapping("/donations")
    public String showAllDonations(@AuthenticationPrincipal CustomUserDetails userDetails, Model model, HttpServletRequest request) {
        if (userDetails != null) {

            String sortType = request.getParameter("sortType");
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            List<Donation> allDonations = donationService.findAll(sortType);
            model.addAttribute("donations", allDonations);

            return "admin-donations-all";
        }
        return "redirect:/";
    }

    @PostMapping("/donations/archive")
    public String archiveDonation(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request, Model model) {
        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            Long id = Long.parseLong(request.getParameter("donationId"));

            Donation donationToArchive = donationService.getDonationById(id);
            donationService.archiveDonation(donationToArchive);

            return "redirect:/admins/donations";
        }

        return "redirect:/";
    }

    @PostMapping("/donations/unarchive")
    public String unarchiveDonation(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request, Model model) {
        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            Long id = Long.parseLong(request.getParameter("donationId"));

            Donation donationToArchive = donationService.getDonationById(id);
            donationService.unArchiveDonation(donationToArchive);

            return "redirect:/admins/donations";
        }

        return "redirect:/";
    }

    @PostMapping("/donations/delete")
    public String deleteDonation(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam("id") Long id, Model model) {
        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            Donation donationToDelete = donationService.getDonationById(id);
            donationService.deleteDonation(donationToDelete);

            return "redirect:/admins/donations";
        }

        return "redirect:/";
    }

    @GetMapping("/donations/{id}")
    public String showDonationDetails(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            Donation donation = donationService.getDonationById(id);
            model.addAttribute("donation", donation);

            return "admin-donation-details";
        }
        return "redirect:/";
    }

    @GetMapping("/categories")
    public String showAllCategories(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            List<Category> categories = categoryService.findAll();
            model.addAttribute("categories", categories);

            return "admin-categories-all";

        }

        return "redirect:/";
    }

    @GetMapping("/categories/{categoryId}")
    public String showCategoryDetails(@PathVariable Long categoryId, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            Category category = categoryService.findById(categoryId);
            model.addAttribute("category", category);

            return "admin-category-details";
        }

        return "redirect:/";
    }

    @GetMapping("/categories/add")
    public String showCategoryForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            model.addAttribute("category", new Category());
            return "admin-category-form";
        }

        return "redirect:/";
    }

    @PostMapping("/categories/add")
    public String processCategoryForm(@Valid @ModelAttribute(name = "category") Category category,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      Model model) {
        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            if (bindingResult.hasErrors()) {
                return "admin-category-form";
            }

            categoryService.save(category);

            return "redirect:/admins/categories";
        }
        return "redirect:/";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategory(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {

        if (userDetails != null) {

            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            Category category = categoryService.findById(id);
            model.addAttribute("category", category);

            return "admin-category-form";
        }

        return "redirect:/";
    }

    @PostMapping("/categories/delete")
    public String deleteCategory(@RequestParam("id") Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            categoryService.deleteById(id);

            return "redirect:/admins/categories";
        }
        return "index";
    }

    @GetMapping("/institutions")
    public String showAllInstitutions(@AuthenticationPrincipal CustomUserDetails userDetails,  Model model) {

        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            List<Institution> institutions = institutionService.findAll();
            model.addAttribute("institutions", institutions);
            return "admin-institutions-all";
        }
        return "redirect:/index";
    }

    @GetMapping("/institutions/{id}")
    public String showInstitutionDetails(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            Institution institution = institutionService.findById(id);
            model.addAttribute("institution", institution);
            return "admin-institution-details";
        }

        return "redirect:/";
    }

    @GetMapping("/institutions/add")
    public String showInstitutionForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            model.addAttribute("institution", new Institution());

            return "admin-institution-form";
        }
        return "redirect:/";
    }

    @PostMapping("/institutions/add")
    public String processInstitutionForm(@Valid @ModelAttribute Institution institution,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model) {
        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            if (bindingResult.hasErrors()) {
                bindingResult.getAllErrors().forEach(System.out::println);
                return "admin-institution-form";
            }

            institutionService.saveInstitution(institution);
            return "redirect:/admins/institutions";
        }
        return "redirect:/";
    }

    @GetMapping("/institutions/edit/{id}")
    public String showEditInstitutionForm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            Institution institution = institutionService.findById(id);
            model.addAttribute("institution", institution);

            return "admin-institution-form";
        }
        return "redirect:/";
    }

    @PostMapping("/institutions/delete")
    public String deleteInstitutionById(@RequestParam("id") Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails != null) {
            User user = LoggedUserModelHandler.getUser(userDetails);
            LoggedUserModelHandler.addUserToModel(user, model);
            institutionService.deleteById(id);

            return "redirect:/admins/institutions";

        }
        return "redirect:/";
    }
}
