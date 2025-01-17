package pl.mateuszmarcyk.charity_donation_app.config.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class CustomAuthenticationSuccessHandlerTest {

    @InjectMocks
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Mock
    private RoleBasedRedirector roleBasedRedirector;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Test
    @WithMockCustomUser
    void givenUserDetailsForWithUserRole_whenOnAuthenticationSuccessThenRedirectedToCorrectUrl() throws ServletException, IOException {
//        Arrange
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        Act
        customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

//        Assert
        verify(roleBasedRedirector, times(1)).determineRedirectUrl(response, authentication);
    }


}