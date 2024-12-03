package pl.mateuszmarcyk.charity_donation_app.event.listener;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.event.PasswordResetEvent;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.PasswordResetVerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;
import pl.mateuszmarcyk.charity_donation_app.util.RegistrationMail;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class PasswordResetEventListener implements ApplicationListener<PasswordResetEvent> {

    private final PasswordResetVerificationTokenService passwordResetVerificationTokenService;
    private final AppMailSender appMailSender;

    @Value("${token.valid.time}")
    private int tokenValidTime;

    @Value("${registration.mail.subject}")
    private String registrationMailSubject;

    @Value("${email.app.name}")
    private String applicationName;

    @Override
    public void onApplicationEvent(PasswordResetEvent event) {

        User user = event.getUser();
        String token = UUID.randomUUID().toString();

        PasswordResetVerificationToken passwordResetVerificationToken = new PasswordResetVerificationToken(token, user, tokenValidTime);
        passwordResetVerificationTokenService.save(passwordResetVerificationToken);


        String url = event.getApplicationUrl() + "password-reset/token=" + token;
        String mailContent = RegistrationMail.buildPasswordResetMessage(url);
        Mail mail = new Mail(applicationName, registrationMailSubject, mailContent);


        try {
            appMailSender.sendEmail(user, mail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
