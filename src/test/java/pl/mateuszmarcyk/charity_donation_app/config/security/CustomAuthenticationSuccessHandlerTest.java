package pl.mateuszmarcyk.charity_donation_app.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.io.IOException;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@SpringBootTest
class CustomAuthenticationSuccessHandlerTest {

    @InjectMocks
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    @WithMockCustomUser
    void givenUserDetailsForWithUserRole_whenOnAuthenticationSuccessThenRedirectedToCorrectUrl() throws ServletException, IOException {
//        Arrange
        String expectedRedirectUrl = "/app";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(stringArgumentCaptor.capture());
        String capturedUrl = stringArgumentCaptor.getValue();
        assertThat(capturedUrl).isEqualTo(expectedRedirectUrl);
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN", "ROLE_USER"})
    void givenUserDetailsForUserWithUserAndAdminRole_whenOnAuthenticationSuccessThenRedirectedToCorrectUrl() throws ServletException, IOException {
//        Arrange
        String expectedRedirectUrl = "/app";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(stringArgumentCaptor.capture());
        String capturedUrl = stringArgumentCaptor.getValue();
        assertThat(capturedUrl).isEqualTo(expectedRedirectUrl);
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserDetailsForUserWithAdminRole_whenOnAuthenticationSuccessThenRedirectedToCorrectUrl() throws ServletException, IOException {
//        Arrange
        String expectedRedirectUrl = "/app/admins/dashboard";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(1)).sendRedirect(stringArgumentCaptor.capture());
        String capturedUrl = stringArgumentCaptor.getValue();
        assertThat(capturedUrl).isEqualTo(expectedRedirectUrl);
    }

    @Test
    void givenUserDetailsWithNoRole_whenOnAuthenticationSuccessThenRedirectedToCorrectUrl() throws ServletException, IOException {
//        Arrange
        String exceptionMessage= "Authenticated user has no roles assigned.";
        Authentication authentication = mock(Authentication.class);
        User user = new User();
        user.setEmail("email@gmail.com");
        user.setUserTypes(new HashSet<>());
        CustomUserDetails customUserDetails = new CustomUserDetails(new User());
        when(authentication.getPrincipal()).thenReturn(customUserDetails);

       Throwable thrown = catchThrowable(() -> customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication));
       assertThat(thrown).isInstanceOf(IllegalStateException.class).hasMessage(exceptionMessage);

    }
}