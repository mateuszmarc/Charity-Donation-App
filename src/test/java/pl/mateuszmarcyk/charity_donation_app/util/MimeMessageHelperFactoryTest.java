package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
class MimeMessageHelperFactoryTest {

    private MimeMessageHelperFactory mimeMessageHelperFactory = new MimeMessageHelperFactory();

    @Mock
    private MimeMessage mimeMessage;


    @Test
    void givenNullMimeMessage_whenCreateHelper_thenNullPointerExceptionExceptionThrown() throws MessagingException, UnsupportedEncodingException {
        MimeMessage mimeMessage = null;
        String from = "app@mail.com";
        String senderName = "Test sender";
        String subject = "Test subject";
        String content = "<p>Test content</p";
        assertThatThrownBy(() -> mimeMessageHelperFactory.createHelper(mimeMessage, from, senderName, subject, content)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void givenMimeMessageAndNullFrom_whenCreateHelper_thenNullPointerExceptionExceptionThrown() throws MessagingException, UnsupportedEncodingException {
        String from = null;
        String senderName = "Test sender";
        String subject = "Test subject";
        String content = "<p>Test content</p";
        assertThatThrownBy(() -> mimeMessageHelperFactory.createHelper(mimeMessage, from, senderName, subject, content))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("From address must not be null");
    }

    @Test
    void givenMimeMessageAndNullSenderName_whenCreateHelper_thenNullPointerExceptionExceptionThrown() throws MessagingException, UnsupportedEncodingException {
        String from = "app@mail.com";
        String senderName = null;
        String subject = "Test subject";
        String content = "<p>Test content</p";
        assertThatThrownBy(() -> mimeMessageHelperFactory.createHelper(mimeMessage, from, senderName, subject, content))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Sender name must be provided");
    }

    @Test
    void givenMimeMessageAndEmptySenderName_whenCreateHelper_thenNullPointerExceptionExceptionThrown() throws MessagingException, UnsupportedEncodingException {
        String from = "app@mail.com";
        String senderName = "";
        String subject = "Test subject";
        String content = "<p>Test content</p";
        assertThatThrownBy(() -> mimeMessageHelperFactory.createHelper(mimeMessage, from, senderName, subject, content))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Sender name must be provided");
    }

    @Test
    void givenMimeMessageAndNullSubject_whenCreateHelper_thenNullPointerExceptionExceptionThrown() throws MessagingException, UnsupportedEncodingException {
        String from = "app@mail.com";
        String senderName = "Test Sender";
        String subject = null;
        String content = "<p>Test content</p";
        assertThatThrownBy(() -> mimeMessageHelperFactory.createHelper(mimeMessage, from, senderName, subject, content))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Subject must be provided");
    }

    @Test
    void givenMimeMessageAndEmptySubject_whenCreateHelper_thenNullPointerExceptionExceptionThrown() throws MessagingException, UnsupportedEncodingException {
        String from = "app@mail.com";
        String senderName = "Test Sender";
        String subject = "";
        String content = "<p>Test content</p";
        assertThatThrownBy(() -> mimeMessageHelperFactory.createHelper(mimeMessage, from, senderName, subject, content))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Subject must be provided");
    }

    @Test
    void givenMimeMessageAndNullContent_whenCreateHelper_thenNullPointerExceptionExceptionThrown() throws MessagingException, UnsupportedEncodingException {
        String from = "app@mail.com";
        String senderName = "Test Sender";
        String subject = "Test subject";
        String content = null;
        assertThatThrownBy(() -> mimeMessageHelperFactory.createHelper(mimeMessage, from, senderName, subject, content))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Text must not be null");
    }

    @Test
    void givenMimeMessageAndAllValidArguments_whenCreateHelper_thenHelperCreated() throws MessagingException, UnsupportedEncodingException {
        MimeMessage message = mimeMessage;
        String from = "app@mail.com";
        String senderName = "Test Sender";
        String subject = "Test subject";
        String content = "<p>Test content</p";

        MimeMessageHelper helper = mimeMessageHelperFactory.createHelper(mimeMessage, from, senderName, subject, content);

        assertAll(
                () -> assertThat(helper.getMimeMessage()).isEqualTo(message),
                () -> assertThat(helper.getEncoding()).isEqualTo("UTF-8"),
                () -> assertThat(helper.isMultipart()).isTrue()
        );
    }
}