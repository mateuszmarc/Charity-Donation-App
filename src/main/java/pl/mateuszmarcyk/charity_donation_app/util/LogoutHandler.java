package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

@Component
public class LogoutHandler {
    SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();

    public void performLogout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        securityContextLogoutHandler.logout(request, response, authentication);
    }

    public void changeEmailInUserDetails(User updatedUser) {
        CustomUserDetails userDetails = new CustomUserDetails(updatedUser);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}
