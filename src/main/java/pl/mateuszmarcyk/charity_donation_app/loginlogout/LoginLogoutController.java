package pl.mateuszmarcyk.charity_donation_app.loginlogout;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.Email;

@Controller
public class LoginLogoutController {

    private final UserService userService;
    @Value("${passwordresend.title}")
    private String passwordResendTitle;

    @Value("${passwordresend.message}")
    private String passwordResendMessage;

    @Value("${token.valid.time}")
    private String tokenValidTime;

    public LoginLogoutController(UserService userService) {
        this.userService = userService;
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/login")
    private String showLoginForm() {
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        return "redirect:/";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(Model model) {

        model.addAttribute("email", new Email());
        return "password-reset-form";
    }

    @PostMapping("/reset-password")
    public String processResetPasswordForm(@Valid @ModelAttribute(name = "email") Email email, BindingResult bindingResult, Model model, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "password-reset-form";
        }

        userService.resetPassword(email, request);



        model.addAttribute("registrationTitle", passwordResendTitle);
        model.addAttribute("registrationMessage", passwordResendMessage + tokenValidTime + " minut");

        return "register-confirmation";
    }
}
