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

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/admins")
public class AdminController {

    private static final String ADMIN_CATEGORY_FORM_VIEW = "admin-category-form";
    private static final String ADMIN_INSTITUTION_FORM_VIEW = "admin-institution-form";
    private static final String REDIRECT_TO_USER_ACCOUNT_DETAILS_URL = "redirect:/admins/users/%d";
    private static final String REDIRECT_TO_ALL_DONATIONS_URL = "redirect:/admins/donations";
    private static final String CATEGORY_MODEL_ATTRIBUTE_KEY = "category";
    private static final String INSTITUTION_MODEL_ATTRIBUTE_KEY = "institution";

    private final UserService userService;
    private final FileUploadUtil fileUploadUtil;
    private final DonationService donationService;
    private final CategoryService categoryService;
    private final InstitutionService institutionService;
    private final LoggedUserModelHandler loggedUserModelHandler;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        return "admin-dashboard";
    }

    @GetMapping("/all-admins")
    public String showAllAdmins(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        List<User> admins = userService.findAllAdmins(user);

        model.addAttribute("users", admins);
        model.addAttribute("title", "Lista administratorów");

        return "admin-users-all";
    }


    @GetMapping("/users")
    public String showAllUsers(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        List<User> allUsers = userService.findAllUsers(user);

        model.addAttribute("users", allUsers);

        return "admin-users-all";
        }


    @GetMapping("/users/{id}")
    public String showUserById(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        User searchedUser = userService.findUserById(id);
        if (searchedUser.getUserTypes().stream().anyMatch(role -> role.getRole().equals("ROLE_ADMIN"))) {
            model.addAttribute("admin", true);
        }
        model.addAttribute("searchedUser", searchedUser);

        return "admin-user-account-details";
    }

    @GetMapping("/users/profiles/{id}")
    public String showUserProfileDetailsByUserId(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        User searchedUser = userService.findUserById(id);
        model.addAttribute("profile", searchedUser.getProfile());

        return "admin-user-profile-details";
    }


    @GetMapping("/users/profiles/edit/{id}")
    public String showUserProfileDetailsEditForm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        User searchedUser = userService.findUserById(id);
        model.addAttribute("profile", searchedUser.getProfile());

        return "admin-user-profile-details-form";
    }


    @PostMapping("/users/profiles/edit")
    public String processUserProfileDetailsEditForm(@Valid @ModelAttribute(name = "profile") UserProfile profile,
                                                @RequestParam("image") MultipartFile image) throws IOException {

        User profileOwner = userService.findUserByProfileId(profile.getId());

        fileUploadUtil.saveImage(profile, image, profileOwner);

        return "redirect:/admins/users/profiles/%d".formatted(profileOwner.getId());
    }

    @GetMapping("/users/change-email/{id}")
    public String showChangeEmailForm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        User userToEdit = userService.findUserById(id);

        userToEdit.setPasswordRepeat(userToEdit.getPassword());
        model.addAttribute("userToEdit", userToEdit);

        return "admin-user-email-edit-form";
    }

    @GetMapping("/users/change-password/{id}")
    public String showChangePasswordForm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {
        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        User userToEdit = userService.findUserById(id);

        userToEdit.setPasswordRepeat(userToEdit.getPassword());
        model.addAttribute("userToEdit", userToEdit);

        return "admin-user-password-edit-form";
    }


    @PostMapping("/users/change-email")
    public String processChangeEmailForm(@Valid @ModelAttribute(name = "userToEdit") User userToEdit,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> log.info("{}", error));
            return "admin-user-email-edit-form";
        }

        userService.updateUserEmail(userToEdit);
        return REDIRECT_TO_USER_ACCOUNT_DETAILS_URL.formatted(userToEdit.getId());
    }

    @PostMapping("/users/change-password")
    public String processUserChangePasswordForm(@Valid @ModelAttribute(name = "userToEdit") User userToEdit,
                                                BindingResult bindingResult,
                                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                                Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        if (bindingResult.hasErrors()) {
            return "admin-user-password-edit-form";
        }
        userService.changePassword(userToEdit);
        return REDIRECT_TO_USER_ACCOUNT_DETAILS_URL.formatted(userToEdit.getId());
    }

    @GetMapping("/users/block/{id}")
    public String blockUser(@PathVariable Long id) {

        userService.blockUserById(id);
        return REDIRECT_TO_USER_ACCOUNT_DETAILS_URL.formatted(id);
    }


    @GetMapping("/users/unblock/{id}")
    public String unblockUser(@PathVariable Long id) {

        userService.unblockUser(id);
        return REDIRECT_TO_USER_ACCOUNT_DETAILS_URL.formatted(id);
    }

    @GetMapping("/users/upgrade/{id}")
    public String addAdminRole(@PathVariable Long id) {

        userService.addAdminRole(id);
        return REDIRECT_TO_USER_ACCOUNT_DETAILS_URL.formatted(id);
    }

    @GetMapping("/users/downgrade/{id}")
    public String removeAdminRole(@PathVariable Long id) {

        userService.removeAdminRole(id);
        return REDIRECT_TO_USER_ACCOUNT_DETAILS_URL.formatted(id);
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam(name = "id") Long id) {

        User userToDelete = userService.findUserById(id);
        userService.deleteUser(id);

        if (userToDelete.getUserTypes().stream().anyMatch(role -> role.getRole().equals("ROLE_ADMIN"))) {
            return "redirect:/admins/all-admins";
        }
        return "redirect:/admins/users";
    }

    @GetMapping("/donations")
    public String showAllDonations(@AuthenticationPrincipal CustomUserDetails userDetails, Model model, HttpServletRequest request) {

        String sortType = request.getParameter("sortType");
        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        List<Donation> allDonations = donationService.findAll(sortType);
        model.addAttribute("donations", allDonations);

        return "admin-donations-all";
    }

    @PostMapping("/donations/archive")
    public String archiveDonation(@RequestParam("donationId") Long id) {

        Donation donationToArchive = donationService.findDonationById(id);
        donationService.archiveDonation(donationToArchive);

        return REDIRECT_TO_ALL_DONATIONS_URL;
    }


    @PostMapping("/donations/unarchive")
    public String unArchiveDonation(@RequestParam("donationId") Long id) {

        Donation donationToArchive = donationService.findDonationById(id);
        donationService.unArchiveDonation(donationToArchive);

        return REDIRECT_TO_ALL_DONATIONS_URL;
    }


    @PostMapping("/donations/delete")
    public String deleteDonation(@RequestParam("id") Long id) {

        Donation donationToDelete = donationService.findDonationById(id);
        donationService.deleteDonation(donationToDelete);

        return REDIRECT_TO_ALL_DONATIONS_URL;
    }


    @GetMapping("/donations/{id}")
    public String showDonationDetails(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        Donation donation = donationService.findDonationById(id);
        model.addAttribute("donation", donation);

        return "admin-donation-details";
    }

    @GetMapping("/categories")
    public String showAllCategories(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);

        return "admin-categories-all";

    }


    @GetMapping("/categories/{categoryId}")
    public String showCategoryDetails(@PathVariable Long categoryId, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        Category category = categoryService.findCategoryById(categoryId);
        model.addAttribute(CATEGORY_MODEL_ATTRIBUTE_KEY, category);

        return "admin-category-details";
    }


    @GetMapping("/categories/add")
    public String showCategoryForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        model.addAttribute(CATEGORY_MODEL_ATTRIBUTE_KEY, new Category());
        return ADMIN_CATEGORY_FORM_VIEW;
    }


    @PostMapping("/categories/add")
    public String processCategoryForm(@Valid @ModelAttribute(name = CATEGORY_MODEL_ATTRIBUTE_KEY) Category category,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        if (bindingResult.hasErrors()) {
            return ADMIN_CATEGORY_FORM_VIEW;
        }

        categoryService.save(category);

        return "redirect:/admins/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String showCategoryEditForm(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {


        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        Category category = categoryService.findCategoryById(id);
        model.addAttribute(CATEGORY_MODEL_ATTRIBUTE_KEY, category);

        return ADMIN_CATEGORY_FORM_VIEW;
    }


    @PostMapping("/categories/delete")
    public String deleteCategory(@RequestParam("id") Long id) {

        categoryService.deleteById(id);

        return "redirect:/admins/categories";
    }


    @GetMapping("/institutions")
    public String showAllInstitutions(@AuthenticationPrincipal CustomUserDetails userDetails,  Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        List<Institution> institutions = institutionService.findAll();
        model.addAttribute("institutions", institutions);
        return "admin-institutions-all";
    }


    @GetMapping("/institutions/{id}")
    public String showInstitutionDetails(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        Institution institution = institutionService.findInstitutionById(id);
        model.addAttribute(INSTITUTION_MODEL_ATTRIBUTE_KEY, institution);
        return "admin-institution-details";
    }


    @GetMapping("/institutions/add")
    public String showInstitutionForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        model.addAttribute(INSTITUTION_MODEL_ATTRIBUTE_KEY, new Institution());

        return ADMIN_INSTITUTION_FORM_VIEW;
    }

    @PostMapping("/institutions/add")
    public String processInstitutionForm(@Valid @ModelAttribute Institution institution,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal CustomUserDetails userDetails,
                                         Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> log.info("{}", error));
            return ADMIN_INSTITUTION_FORM_VIEW;
        }

        institutionService.saveInstitution(institution);
        return "redirect:/admins/institutions";
    }

    @GetMapping("/institutions/edit/{id}")
    public String showInstitutionEditForm(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, Model model) {

        User user = loggedUserModelHandler.getUser(userDetails);
        loggedUserModelHandler.addUserToModel(user, model);
        Institution institution = institutionService.findInstitutionById(id);
        model.addAttribute(INSTITUTION_MODEL_ATTRIBUTE_KEY, institution);

        return ADMIN_INSTITUTION_FORM_VIEW;
    }

    @PostMapping("/institutions/delete")
    public String deleteInstitutionById(@RequestParam("id") Long id) {

        institutionService.deleteIntitutionById(id);

        return "redirect:/admins/institutions";
    }
}
