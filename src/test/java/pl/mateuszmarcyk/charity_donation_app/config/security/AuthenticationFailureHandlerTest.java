package pl.mateuszmarcyk.charity_donation_app.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import pl.mateuszmarcyk.charity_donation_app.ErrorMessages;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationFailureHandlerTest {

    @InjectMocks
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    void givenDisabledException_whenAuthenticationFails_thenRedirectsWithProperErrorMessage() throws IOException, ServletException {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Disabled", new DisabledException("Account disabled")) {};

        // Act & Assert
        assertFailureHandlerRedirects(exception, ErrorMessages.ACCOUNT_DISABLED);
    }

    @Test
    void givenLockedException_whenAuthenticationFails_thenRedirectsWithProperErrorMessage() throws IOException, ServletException {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Locked", new LockedException("Account locked")) {};

        // Act & Assert
        assertFailureHandlerRedirects(exception, ErrorMessages.ACCOUNT_BLOCKED);
    }

    @Test
    void givenInvalidCredentials_whenAuthenticationFails_thenRedirectsWithDefaultErrorMessage() throws IOException, ServletException {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Invalid credentials") {};

        // Act & Assert
        assertFailureHandlerRedirects(exception, ErrorMessages.INVALID_CREDENTIALS);
    }

    private void assertFailureHandlerRedirects(AuthenticationException exception, String expectedRedirect) throws IOException, ServletException {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        authenticationFailureHandler.onAuthenticationFailure(request, response, exception);
        verify(response).sendRedirect(captor.capture());
        assertThat(captor.getValue()).isEqualTo(expectedRedirect);
    }
}