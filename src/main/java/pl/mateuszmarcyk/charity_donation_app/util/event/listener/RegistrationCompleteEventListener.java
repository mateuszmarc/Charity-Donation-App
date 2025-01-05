package pl.mateuszmarcyk.charity_donation_app.util.event.listener;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.exception.MailException;
import pl.mateuszmarcyk.charity_donation_app.service.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.util.*;
import pl.mateuszmarcyk.charity_donation_app.util.event.RegistrationCompleteEvent;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    private final MessageSource messageSource;
    private final AppMailSender appMailSender;
    private final VerificationTokenService verificationTokenService;
    private final MailMessage mailMessage;
    private final TokenFactory tokenFactory;
    private final MailFactory mailFactory;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {

        User user = event.getUser();
        String token = UUID.randomUUID().toString();

        int tokenValidTime = Integer.parseInt(messageSource.getMessage("token.valid.time", null, Locale.getDefault()));
        String applicationName = messageSource.getMessage("email.app.name", null, Locale.getDefault());
        String registrationMailSubject = messageSource.getMessage("registration.mail.subject", null, Locale.getDefault());

        VerificationToken verificationToken = tokenFactory.getVerificationToken(token, user, tokenValidTime);
        verificationTokenService.saveToken(verificationToken);


        String url = event.getApplicationUrl() + "/register/verifyEmail?token=" + token;
        String mailContent = mailMessage.buildMessage(url);

        Mail mail = mailFactory.createMail(registrationMailSubject, applicationName, mailContent);

        try {
            appMailSender.sendEmail(user, mail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.info(e.getMessage());
            throw new MailException("Wystąpił błąd podczas wysyłania. Spróbuj ponownie", "Nie można wysłać");
        }
    }
}
