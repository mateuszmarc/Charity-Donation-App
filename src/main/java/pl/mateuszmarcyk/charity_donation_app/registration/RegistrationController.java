package pl.mateuszmarcyk.charity_donation_app.registration;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pl.mateuszmarcyk.charity_donation_app.user.User;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    @GetMapping
    public String registerForm(Model model) {

        model.addAttribute("user", new User());
        model.addAttribute("passwordError", null);

        return "register";
    }
}
