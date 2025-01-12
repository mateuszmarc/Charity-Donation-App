package pl.mateuszmarcyk.charity_donation_app.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedRedirector {

    public void determineRedirectUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        boolean hasUserRoleAuthority = authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_USER"));

        boolean hasAdminRoleAuthority = authentication.getAuthorities().stream().anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (hasUserRoleAuthority) {
            response.sendRedirect("/app");
        } else if (hasAdminRoleAuthority) {
            response.sendRedirect("/app/admins/dashboard");
        } else {
            throw new IllegalStateException("Authenticated user has no roles assigned.");
        }
    }
}
