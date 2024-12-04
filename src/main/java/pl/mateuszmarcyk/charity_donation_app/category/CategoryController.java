package pl.mateuszmarcyk.charity_donation_app.category;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
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
    public String showAllCategories(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails != null) {

            User user = userDetails.getUser();
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            List<Category> categories = categoryService.findAll();
            model.addAttribute("categories", categories);

            return "categories-all";

        }

        return "redirect:/";
    }

    @GetMapping("/{categoryId}")
    public String showCategoryDetails(@PathVariable Long categoryId, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {

            User user = userDetails.getUser();
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            Category category = categoryService.findById(categoryId);
            model.addAttribute("category", category);

            return "category-details";
        }

        return "redirect:/";
    }

    @GetMapping("/add")
    public String showCategoryForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {

            User user = userDetails.getUser();
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);
            model.addAttribute("category", new Category());
            return "category-form";
        }

        return "redirect:/";
    }

    @PostMapping("/add")
    public String processCategoryForm(@Valid @ModelAttribute(name = "category") Category category,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      Model model) {
        if (userDetails != null) {

            User user = userDetails.getUser();
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);

            if (bindingResult.hasErrors()) {
                return "category-form";
            }

            categoryService.save(category);

            return "redirect:/admins/categories";
        }
        return "redirect:/";
    }

    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {

        if (userDetails != null) {

            User user = userDetails.getUser();
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);


            Category category = categoryService.findById(id);
            model.addAttribute("category", category);

            return "category-form";
        }

        return "redirect:/";
    }



    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails != null) {
            User user = userDetails.getUser();
            UserProfile userProfile = user.getProfile();

            model.addAttribute("user", user);
            model.addAttribute("userProfile", userProfile);
            categoryService.deleteById(id);

            return "redirect:/admins/categories";
        }
        return "index";
    }


}