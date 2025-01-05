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
import pl.mateuszmarcyk.charity_donation_app.entity.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.exception.BusinessException;
import pl.mateuszmarcyk.charity_donation_app.exception.MailException;
import pl.mateuszmarcyk.charity_donation_app.service.PasswordResetVerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.util.*;
import pl.mateuszmarcyk.charity_donation_app.util.event.PasswordResetEvent;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetEventListenerTest {

    @InjectMocks
    private PasswordResetEventListener listener;

    @Mock
    private MessageSource messageSource;

    @Mock
    private AppMailSender appMailSender;

    @Mock
    private PasswordResetVerificationTokenService passwordResetVerificationTokenService;

    @Mock
    private MailMessage mailMessage;

    @Mock
    private TokenFactory tokenFactory;

    @Mock
    MailFactory mailFactory;

    @Test
    void givenUserWithExpiredToken_whenEventPublished_thenEmailWithUpdatedTokenIsSent() throws MessagingException, UnsupportedEncodingException {
//        Arrange
        try (MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {
            String appName = "App name";
            String subject = "Test subject";
            String message = "Message";
            String token = "Randomtoken";
            int tokenValidTime = 15;
            String expectedUrl = "http://localhost/app/reset-password/verifyEmail?token=Randomtoken";
            UUID mockUUID = mock(UUID.class);
            User spyUser = spy(new User());
            String applicationUrl = "http://localhost/app";
            PasswordResetVerificationToken spyToken = spy(new PasswordResetVerificationToken(1L, "token", LocalDateTime.now().plusMinutes(15), spyUser, LocalDateTime.now(), false));
            spyUser.setPasswordResetVerificationToken(spyToken);
            Mail mail = new Mail(subject, appName, message);

            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);
            when(mockUUID.toString()).thenReturn(token);
            when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn("15");
            when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
            when(messageSource.getMessage("registration.mail.subject", null, Locale.getDefault())).thenReturn(subject);
            when(mailMessage.buildPasswordResetMessage(expectedUrl)).thenReturn(message);
            PasswordResetEvent spyEvent = spy(new PasswordResetEvent(spyUser, applicationUrl));
            when(spyEvent.getApplicationUrl()).thenReturn(applicationUrl);
            when(mailFactory.createMail(subject, appName, message)).thenReturn(mail);

//            Act
            assertThatNoException().isThrownBy(() -> listener.onApplicationEvent(spyEvent));

//            Assert
            verify(spyEvent, times(1)).getUser();
            verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("registration.mail.subject", null, Locale.getDefault());
            verify(spyUser, times(1)).getPasswordResetVerificationToken();

            ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(spyToken, times(1)).setToken(stringArgumentCaptor.capture());
            String capturedToken = stringArgumentCaptor.getValue();
            assertThat(capturedToken).isEqualTo(token);

            verify(tokenFactory, never()).getVerificationToken(token, spyUser, tokenValidTime);

            verify(spyToken, times(1)).setExpirationTime(any(LocalDateTime.class));

            ArgumentCaptor<PasswordResetVerificationToken> passwordResetVerificationTokenArgumentCaptor = ArgumentCaptor.forClass(PasswordResetVerificationToken.class);
            verify(passwordResetVerificationTokenService, times(1)).save(passwordResetVerificationTokenArgumentCaptor.capture());
            PasswordResetVerificationToken capturedPasswordResetVerificationToken = passwordResetVerificationTokenArgumentCaptor.getValue();
            assertThat(capturedPasswordResetVerificationToken).isEqualTo(spyToken);

            verify(spyEvent, times(1)).getApplicationUrl();

            verify(mailMessage, times(1)).buildPasswordResetMessage(stringArgumentCaptor.capture());
            String capturedUrl = stringArgumentCaptor.getValue();
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

            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
            verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());
            User capturedUser = userArgumentCaptor.getValue();
            Mail capturedMail = mailArgumentCaptor.getValue();


            assertAll(
                    () -> assertThat(capturedUser).isEqualTo(spyUser),
                    () -> assertThat(capturedMail).isEqualTo(mail)
            );
        }
    }

    @Test
    void givenUserWithExpiredToken_whenEventPublished_thenEmailWithNewTokenIsSent() throws MessagingException, UnsupportedEncodingException {
        try (MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {
            String appName = "App name";
            String subject = "Test subject";
            String message = "Message";
            String token = "Randomtoken";
            int tokenValidTime = 15;
            String expectedUrl = "http://localhost/app/reset-password/verifyEmail?token=Randomtoken";
            UUID mockUUID = mock(UUID.class);
            User spyUser = spy(new User());
            String applicationUrl = "http://localhost/app";
            PasswordResetVerificationToken spyToken = spy(new PasswordResetVerificationToken(1L, "token", LocalDateTime.now().plusMinutes(15), spyUser, LocalDateTime.now(), false));
            Mail mail = new Mail(subject, appName, message);

            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);
            when(mockUUID.toString()).thenReturn(token);
            when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn("15");
            when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
            when(messageSource.getMessage("registration.mail.subject", null, Locale.getDefault())).thenReturn(subject);
            when(tokenFactory.getPasswordResetVerificationToken(token, spyUser, tokenValidTime)).thenReturn(spyToken);
            when(mailMessage.buildPasswordResetMessage(expectedUrl)).thenReturn(message);
            PasswordResetEvent spyEvent = spy(new PasswordResetEvent(spyUser, applicationUrl));
            when(spyEvent.getApplicationUrl()).thenReturn(applicationUrl);
            when(mailFactory.createMail(subject, appName, message)).thenReturn(mail);

//            Act
            assertThatNoException().isThrownBy(() -> listener.onApplicationEvent(spyEvent));

//            Assert
            verify(spyEvent, times(1)).getUser();
            verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("registration.mail.subject", null, Locale.getDefault());
            verify(spyUser, times(1)).getPasswordResetVerificationToken();

            ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(spyToken, never()).setToken(stringArgumentCaptor.capture());

            verify(tokenFactory, times(1)).getPasswordResetVerificationToken(token, spyUser, tokenValidTime);

            verify(spyToken, never()).setExpirationTime(any(LocalDateTime.class));

            ArgumentCaptor<PasswordResetVerificationToken> passwordResetVerificationTokenArgumentCaptor = ArgumentCaptor.forClass(PasswordResetVerificationToken.class);
            verify(passwordResetVerificationTokenService, times(1)).save(passwordResetVerificationTokenArgumentCaptor.capture());
            PasswordResetVerificationToken capturedPasswordResetVerificationToken = passwordResetVerificationTokenArgumentCaptor.getValue();
            assertThat(capturedPasswordResetVerificationToken).isEqualTo(spyToken);

            verify(spyEvent, times(1)).getApplicationUrl();

            verify(mailMessage, times(1)).buildPasswordResetMessage(stringArgumentCaptor.capture());
            String capturedUrl = stringArgumentCaptor.getValue();
            assertThat(capturedUrl).isEqualTo(expectedUrl);

            verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
            String capturedSubject = stringArgumentCaptor.getAllValues().get(1);
            String capturedAppName = stringArgumentCaptor.getAllValues().get(2);
            String capturedMessage = stringArgumentCaptor.getAllValues().get(3);

            assertAll(
                    () -> assertThat(capturedSubject).isEqualTo(subject),
                    () -> assertThat(capturedAppName).isEqualTo(appName),
                    () -> assertThat(capturedMessage).isEqualTo(message)
            );

            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
            verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());
            User capturedUser = userArgumentCaptor.getValue();
            Mail capturedMail = mailArgumentCaptor.getValue();


            assertAll(
                    () -> assertThat(capturedUser).isEqualTo(spyUser),
                    () -> assertThat(capturedMail).isEqualTo(mail)
            );
        }
    }

    @Test
    void whenOnApplicationEventAppMailSenderThrowsMessagingException_thenMailExceptionIsThrown() throws MessagingException, UnsupportedEncodingException {
        try (MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {
            String exceptionTitle = "Nie można wysłać";
            String exceptionMessage = "Wystąpił błąd podczas wysyłania. Spróbuj ponownie";
            String appName = "App name";
            String subject = "Test subject";
            String message = "Message";
            String token = "Randomtoken";
            int tokenValidTime = 15;
            String expectedUrl = "http://localhost/app/reset-password/verifyEmail?token=Randomtoken";
            UUID mockUUID = mock(UUID.class);
            User spyUser = spy(new User());
            String applicationUrl = "http://localhost/app";
            PasswordResetVerificationToken spyToken = spy(new PasswordResetVerificationToken(1L, "token", LocalDateTime.now().plusMinutes(15), spyUser, LocalDateTime.now(), false));
            Mail mail = new Mail(subject, appName, message);

            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);
            when(mockUUID.toString()).thenReturn(token);
            when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn("15");
            when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
            when(messageSource.getMessage("registration.mail.subject", null, Locale.getDefault())).thenReturn(subject);
            when(tokenFactory.getPasswordResetVerificationToken(token, spyUser, tokenValidTime)).thenReturn(spyToken);
            when(mailMessage.buildPasswordResetMessage(expectedUrl)).thenReturn(message);
            PasswordResetEvent spyEvent = spy(new PasswordResetEvent(spyUser, applicationUrl));
            when(spyEvent.getApplicationUrl()).thenReturn(applicationUrl);
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
            verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("registration.mail.subject", null, Locale.getDefault());
            verify(spyUser, times(1)).getPasswordResetVerificationToken();

            ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(spyToken, never()).setToken(stringArgumentCaptor.capture());

            verify(tokenFactory, times(1)).getPasswordResetVerificationToken(token, spyUser, tokenValidTime);

            verify(spyToken, never()).setExpirationTime(any(LocalDateTime.class));

            ArgumentCaptor<PasswordResetVerificationToken> passwordResetVerificationTokenArgumentCaptor = ArgumentCaptor.forClass(PasswordResetVerificationToken.class);
            verify(passwordResetVerificationTokenService, times(1)).save(passwordResetVerificationTokenArgumentCaptor.capture());
            PasswordResetVerificationToken capturedPasswordResetVerificationToken = passwordResetVerificationTokenArgumentCaptor.getValue();
            assertThat(capturedPasswordResetVerificationToken).isEqualTo(spyToken);

            verify(spyEvent, times(1)).getApplicationUrl();

            verify(mailMessage, times(1)).buildPasswordResetMessage(stringArgumentCaptor.capture());
            String capturedUrl = stringArgumentCaptor.getValue();
            assertThat(capturedUrl).isEqualTo(expectedUrl);

            verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
            String capturedSubject = stringArgumentCaptor.getAllValues().get(1);
            String capturedAppName = stringArgumentCaptor.getAllValues().get(2);
            String capturedMessage = stringArgumentCaptor.getAllValues().get(3);

            assertAll(
                    () -> assertThat(capturedSubject).isEqualTo(subject),
                    () -> assertThat(capturedAppName).isEqualTo(appName),
                    () -> assertThat(capturedMessage).isEqualTo(message)
            );

            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
            verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());
            User capturedUser = userArgumentCaptor.getValue();
            Mail capturedMail = mailArgumentCaptor.getValue();


            assertAll(
                    () -> assertThat(capturedUser).isEqualTo(spyUser),
                    () -> assertThat(capturedMail).isEqualTo(mail)
            );
        }
    }

    @Test
    void whenOnApplicationEventAppMailSenderThrowsUnsupportedEncodingException_thenMailExceptionIsThrown() throws MessagingException, UnsupportedEncodingException {
        try (MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {
            String exceptionTitle = "Nie można wysłać";
            String exceptionMessage = "Wystąpił błąd podczas wysyłania. Spróbuj ponownie";
            String appName = "App name";
            String subject = "Test subject";
            String message = "Message";
            String token = "Randomtoken";
            int tokenValidTime = 15;
            String expectedUrl = "http://localhost/app/reset-password/verifyEmail?token=Randomtoken";
            UUID mockUUID = mock(UUID.class);
            User spyUser = spy(new User());
            String applicationUrl = "http://localhost/app";
            PasswordResetVerificationToken spyToken = spy(new PasswordResetVerificationToken(1L, "token", LocalDateTime.now().plusMinutes(15), spyUser, LocalDateTime.now(), false));
            Mail mail = new Mail(subject, appName, message);

            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);
            when(mockUUID.toString()).thenReturn(token);
            when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn("15");
            when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
            when(messageSource.getMessage("registration.mail.subject", null, Locale.getDefault())).thenReturn(subject);
            when(tokenFactory.getPasswordResetVerificationToken(token, spyUser, tokenValidTime)).thenReturn(spyToken);
            when(mailMessage.buildPasswordResetMessage(expectedUrl)).thenReturn(message);
            PasswordResetEvent spyEvent = spy(new PasswordResetEvent(spyUser, applicationUrl));
            when(spyEvent.getApplicationUrl()).thenReturn(applicationUrl);
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
            verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("registration.mail.subject", null, Locale.getDefault());
            verify(spyUser, times(1)).getPasswordResetVerificationToken();

            ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(spyToken, never()).setToken(stringArgumentCaptor.capture());

            verify(tokenFactory, times(1)).getPasswordResetVerificationToken(token, spyUser, tokenValidTime);

            verify(spyToken, never()).setExpirationTime(any(LocalDateTime.class));

            ArgumentCaptor<PasswordResetVerificationToken> passwordResetVerificationTokenArgumentCaptor = ArgumentCaptor.forClass(PasswordResetVerificationToken.class);
            verify(passwordResetVerificationTokenService, times(1)).save(passwordResetVerificationTokenArgumentCaptor.capture());
            PasswordResetVerificationToken capturedPasswordResetVerificationToken = passwordResetVerificationTokenArgumentCaptor.getValue();
            assertThat(capturedPasswordResetVerificationToken).isEqualTo(spyToken);

            verify(spyEvent, times(1)).getApplicationUrl();

            verify(mailMessage, times(1)).buildPasswordResetMessage(stringArgumentCaptor.capture());
            String capturedUrl = stringArgumentCaptor.getValue();
            assertThat(capturedUrl).isEqualTo(expectedUrl);

            verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
            String capturedSubject = stringArgumentCaptor.getAllValues().get(1);
            String capturedAppName = stringArgumentCaptor.getAllValues().get(2);
            String capturedMessage = stringArgumentCaptor.getAllValues().get(3);

            assertAll(
                    () -> assertThat(capturedSubject).isEqualTo(subject),
                    () -> assertThat(capturedAppName).isEqualTo(appName),
                    () -> assertThat(capturedMessage).isEqualTo(message)
            );

            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
            verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());
            User capturedUser = userArgumentCaptor.getValue();
            Mail capturedMail = mailArgumentCaptor.getValue();


            assertAll(
                    () -> assertThat(capturedUser).isEqualTo(spyUser),
                    () -> assertThat(capturedMail).isEqualTo(mail)
            );
        }
    }
}





















