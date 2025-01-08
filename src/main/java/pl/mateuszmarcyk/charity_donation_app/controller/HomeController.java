package pl.mateuszmarcyk.charity_donation_app.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;
import pl.mateuszmarcyk.charity_donation_app.util.MailMessage;

import java.io.UnsupportedEncodingException;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final UserService userService;
    private final DonationService donationService;
    private final InstitutionService institutionService;
    private final AppMailSender appMailSender;
    private final MailMessage mailMessageHelper;
    private final LoggedUserModelHandler loggedUserModelHandler;


    @GetMapping("/")
    public String index(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {

        if (userDetails != null) {
            if (userDetails.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_USER"))) {
                return "redirect:/admins/dashboard";
            }
            User loggedUser = loggedUserModelHandler.getUser(userDetails);
            loggedUserModelHandler.addUserToModel(loggedUser, model);
        }

        List<Institution> institutions = institutionService.findAll();
        int allDonations = donationService.countAllDonations();
        int allDonationBags = donationService.countAllBags();

        model.addAttribute("institutions", institutions);
        model.addAttribute("allDonations", allDonations);
        model.addAttribute("allDonationBags", allDonationBags);

        return "index";
    }

    @PostMapping("/message")
    public String processMessageForm(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletRequest request, Model model) {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String message = request.getParameter("message");
        String messageEmail = request.getParameter("email");
        User user = null;

        String infoMessage = "Wiadomość nie została wysłana. Wszystkie pola muszą być wypełnione. Kliknij, a zostaniesz przekierowany do formularza";

        if (userDetails != null) {
            user = loggedUserModelHandler.getUser(userDetails);
            loggedUserModelHandler.addUserToModel(user, model);
            messageEmail = user.getEmail();

        }

        if (validateMailMessage(firstName, lastName, message, messageEmail)) {
            String mailMessage = mailMessageHelper.getMailMessage(firstName, lastName, message, user);
            Mail mail = new Mail("Nowa wiadomość", firstName + " " + lastName, mailMessage);

            try {
                appMailSender.sendMailMessage(mail);
                infoMessage = "Wiadomość wysłana pomyślnie. Odpowiemy tak szybko jak tylko możemy";
                model.addAttribute("messageSuccess", infoMessage);
            } catch (MessagingException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            model.addAttribute("messageError", infoMessage);
        }
        return "index";
    }

    private boolean validateMailMessage(String firstName, String lastName, String message, String messageEmail) {

        return firstName != null && !firstName.trim().isEmpty() &&
                lastName != null && !lastName.trim().isEmpty() &&
                message != null && !message.trim().isEmpty() &&
                messageEmail != null && !messageEmail.trim().isEmpty();
    }

}
