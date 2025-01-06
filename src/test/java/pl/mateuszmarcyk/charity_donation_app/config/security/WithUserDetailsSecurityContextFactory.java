package pl.mateuszmarcyk.charity_donation_app.config.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

final class WithUserDetailsSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserProfile profile = new UserProfile();
        profile.setFirstName("Mateusz");
        profile.setLastName("Marcykiewicz");
        profile.setCity("Kielce");
        profile.setCountry("Poland");
        profile.setPhoneNumber("555666777");

        User user = new User();
        user.setEmail(customUser.email());
        user.setPassword("password"); // Not relevant for tests
        user.setEnabled(customUser.enabled());
        user.setBlocked(customUser.blocked());
        user.setProfile(profile);

        // Assign roles
        Set<UserType> roles = Arrays.stream(customUser.roles()).map(role -> {
            UserType userType = new UserType();
            userType.setRole(role);
            return userType;
        }).collect(Collectors.toSet());

        user.setUserTypes(roles);

        CustomUserDetails principal =
                new CustomUserDetails(user);

        Authentication auth =
                UsernamePasswordAuthenticationToken.authenticated(principal, "password", principal.getAuthorities());

        context.setAuthentication(auth);
        return context;
    }
}
