package pl.mateuszmarcyk.charity_donation_app.event.listener;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.event.RegistrationCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;
import pl.mateuszmarcyk.charity_donation_app.util.RegistrationMail;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    private final MessageSource messageSource;
    private final AppMailSender appMailSender;
    private final VerificationTokenService verificationTokenService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {

        User user = event.getUser();
        String token = UUID.randomUUID().toString();

        int tokenValidTime = Integer.parseInt(messageSource.getMessage("token.valid.time", null, Locale.getDefault()));
        String applicationName = messageSource.getMessage("email.app.name", null, Locale.getDefault());
        String registrationMailSubject = messageSource.getMessage("registration.mail.subject", null, Locale.getDefault());

        VerificationToken verificationToken = new VerificationToken(token, user, tokenValidTime);
        verificationTokenService.saveToken(verificationToken);

        String url = event.getApplicationUrl() + "/register/verifyEmail?token=" + token;
        String mailContent = RegistrationMail.buildMessage(url);

        Mail mail = new Mail(applicationName, registrationMailSubject, mailContent);

        try {
            appMailSender.sendEmail(user, mail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
