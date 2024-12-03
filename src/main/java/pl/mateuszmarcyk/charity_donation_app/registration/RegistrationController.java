package pl.mateuszmarcyk.charity_donation_app.registration;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;

@RequiredArgsConstructor
@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final RegistrationService registrationService;
    private final UserService userService;

    @Value("${password.rule}")
    private String passwordRule;

    @Value("${error.tokennotfound.title}")
    private String tokenVerificationTitle;

    @Value("${token.validation.message}")
    private String tokenValidationMessage;

    @Value("${registration.confirmation.title}")
    private String registrationCompleteTitle;

    @Value("${token.resend.title}")
    private String tokenResendTitle;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }


    @GetMapping
    public String registerForm(Model model) {

        model.addAttribute("user", new User());
        model.addAttribute("passwordRule", passwordRule);

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
            bindingResult.getAllErrors().forEach(System.out::println);
            model.addAttribute("passwordError", passwordEqualityError);
            model.addAttribute("passwordRule", passwordRule);
            return "register";
        }

        registrationService.registerUser(user, request);

        model.addAttribute("registrationTitle", registrationCompleteTitle);
        model.addAttribute("registrationMessage", registrationService.getRegistrationCompleteMessage());

        return "register-confirmation";
    }

    @GetMapping("/verifyEmail")
    public String verifyUser(@RequestParam String token, Model model) {

        userService.validateToken(token);

        model.addAttribute("validationTitle", tokenVerificationTitle);
        model.addAttribute("validationMessage", tokenValidationMessage);

        return "validation-complete";
    }

    @PostMapping("/resendToken")
    public String resendToken(HttpServletRequest request, Model model) {
        String oldToken = request.getParameter("token");
        System.out.println("Old token: " + oldToken);
        if (oldToken != null) {
            registrationService.resendToken(oldToken, request);
        }

        model.addAttribute("registrationTitle", tokenResendTitle);
        model.addAttribute("registrationMessage", registrationService.getRegistrationCompleteMessage());

        return "register-confirmation";
    }
}