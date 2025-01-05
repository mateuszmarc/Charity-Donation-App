package pl.mateuszmarcyk.charity_donation_app.util.event.listener;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.exception.BusinessException;
import pl.mateuszmarcyk.charity_donation_app.exception.MailException;
import pl.mateuszmarcyk.charity_donation_app.service.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;
import pl.mateuszmarcyk.charity_donation_app.util.MailFactory;
import pl.mateuszmarcyk.charity_donation_app.util.MailMessage;
import pl.mateuszmarcyk.charity_donation_app.util.event.ResendTokenEvent;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResendTokenEventListenerTest {

    @InjectMocks
    private ResendTokenEventListener listener;

    @Mock
    private  MessageSource messageSource;

    @Mock
    private  VerificationTokenService verificationTokenService;

    @Mock
    private  AppMailSender appMailSender;

    @Mock
    private  MailMessage mailMessage;

    @Mock
    private  MailFactory mailFactory;

    @Test
    void givenResendTokenEventListener_whenOnApplicationEvent_thenEmailWithNewTokenIsSent() throws MessagingException, UnsupportedEncodingException {
//        Arrange
        try (MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {
            String token = "token";
            String newToken = "newToken";
            String appName = "App name";
            String subject = "Test subject";
            String message = "Message";
            String expectedUrl = "http://localhost/app/register/verifyEmail?token=newToken";
            UUID mockUUID = mock(UUID.class);

            User user = new User();
            VerificationToken spyOldVerificationToken = spy(new VerificationToken(1L, token, LocalDateTime.now().plusMinutes(15), user, LocalDateTime.now()));
            String applicationUrl = "http://localhost/app";

            ResendTokenEvent spyEvent = spy(new ResendTokenEvent(user, applicationUrl, spyOldVerificationToken));
            Mail mail = new Mail(subject, appName, message);

            when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn("15");
            when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
            when(messageSource.getMessage("registration.mail.subject", null, Locale.getDefault())).thenReturn(subject);
            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);
            when(mockUUID.toString()).thenReturn(newToken);
            when(mailMessage.buildMessage(expectedUrl)).thenReturn(message);
            when(mailFactory.createMail(subject, appName, message)).thenReturn(mail);

//            Act & assert
            assertThatNoException().isThrownBy(() -> listener.onApplicationEvent(spyEvent));

            verify(spyEvent, times(1)).getUser();
            verify(spyEvent, times(1)).getOldToken();
            verify(spyEvent, times(1)).getApplicationUrl();

            verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("registration.mail.subject", null, Locale.getDefault());

            uuidMockedStatic.verify(UUID::randomUUID, times(1));

            verify(spyOldVerificationToken, times(1)).setExpirationTime(any(LocalDateTime.class));

            ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(spyOldVerificationToken, times(1)).setToken(stringArgumentCaptor.capture());
            String capturedToken = stringArgumentCaptor.getValue();
            assertThat(capturedToken).isEqualTo(newToken);

            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            verify(spyOldVerificationToken, times(1)).setUser(userArgumentCaptor.capture());
            User capturedUser = userArgumentCaptor.getValue();
            assertThat(capturedUser).isEqualTo(user);

            ArgumentCaptor<VerificationToken> verificationTokenArgumentCaptor = ArgumentCaptor.forClass(VerificationToken.class);
            verify(verificationTokenService, times(1)).saveToken(verificationTokenArgumentCaptor.capture());
            VerificationToken capturedVerificationToken = verificationTokenArgumentCaptor.getValue();
            assertThat(capturedVerificationToken).isEqualTo(spyOldVerificationToken);

            verify(mailMessage, times(1)).buildMessage(stringArgumentCaptor.capture());
            String capturedUrl = stringArgumentCaptor.getAllValues().get(1);
            assertThat(capturedUrl).isEqualTo(expectedUrl);

            verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
            String capturedSubject = stringArgumentCaptor.getAllValues().get(2);
            String capturedAppName = stringArgumentCaptor.getAllValues().get(3);
            String capturedMessage = stringArgumentCaptor.getAllValues().get(4);

            assertAll(
                    () -> assertThat(capturedSubject).isEqualTo(subject),
                    () -> assertThat(capturedAppName).isEqualTo(appName),
                    () -> assertThat(capturedMessage).isEqualTo(message)
            );

            ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
            verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());

            User capturedUserForAppMailSender = userArgumentCaptor.getAllValues().get(1);
            Mail capturedMail = mailArgumentCaptor.getValue();

            assertAll(
                    () -> assertThat(capturedUserForAppMailSender).isEqualTo(user),
                    () -> assertThat(capturedMail).isEqualTo(mail)
            );
        }
    }

    @Test
    void whenOnApplicationEventAppMailSenderThrowsMessagingException_thenMailExceptionIsThrown() throws MessagingException, UnsupportedEncodingException {
//        Arrange
        try (MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {
            String exceptionTitle = "Nie można wysłać";
            String exceptionMessage = "Wystąpił błąd podczas wysyłania. Spróbuj ponownie";
            String token = "token";
            String newToken = "newToken";
            String appName = "App name";
            String subject = "Test subject";
            String message = "Message";
            String expectedUrl = "http://localhost/app/register/verifyEmail?token=newToken";
            UUID mockUUID = mock(UUID.class);

            User user = new User();
            VerificationToken spyOldVerificationToken = spy(new VerificationToken(1L, token, LocalDateTime.now().plusMinutes(15), user, LocalDateTime.now()));
            String applicationUrl = "http://localhost/app";

            ResendTokenEvent spyEvent = spy(new ResendTokenEvent(user, applicationUrl, spyOldVerificationToken));
            Mail mail = new Mail(subject, appName, message);

            when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn("15");
            when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
            when(messageSource.getMessage("registration.mail.subject", null, Locale.getDefault())).thenReturn(subject);
            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);
            when(mockUUID.toString()).thenReturn(newToken);
            when(mailMessage.buildMessage(expectedUrl)).thenReturn(message);
            when(mailFactory.createMail(subject, appName, message)).thenReturn(mail);

            doThrow(new MessagingException("message")).when(appMailSender).sendEmail(any(User.class), any(Mail.class));


//            Act & Assert
            Throwable thrown = catchThrowable(() -> listener.onApplicationEvent(spyEvent));
            assertThat(thrown).isInstanceOf(MailException.class);
            if (thrown instanceof BusinessException e) {
                assertAll(
                        () -> assertThat(e.getTitle()).isEqualTo(exceptionTitle),
                        () -> assertThat(e.getMessage()).isEqualTo(exceptionMessage)
                );
            }

            verify(spyEvent, times(1)).getUser();
            verify(spyEvent, times(1)).getOldToken();
            verify(spyEvent, times(1)).getApplicationUrl();

            verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("registration.mail.subject", null, Locale.getDefault());

            uuidMockedStatic.verify(UUID::randomUUID, times(1));

            verify(spyOldVerificationToken, times(1)).setExpirationTime(any(LocalDateTime.class));

            ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(spyOldVerificationToken, times(1)).setToken(stringArgumentCaptor.capture());
            String capturedToken = stringArgumentCaptor.getValue();
            assertThat(capturedToken).isEqualTo(newToken);

            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            verify(spyOldVerificationToken, times(1)).setUser(userArgumentCaptor.capture());
            User capturedUser = userArgumentCaptor.getValue();
            assertThat(capturedUser).isEqualTo(user);

            ArgumentCaptor<VerificationToken> verificationTokenArgumentCaptor = ArgumentCaptor.forClass(VerificationToken.class);
            verify(verificationTokenService, times(1)).saveToken(verificationTokenArgumentCaptor.capture());
            VerificationToken capturedVerificationToken = verificationTokenArgumentCaptor.getValue();
            assertThat(capturedVerificationToken).isEqualTo(spyOldVerificationToken);

            verify(mailMessage, times(1)).buildMessage(stringArgumentCaptor.capture());
            String capturedUrl = stringArgumentCaptor.getAllValues().get(1);
            assertThat(capturedUrl).isEqualTo(expectedUrl);

            verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
            String capturedSubject = stringArgumentCaptor.getAllValues().get(2);
            String capturedAppName = stringArgumentCaptor.getAllValues().get(3);
            String capturedMessage = stringArgumentCaptor.getAllValues().get(4);

            assertAll(
                    () -> assertThat(capturedSubject).isEqualTo(subject),
                    () -> assertThat(capturedAppName).isEqualTo(appName),
                    () -> assertThat(capturedMessage).isEqualTo(message)
            );

            ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
            verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());

            User capturedUserForAppMailSender = userArgumentCaptor.getAllValues().get(1);
            Mail capturedMail = mailArgumentCaptor.getValue();

            assertAll(
                    () -> assertThat(capturedUserForAppMailSender).isEqualTo(user),
                    () -> assertThat(capturedMail).isEqualTo(mail)
            );
        }
    }

    @Test
    void whenOnApplicationEventAppMailSenderThrowsUnsupportedEncodingException_thenMailExceptionIsThrown() throws MessagingException, UnsupportedEncodingException {
//        Arrange
        try (MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {
            String exceptionTitle = "Nie można wysłać";
            String exceptionMessage = "Wystąpił błąd podczas wysyłania. Spróbuj ponownie";
            String token = "token";
            String newToken = "newToken";
            String appName = "App name";
            String subject = "Test subject";
            String message = "Message";
            String expectedUrl = "http://localhost/app/register/verifyEmail?token=newToken";
            UUID mockUUID = mock(UUID.class);

            User user = new User();
            VerificationToken spyOldVerificationToken = spy(new VerificationToken(1L, token, LocalDateTime.now().plusMinutes(15), user, LocalDateTime.now()));
            String applicationUrl = "http://localhost/app";

            ResendTokenEvent spyEvent = spy(new ResendTokenEvent(user, applicationUrl, spyOldVerificationToken));
            Mail mail = new Mail(subject, appName, message);

            when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn("15");
            when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
            when(messageSource.getMessage("registration.mail.subject", null, Locale.getDefault())).thenReturn(subject);
            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);
            when(mockUUID.toString()).thenReturn(newToken);
            when(mailMessage.buildMessage(expectedUrl)).thenReturn(message);
            when(mailFactory.createMail(subject, appName, message)).thenReturn(mail);

            doThrow(new UnsupportedEncodingException("message")).when(appMailSender).sendEmail(any(User.class), any(Mail.class));


//            Act & Assert
            Throwable thrown = catchThrowable(() -> listener.onApplicationEvent(spyEvent));
            assertThat(thrown).isInstanceOf(MailException.class);
            if (thrown instanceof BusinessException e) {
                assertAll(
                        () -> assertThat(e.getTitle()).isEqualTo(exceptionTitle),
                        () -> assertThat(e.getMessage()).isEqualTo(exceptionMessage)
                );
            }

            verify(spyEvent, times(1)).getUser();
            verify(spyEvent, times(1)).getOldToken();
            verify(spyEvent, times(1)).getApplicationUrl();

            verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("registration.mail.subject", null, Locale.getDefault());

            uuidMockedStatic.verify(UUID::randomUUID, times(1));

            verify(spyOldVerificationToken, times(1)).setExpirationTime(any(LocalDateTime.class));

            ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(spyOldVerificationToken, times(1)).setToken(stringArgumentCaptor.capture());
            String capturedToken = stringArgumentCaptor.getValue();
            assertThat(capturedToken).isEqualTo(newToken);

            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            verify(spyOldVerificationToken, times(1)).setUser(userArgumentCaptor.capture());
            User capturedUser = userArgumentCaptor.getValue();
            assertThat(capturedUser).isEqualTo(user);

            ArgumentCaptor<VerificationToken> verificationTokenArgumentCaptor = ArgumentCaptor.forClass(VerificationToken.class);
            verify(verificationTokenService, times(1)).saveToken(verificationTokenArgumentCaptor.capture());
            VerificationToken capturedVerificationToken = verificationTokenArgumentCaptor.getValue();
            assertThat(capturedVerificationToken).isEqualTo(spyOldVerificationToken);

            verify(mailMessage, times(1)).buildMessage(stringArgumentCaptor.capture());
            String capturedUrl = stringArgumentCaptor.getAllValues().get(1);
            assertThat(capturedUrl).isEqualTo(expectedUrl);

            verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
            String capturedSubject = stringArgumentCaptor.getAllValues().get(2);
            String capturedAppName = stringArgumentCaptor.getAllValues().get(3);
            String capturedMessage = stringArgumentCaptor.getAllValues().get(4);

            assertAll(
                    () -> assertThat(capturedSubject).isEqualTo(subject),
                    () -> assertThat(capturedAppName).isEqualTo(appName),
                    () -> assertThat(capturedMessage).isEqualTo(message)
            );

            ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
            verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());

            User capturedUserForAppMailSender = userArgumentCaptor.getAllValues().get(1);
            Mail capturedMail = mailArgumentCaptor.getValue();

            assertAll(
                    () -> assertThat(capturedUserForAppMailSender).isEqualTo(user),
                    () -> assertThat(capturedMail).isEqualTo(mail)
            );

        }
    }
}