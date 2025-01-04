package pl.mateuszmarcyk.charity_donation_app.util.event;

import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;

@Component
public class MailFactory {

    public Mail createMail(String subject, String applicationName, String message) {
        return new Mail(subject,  applicationName, message);
    }
}
