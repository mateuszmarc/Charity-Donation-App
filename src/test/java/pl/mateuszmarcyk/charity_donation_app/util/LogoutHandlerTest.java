package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutHandlerTest {

    @InjectMocks
    private LogoutHandler logoutHandler;

    @Mock
    private SecurityContextLogoutHandler securityContextLogoutHandler;

    @Test
    void whenChangeEmailInUserDetails_thenAuthenticationIsUpdated() {
        // Arrange
        User oldUser = TestDataFactory.getUser();
        User updatedUser = TestDataFactory.getUser();
        updatedUser.setEmail("newEmail@gmail.com");
        CustomUserDetails userDetails = new CustomUserDetails(updatedUser);

        // Set an initial authentication object in the SecurityContext
        Authentication originalAuth = new UsernamePasswordAuthenticationToken(
                new CustomUserDetails(oldUser), "originalPassword", oldUser.getUserTypes().stream().map(userType -> new SimpleGrantedAuthority(userType.getRole())).toList());

        SecurityContextHolder.getContext().setAuthentication(originalAuth);

        // Act
        logoutHandler.changeEmailInUserDetails(updatedUser);

        // Assert
        Authentication newAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(newAuth).isNotNull();
        assertThat(newAuth.getPrincipal()).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails updatedUserDetails = (CustomUserDetails) newAuth.getPrincipal();
        assertThat(updatedUserDetails.getUsername()).isEqualTo(updatedUser.getEmail());
        assertThat(updatedUserDetails.getPassword()).isEqualTo(updatedUser.getPassword());
        assertIterableEquals(userDetails.getAuthorities(), updatedUserDetails.getAuthorities());
    }

    @Test
    void whenPerformLogout_thenLogoutPerformed() {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        Authentication mockAuthentication = mock(Authentication.class);

        logoutHandler.performLogout(mockRequest, mockResponse, mockAuthentication);

        verify(securityContextLogoutHandler, times(1)).logout(mockRequest, mockResponse, mockAuthentication);
    }

}