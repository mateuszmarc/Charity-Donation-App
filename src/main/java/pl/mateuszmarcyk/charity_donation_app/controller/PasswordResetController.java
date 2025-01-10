package pl.mateuszmarcyk.charity_donation_app.controller;

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
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.Email;

import java.util.Locale;

@RequiredArgsConstructor
@Controller
public class PasswordResetController {

    private final MessageSource messageSource;

    private final UserService userService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/reset-password")
    public String showResetPasswordEmailForm(Model model) {

        model.addAttribute("email", new Email());
        return "password-reset-form";
    }

    @PostMapping("/reset-password")
    public String processResetPasswordForm(@Valid @ModelAttribute(name = "email") Email email, BindingResult bindingResult, Model model, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "password-reset-form";
        }

        userService.resetPassword(email, request);


        String passwordResendTitle =  messageSource.getMessage("passwordresend.title", null, Locale.getDefault());
        String passwordResendMessage = messageSource.getMessage("passwordresend.message", null, Locale.getDefault());
        String tokenValidTime = messageSource.getMessage("token.valid.time", null, Locale.getDefault());

        model.addAttribute("registrationCompleteTitle", passwordResendTitle);
        model.addAttribute("registrationMessage", passwordResendMessage + " " + tokenValidTime + " minut");

        return "register-confirmation";
    }

    @GetMapping("/reset-password/verifyEmail")
    public String showChangePasswordForm(@RequestParam(name = "token") String token, Model model) {

        User user = userService.validatePasswordResetToken(token);

        model.addAttribute("user", user);

        return "new-password-form";
    }

    @PostMapping("/new-password")
    public String processChangePasswordForm(@Valid @ModelAttribute User user, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(System.out::println);
            return "new-password-form";
        }

        userService.changePassword(user);
        return "redirect:/login";
    }

    @ModelAttribute(name = "passwordRule")
    public String getPasswordRule() {
        return messageSource.getMessage("password.rule", null, Locale.getDefault());
    }
}
