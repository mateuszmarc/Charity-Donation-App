package pl.mateuszmarcyk.charity_donation_app.category;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;

import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admins/categories")
public class CategoryController {

    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping
    public String showAllCategories(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            List<Category> categories = categoryService.findAll();
            model.addAttribute("categories", categories);

            return "categories-all";

        }

        return "index";
    }

    @GetMapping("/{categoryId}")
    public String showCategoryDetails(@PathVariable Long categoryId, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            Category category = categoryService.findById(categoryId);
            model.addAttribute("category", category);

            return "category-details";
        }

        return "index";
    }

    @GetMapping("/add")
    public String showCategoryForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);
            model.addAttribute("category", new Category());
            return "category-form";
        }

        return "index";
    }

    @PostMapping("/add")
    public String processCategoryForm(@ModelAttribute Category category, BindingResult bindingResult, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            System.out.println(category.getDonations());
            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            if (bindingResult.hasErrors()) {
                return "category-form";
            }

            categoryService.save(category);

            return "redirect:/admins/categories";
        }
        return "index";
    }

    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();

            Category category = categoryService.findById(id);

            model.addAttribute("category", category);
            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);
            return "category-form";
        }

        return "index";
    }



    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();

            User user = userService.findUserByEmail(email);
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            categoryService.deleteById(id);

            return "redirect:/admins/categories";
        }
        return "index";
    }


}