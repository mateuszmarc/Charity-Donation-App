package pl.mateuszmarcyk.charity_donation_app.registration;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.mateuszmarcyk.charity_donation_app.user.User;

@RequiredArgsConstructor
@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final RegistrationService registrationService;


    @GetMapping
    public String registerForm(Model model) {

        model.addAttribute("user", new User());
        model.addAttribute("passwordError", null);

        return "register";
    }

    @PostMapping
    public String processForm(@Valid @ModelAttribute User user, BindingResult bindingResult, Model model, HttpServletRequest request) {

        String passwordRepeat = request.getParameter("passwordRepeat");
        String passwordEqualityError = registrationService.getPasswordErrorIfExists(user.getPassword(), passwordRepeat);

        if (bindingResult.hasErrors() || passwordRepeat != null) {
            model.addAttribute("passwordError", passwordEqualityError);
            return "register";
        }

        return "registration-confirmation";
    }
}