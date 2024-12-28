package pl.mateuszmarcyk.charity_donation_app.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.util.event.RegistrationCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.util.event.ResendTokenEvent;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @InjectMocks
    private RegistrationService registrationService;

    @Mock
    private UserService userService;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private MessageSource messageSource;

    @Test
    void givenRegistrationService_whenGetApplicationUrl_ThenGeneratedUrlCorrect() {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getServerName()).thenReturn("localhost");
        when(servletRequest.getServerPort()).thenReturn(8000);
        when(servletRequest.getContextPath()).thenReturn("/app");

        String expectedUrl = "http://localhost:8000/app";
        String actualUrl = registrationService.getApplicationUrl(servletRequest);

        verify(servletRequest, times(1)).getServerName();
        verify(servletRequest, times(1)).getServerPort();
        verify(servletRequest, times(1)).getContextPath();

        assertThat(actualUrl).isEqualTo(expectedUrl);
    }

    @Test
    void givenRegistrationService_whenRegisterUser_thenUserServiceSaveInvokedAndPublisherPublishEventInvoked() {
        User user = new User();
        user.setId(null);
        user.setEmail("mmarcykiewicz@gmail.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("mmarcykiewicz@gmail.com");

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getServerName()).thenReturn("localhost");
        when(servletRequest.getServerPort()).thenReturn(8000);
        when(servletRequest.getContextPath()).thenReturn("/app");

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userService.save(user)).thenReturn(savedUser);

        String applicationUrl = "http://localhost:8000/app";

        ArgumentCaptor<RegistrationCompleteEvent> registrationCompleteEventArgumentCaptor = ArgumentCaptor.forClass(RegistrationCompleteEvent.class);

        registrationService.registerUser(user, servletRequest);
        verify(userService).save(userArgumentCaptor.capture());
        User userSavedByService = userArgumentCaptor.getValue();
        assertThat(userSavedByService).isEqualTo(user);

        verify(publisher).publishEvent(registrationCompleteEventArgumentCaptor.capture());
        RegistrationCompleteEvent publishedEvent = registrationCompleteEventArgumentCaptor.getValue();
        assertThat(publishedEvent.getUser()).isEqualTo(savedUser);
        assertThat(publishedEvent.getApplicationUrl()).isEqualTo(applicationUrl);

        verify(servletRequest, times(1)).getServerName();
        verify(servletRequest, times(1)).getServerPort();
        verify(servletRequest, times(1)).getContextPath();
    }

    @Test
    void givenRegistrationService_whenResendTokenWithTokenNotInDatabase_thenResourceNotFoundExceptionThrown() {
        User user = new User();
        user.setId(1L);
        String token = "token";

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(userService.findUserByVerificationToken(token)).thenThrow(new ResourceNotFoundException("Brak użytkownika", "Użytkownik nie istnieje"));

        assertThatThrownBy(() -> registrationService.resendToken(token, servletRequest)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Użytkownik nie istnieje");

        verify(servletRequest, never()).getServerName();
        verify(servletRequest, never()).getServerPort();
        verify(servletRequest, never()).getContextPath();

        verifyNoInteractions(publisher);
        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void givenRegistrationService_whenResendToken_thenResendTokenEventPublished() {
        User user = new User();
        user.setId(1L);
        VerificationToken verificationToken = new VerificationToken("token", user, 15);
        user.setVerificationToken(verificationToken);
        String applicationUrl = "http://localhost:8000/app";

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getServerName()).thenReturn("localhost");
        when(servletRequest.getServerPort()).thenReturn(8000);
        when(servletRequest.getContextPath()).thenReturn("/app");
        when(userService.findUserByVerificationToken(user.getVerificationToken().getToken())).thenReturn(user);

        ArgumentCaptor<String> tokenArgumentCaptor = ArgumentCaptor.forClass(String.class);
        registrationService.resendToken(verificationToken.getToken(), servletRequest);
        verify(userService).findUserByVerificationToken(tokenArgumentCaptor.capture());
        String token = tokenArgumentCaptor.getValue();
        assertThat(token).isEqualTo(user.getVerificationToken().getToken());

        verify(servletRequest, times(1)).getServerName();
        verify(servletRequest, times(1)).getServerPort();
        verify(servletRequest, times(1)).getContextPath();

        ArgumentCaptor<ResendTokenEvent> resendTokenEventArgumentCaptor = ArgumentCaptor.forClass(ResendTokenEvent.class);
        verify(publisher).publishEvent(resendTokenEventArgumentCaptor.capture());
        ResendTokenEvent tokenEvent = resendTokenEventArgumentCaptor.getValue();

        assertThat(tokenEvent.getUser()).isEqualTo(user);
        assertThat(tokenEvent.getApplicationUrl()).isEqualTo(applicationUrl);
        assertThat(tokenEvent.getOldToken()).isEqualTo(verificationToken);
    }

    @Test
    void givenRegistrationService_whenGetRegistrationCompleteMessageThenMessageIsEqualToExpected() {

        String expectedMessage = "Test message 15 minut";

        when(messageSource.getMessage("token.validation.time.message", null, Locale.getDefault())).thenReturn("Test message");
        when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn("15");

        String createdMessage = registrationService.getRegistrationCompleteMessage();
        verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());

        assertThat(createdMessage).isEqualTo(expectedMessage);
    }
}