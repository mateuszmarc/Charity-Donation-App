package pl.mateuszmarcyk.charity_donation_app.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthenticationFailureHandler implements org.springframework.security.web.authentication.AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String errorMessage ="Invalid username or password.";

        Throwable cause = exception.getCause();
        if (cause instanceof DisabledException) {
            errorMessage = "Your account is not enabled.";
        } else if (cause instanceof LockedException) {
            errorMessage = "Your account is blocked.";
        }

        response.sendRedirect("/app/login?error=" + errorMessage);
    }
}
