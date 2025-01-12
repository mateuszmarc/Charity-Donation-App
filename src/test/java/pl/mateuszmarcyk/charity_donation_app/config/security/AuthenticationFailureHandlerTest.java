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
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        // Act
        authenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(captor.capture());
        assertThat(captor.getValue()).isEqualTo("/app/login?error=Your account is not enabled.");
    }

    @Test
    void givenLockedException_whenAuthenticationFails_thenRedirectsWithProperErrorMessage() throws IOException, ServletException {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Locked", new LockedException("Account locked")) {};
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        // Act
        authenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(captor.capture());
        assertThat(captor.getValue()).isEqualTo("/app/login?error=Your account is blocked.");
    }

    @Test
    void givenInvalidCredentials_whenAuthenticationFails_thenRedirectsWithDefaultErrorMessage() throws IOException, ServletException {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Invalid credentials") {};
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        // Act
        authenticationFailureHandler.onAuthenticationFailure(request, response, exception);

        // Assert
        verify(response).sendRedirect(captor.capture());
        assertThat(captor.getValue()).isEqualTo("/app/login?error=Invalid username or password.");
    }
}