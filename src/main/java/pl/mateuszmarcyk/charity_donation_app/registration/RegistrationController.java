package pl.mateuszmarcyk.charity_donation_app.registration;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;

import java.util.Locale;

@RequiredArgsConstructor
@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserService userService;
    private final MessageSource messageSource;

    @ModelAttribute(name = "passwordRule")
    public String getPasswordRule() {
        return messageSource.getMessage("password.rule", null, Locale.getDefault());
    }

    @ModelAttribute(name = "registrationTitle")
    public String getTokenRegistrationTitle() {
        return messageSource.getMessage("token.resend.title", null, Locale.getDefault());
    }

    @ModelAttribute(name = "registrationCompleteTitle")
    public String getTokenRegistrationCompleteTitle() {
        return messageSource.getMessage("registration.confirmation.title", null, Locale.getDefault());
    }

    @ModelAttribute(name = "validationTitle")
    public String getTokenValidationTitle() {
        return messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault());
    }

    @ModelAttribute(name = "validationMessage")
    public String getTokenValidationMessage() {
        return messageSource.getMessage("token.validation.message", null, Locale.getDefault());
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }


    @GetMapping
    public String registerForm(Model model) {

        model.addAttribute("user", new User());

        return "register";
    }

    @PostMapping
    public String processForm(@Valid @ModelAttribute User user, BindingResult bindingResult, Model model, HttpServletRequest request) {

        String passwordRepeat = request.getParameter("passwordRepeat");
        System.out.println("Password repeat: " + passwordRepeat);
        System.out.println("User password: " + user.getPassword());
        System.out.println("User email: " + user.getEmail());


        String passwordEqualityError = registrationService.getPasswordErrorIfExists(user.getPassword(), passwordRepeat);

        if (bindingResult.hasErrors() || passwordEqualityError != null) {
            return "register";
        }

        registrationService.registerUser(user, request);


        model.addAttribute("registrationMessage", registrationService.getRegistrationCompleteMessage());

        return "register-confirmation";
    }

    @GetMapping("/verifyEmail")
    public String verifyUser(@RequestParam String token, Model model) {

        userService.validateToken(token);

        return "validation-complete";
    }

    @PostMapping("/resendToken")
    public String resendToken(HttpServletRequest request, Model model) {
        String oldToken = request.getParameter("token");
        System.out.println("Old token: " + oldToken);
        if (oldToken != null) {
            registrationService.resendToken(oldToken, request);
        }

        model.addAttribute("registrationMessage", registrationService.getRegistrationCompleteMessage());

        return "register-confirmation";
    }
}