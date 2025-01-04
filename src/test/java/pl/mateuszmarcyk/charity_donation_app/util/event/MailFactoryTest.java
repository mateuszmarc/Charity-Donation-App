package pl.mateuszmarcyk.charity_donation_app.util.event;

import org.junit.jupiter.api.Test;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class MailFactoryTest {

    private final MailFactory mailFactory = new MailFactory();

    @Test
    void givenMailFactory_whenCreateMail_thenMailCreated() {
        String subject = "Donation subject";
        String appName = "App Name";
        String content = "Test content";

        Mail mail = mailFactory.createMail(subject, appName, content);

        assertAll(
                () -> assertThat(mail.getSubject()).isEqualTo(subject),
                () -> assertThat(mail.getSenderName()).isEqualTo(appName),
                () -> assertThat(mail.getMailContent()).isEqualTo(content)
        );
    }
}