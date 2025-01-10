package pl.mateuszmarcyk.charity_donation_app.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.service.RegistrationService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.MessageDTO;

import java.util.Locale;

@Slf4j
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

    @ModelAttribute(name = "message")
    public MessageDTO getMessage() {
        return new MessageDTO();
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }


    @GetMapping
    public String showRegisterForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        if (userDetails != null) {
            return "redirect:/";
        }

        model.addAttribute("user", new User());
        return "register-form";
    }

    @PostMapping
    public String processRegistrationForm(@Valid @ModelAttribute User user, BindingResult bindingResult, Model model, HttpServletRequest request) {

        String passwordRepeat = request.getParameter("passwordRepeat");
        log.info("Password repeat: {}", passwordRepeat);
        log.info("User password: {}", user.getPassword());
        log.info("User email: {}", user.getEmail());

        if (bindingResult.hasErrors()) {
            return "register-form";
        }

        registrationService.registerUser(user, request);


        model.addAttribute("registrationMessage", registrationService.getRegistrationCompleteMessage());

        return "register-confirmation";
    }

    @GetMapping("/verifyEmail")
    public String verifyUserByRegistrationToken(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String token) {

        if (userDetails != null) {
            return "redirect:/";
        }

        userService.validateToken(token);

        return "validation-complete";
    }

    @PostMapping("/resendToken")
    public String resendToken(HttpServletRequest request, Model model) {
        String oldToken = request.getParameter("token");
        log.info("Old token: {}", oldToken);
        if (oldToken != null) {
            registrationService.resendToken(oldToken, request);
        }

        model.addAttribute("registrationMessage", registrationService.getRegistrationCompleteMessage());

        return "register-confirmation";
    }
}