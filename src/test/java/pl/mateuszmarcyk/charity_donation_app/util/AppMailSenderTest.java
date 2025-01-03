package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.io.UnsupportedEncodingException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppMailSenderTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessageHelperFactory mimeMessageHelperFactory;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    MimeMessageHelper mimeMessageHelper;

    private AppMailSender appMailSender;


    @BeforeEach
    void setUp() {
        appMailSender = new AppMailSender(javaMailSender, "test@gmail.com", mimeMessageHelperFactory);
    }

    @Test
    void givenUserAndMail_whenSendEmail_thenEmailIsSent() throws MessagingException, UnsupportedEncodingException {

        User user = getuUser();
        Mail mail = getMail();

        ArgumentCaptor<MimeMessage> mimeMessageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        ArgumentCaptor<String> emailArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(mimeMessageHelperFactory.createHelper(mimeMessage, "test@gmail.com", mail.getSenderName(), mail.getSubject(), mail.getMailContent())).thenReturn(mimeMessageHelper);

        appMailSender.sendEmail(user, mail);

        verify(javaMailSender, times(1)).send(mimeMessageArgumentCaptor.capture());
        assertThat(mimeMessageArgumentCaptor.getValue()).isEqualTo(mimeMessage);

        verify(mimeMessageHelper).setTo(emailArgumentCaptor.capture());
        String setEmail = emailArgumentCaptor.getValue();
        assertThat(setEmail).isEqualTo(user.getEmail());
    }

    @Test
    void givenNullUserAndMail_whenSendEmail_thenNullPointerExceptionIsThrown() throws MessagingException, UnsupportedEncodingException {

        User user = null;
        Mail mail = getMail();

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(mimeMessageHelperFactory.createHelper(mimeMessage, "test@gmail.com", mail.getSenderName(), mail.getSubject(), mail.getMailContent())).thenReturn(mimeMessageHelper);

        assertThatThrownBy(() -> appMailSender.sendEmail(user, mail)).isInstanceOf(NullPointerException.class);

        verify(javaMailSender, never()).send(mimeMessage);
        verify(mimeMessageHelper, never()).setTo(anyString());
    }

    @Test
    void givenNullUserAndNullMail_whenSendEmail_thenNullPointerExceptionIsThrown() throws MessagingException {

        User user = null;
        Mail mail = null;

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThatThrownBy(() -> appMailSender.sendEmail(user, mail)).isInstanceOf(NullPointerException.class);

        verify(javaMailSender, times(1)).createMimeMessage();
        verify(mimeMessageHelper, never()).setTo(anyString());
        verify(javaMailSender, never()).send(mimeMessage);
    }

    @Test
    void givenUserAndNullMail_whenSendEmail_thenNullPointerExceptionIsThrown() throws MessagingException {

        User user = getuUser();
        Mail mail = null;

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThatThrownBy(() -> appMailSender.sendEmail(user, mail)).isInstanceOf(NullPointerException.class);

        verify(javaMailSender, times(1)).createMimeMessage();
        verify(mimeMessageHelper, never()).setTo(anyString());
        verify(javaMailSender, never()).send(mimeMessage);
    }

    @Test
    void givenMail_whenSendMailMessage_thenMessageSent() throws MessagingException, UnsupportedEncodingException {

        Mail mail = getMail();

        ArgumentCaptor<MimeMessage> mimeMessageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        ArgumentCaptor<String> emailArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(mimeMessageHelperFactory.createHelper(mimeMessage, "test@gmail.com", mail.getSenderName(), mail.getSubject(), mail.getMailContent())).thenReturn(mimeMessageHelper);

        appMailSender.sendMailMessage(mail);

        verify(mimeMessageHelper, times(1)).setTo(emailArgumentCaptor.capture());
        String setEmail = emailArgumentCaptor.getValue();
        assertThat(setEmail).isEqualTo("test@gmail.com");

        verify(javaMailSender, times(1)).send(mimeMessageArgumentCaptor.capture());
        assertThat(mimeMessageArgumentCaptor.getValue()).isEqualTo(mimeMessage);
    }

    @Test
    void givenNullMail_whenSendMailMessage_thenNullPointerExceptionIsThrown() {

        Mail mail = null;
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

       assertThatThrownBy(() -> appMailSender.sendMailMessage(mail)).isInstanceOf(NullPointerException.class);

        verify(javaMailSender, never()).send(mimeMessage);
    }


    public static Mail getMail() {
        return new Mail("Test Subject", "Test Sender", "<p>Test Content</p>");
    }

    public static User getuUser() {
        User user = new User();
        user.setEmail("user.email@gmail.com");
        return user;
    }
}