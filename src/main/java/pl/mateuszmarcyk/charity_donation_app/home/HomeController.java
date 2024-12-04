package pl.mateuszmarcyk.charity_donation_app.home;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import pl.mateuszmarcyk.charity_donation_app.donation.DonationService;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.institution.Institution;
import pl.mateuszmarcyk.charity_donation_app.institution.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
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


    @GetMapping("/")
    public String index(Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            User user = userService.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("No user", "Could not find the user"));
            model.addAttribute("userProfile", user.getProfile());
            model.addAttribute("user", user);
        }

        List<Institution> institutions = institutionService.findAll();
        long allDonations = donationService.countAllDonations();
        long allDonationBags = donationService.countAllBags();

        model.addAttribute("institutions", institutions);
        model.addAttribute("allDonations", allDonations);
        model.addAttribute("allDonationBags", allDonationBags);

        return "index";
    }

    @PostMapping("/message")
    public String processMessageForm(HttpServletRequest request, Model model) {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String message = request.getParameter("message");
        String messageEmail = request.getParameter("email");
        User user = null;

        String infoMessage = "Wiadomość nie została wysłana. Wszystkie pola muszą być wypełnione. Kliknij, a zostaniesz przekierowany do formularza";


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            String email = authentication.getName();
            System.out.println(email);
            user = userService.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("No user", "Could not find the user"));
            model.addAttribute("userProfile", user.getProfile());
            model.addAttribute("user", user);
            messageEmail = email;
        }

        if (validateMailMessage(firstName, lastName, message, messageEmail)) {
            String mailMessage = MailMessage.getMailMessage(firstName, lastName, message, user);
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
