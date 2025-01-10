package pl.mateuszmarcyk.charity_donation_app.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.util.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Controller
public class HomeController {

    private final DonationService donationService;
    private final InstitutionService institutionService;
    private final AppMailSender appMailSender;
    private final MailMessage mailMessageHelper;
    private final LoggedUserModelHandler loggedUserModelHandler;
    private final MessageSource messageSource;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }


    @GetMapping("/")
    public String index(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        MessageDTO messageDTO = new MessageDTO();

        if (userDetails != null) {
            if (userDetails.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_USER"))) {
                return "redirect:/admins/dashboard";
            }
            User loggedUser = loggedUserModelHandler.getUser(userDetails);
            loggedUserModelHandler.addUserToModel(loggedUser, model);

            messageDTO.setEmail(loggedUser.getEmail());
        }

        model.addAttribute("message", messageDTO);

        return "index";
    }

    @PostMapping("/message")
    public String processMessageForm(@AuthenticationPrincipal CustomUserDetails userDetails, @Valid @ModelAttribute(name = "message") MessageDTO message, BindingResult bindingResult, Model model) {

        String messageSuccessInfo = messageSource.getMessage("mail.message.success.info", null, Locale.getDefault());
        if (userDetails != null) {
            User user = loggedUserModelHandler.getUser(userDetails);
            loggedUserModelHandler.addUserToModel(user, model);
        }

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(error -> log.info("{}", error));
            return "index";
        }

        String firstName = message.getFirstName();
        String lastName = message.getLastName();
        String messageContent = message.getMessage();
        String email = message.getEmail();

        String mailMessage = mailMessageHelper.getMailMessage(firstName, lastName, messageContent, email);
            Mail mail = new Mail("Nowa wiadomość", firstName + " " + lastName, mailMessage);

            try {
                appMailSender.sendMailMessage(mail);
            } catch (MessagingException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

        model.addAttribute("messageSuccess", messageSuccessInfo);

        return "index";
    }

    @ModelAttribute(name = "institutions")
    public List<Institution> getInstitutions() {
        return institutionService.findAll();
    }

    @ModelAttribute(name = "allDonations")
    public Integer getDonationCount() {
        return donationService.countAllDonations();
    }

    @ModelAttribute(name = "allDonationBags")
    public Integer getDonationBagCount() {
        return donationService.countAllBags();
    }

}
