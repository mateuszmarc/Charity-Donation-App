package pl.mateuszmarcyk.charity_donation_app.util.event.listener;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.util.event.PasswordResetEvent;
import pl.mateuszmarcyk.charity_donation_app.entity.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.service.PasswordResetVerificationTokenService;
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
public class PasswordResetEventListener implements ApplicationListener<PasswordResetEvent> {

    private final PasswordResetVerificationTokenService passwordResetVerificationTokenService;
    private final AppMailSender appMailSender;
    private final MessageSource messageSource;
    private final MailMessage mailMessage;

    @Override
    public void onApplicationEvent(PasswordResetEvent event) {

        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        int tokenValidTime = Integer.parseInt(messageSource.getMessage("token.valid.time", null, Locale.getDefault()));
        String applicationName = messageSource.getMessage("email.app.name", null, Locale.getDefault());
        String registrationMailSubject = messageSource.getMessage("registration.mail.subject", null, Locale.getDefault());

        PasswordResetVerificationToken passwordResetVerificationToken =  user.getPasswordResetVerificationToken();
        if (passwordResetVerificationToken != null) {
            passwordResetVerificationToken.setToken(token);
            passwordResetVerificationToken.setExpirationTime(LocalDateTime.now().plusMinutes(tokenValidTime));
        } else {
            passwordResetVerificationToken = new PasswordResetVerificationToken(token, user, tokenValidTime);
        }
        passwordResetVerificationTokenService.save(passwordResetVerificationToken);

        String url = event.getApplicationUrl() + "/reset-password/verifyEmail?token=" + token;
        String mailContent = mailMessage.buildPasswordResetMessage(url);
        Mail mail = new Mail(applicationName, registrationMailSubject, mailContent);

        try {
            appMailSender.sendEmail(user, mail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
