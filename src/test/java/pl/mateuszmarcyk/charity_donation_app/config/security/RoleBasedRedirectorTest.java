package pl.mateuszmarcyk.charity_donation_app.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.mateuszmarcyk.charity_donation_app.ErrorMessages;
import pl.mateuszmarcyk.charity_donation_app.UrlTemplates;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@SpringBootTest
class RoleBasedRedirectorTest {
    
    @InjectMocks
    private RoleBasedRedirector roleBasedRedirector;
    
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    @WithMockCustomUser
    void givenUserDetailsForWithUserRole_whenOnAuthenticationSuccessThenRedirectedToCorrectUrl() throws IOException {
//        Arrange
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        Act & Assert
        assertRedirectionUrl(authentication, UrlTemplates.APPLICATION_URL);
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN", "ROLE_USER"})
    void givenUserDetailsForUserWithUserAndAdminRole_whenOnAuthenticationSuccessThenRedirectedToCorrectUrl() throws IOException {
//        Arrange
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        Act & Assert
        assertRedirectionUrl(authentication, UrlTemplates.APPLICATION_URL);

    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserDetailsForUserWithAdminRole_whenOnAuthenticationSuccessThenRedirectedToCorrectUrl() throws IOException {
//        Arrange
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //        Act & Assert
        assertRedirectionUrl(authentication, UrlTemplates.ADMIN_DASHBOARD_URL);
    }

    @Test
    void givenUserDetailsWithNoRole_whenOnAuthenticationSuccessThenRedirectedToCorrectUrl() {
//        Arrange
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails customUserDetails = new CustomUserDetails(new User());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);

//        Act & Assert
        Throwable thrown = catchThrowable(() -> roleBasedRedirector.determineRedirectUrl(request, response, authentication));
        assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage(ErrorMessages.ILLEGAL_STATE_EXCEPTION_MESSAGE);
    }

    private void assertRedirectionUrl(Authentication authentication, String expectedRedirectUrl) throws IOException {
        roleBasedRedirector.determineRedirectUrl(request, response, authentication);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(stringArgumentCaptor.capture());
        String capturedUrl = stringArgumentCaptor.getValue();
        assertThat(capturedUrl).isEqualTo(expectedRedirectUrl);
    }
}