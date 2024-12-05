package pl.mateuszmarcyk.charity_donation_app.event.listener;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.event.ResendTokenEvent;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.user.User;
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
        String registrationMailContent = MailMessage.buildMessage(url);
        Mail mail = new Mail(applicationName, registrationMailSubject, registrationMailContent);

        try {
            appMailSender.sendEmail(user, mail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
