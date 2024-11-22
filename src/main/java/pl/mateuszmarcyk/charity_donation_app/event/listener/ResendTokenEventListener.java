package pl.mateuszmarcyk.charity_donation_app.event.listener;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.event.ResendTokenEvent;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class ResendTokenEventListener implements ApplicationListener<ResendTokenEvent> {

    @Value("${token.valid.time}")
    private int tokenValidTime;

    @Value("${spring.mail.username}")
    private String appEmail;

    @Value("${registration.mail.message}")
    private String mailContent;

    @Value("${registration.mail.subject}")
    private String registrationMailSubject;

    @Value("${email.app.name}")
    private String applicationName;

    private final VerificationTokenService verificationTokenService;
    private final AppMailSender appMailSender;

    @Override
    public void onApplicationEvent(ResendTokenEvent event) {

        User user = event.getUser();
        VerificationToken oldVerificationToken = event.getOldToken();
        String applicationUrl = event.getApplicationUrl();

        String newToken = UUID.randomUUID().toString();
        oldVerificationToken.setExpirationTime(LocalDateTime.now().plusMinutes(tokenValidTime));
        oldVerificationToken.setToken(newToken);
        oldVerificationToken.setUser(user);

        Mail mail = new Mail(applicationName, registrationMailSubject, mailContent);

        verificationTokenService.saveToken(oldVerificationToken);

        String url = applicationUrl + "/register/verifyEmail?token=" + newToken;

        try {
            appMailSender.sendEmail(user, url, mail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
