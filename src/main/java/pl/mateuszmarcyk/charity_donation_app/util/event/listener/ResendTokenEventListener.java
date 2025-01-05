package pl.mateuszmarcyk.charity_donation_app.util.event.listener;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.exception.MailException;
import pl.mateuszmarcyk.charity_donation_app.util.MailFactory;
import pl.mateuszmarcyk.charity_donation_app.util.event.ResendTokenEvent;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.service.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;
import pl.mateuszmarcyk.charity_donation_app.util.MailMessage;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class ResendTokenEventListener implements ApplicationListener<ResendTokenEvent> {

    private final MessageSource messageSource;
    private final VerificationTokenService verificationTokenService;
    private final AppMailSender appMailSender;
    private final MailMessage mailMessage;
    private final MailFactory mailFactory;

    @Override
    public void onApplicationEvent(ResendTokenEvent event) {

        User user = event.getUser();
        VerificationToken oldVerificationToken = event.getOldToken();
        String applicationUrl = event.getApplicationUrl();

        int tokenValidTime = Integer.parseInt(messageSource.getMessage("token.valid.time", null, Locale.getDefault()));
        String applicationName = messageSource.getMessage("email.app.name", null, Locale.getDefault());
        String registrationMailSubject = messageSource.getMessage("registration.mail.subject", null, Locale.getDefault());

        String newToken = UUID.randomUUID().toString();
        oldVerificationToken.setExpirationTime(LocalDateTime.now().plusMinutes(tokenValidTime));
        oldVerificationToken.setToken(newToken);
        oldVerificationToken.setUser(user);

        verificationTokenService.saveToken(oldVerificationToken);

        String url = applicationUrl + "/register/verifyEmail?token=" + newToken;
        String registrationMailContent = mailMessage.buildMessage(url);
        Mail mail = mailFactory.createMail(registrationMailSubject, applicationName, registrationMailContent);

        try {
            appMailSender.sendEmail(user, mail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new MailException("Wystąpił błąd podczas wysyłania. Spróbuj ponownie", "Nie można wysłać");        }
    }
}
