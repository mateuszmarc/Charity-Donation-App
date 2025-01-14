package pl.mateuszmarcyk.charity_donation_app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.mateuszmarcyk.charity_donation_app.UrlTemplates;
import pl.mateuszmarcyk.charity_donation_app.ViewNames;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoginLogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithAnonymousUser
    void givenUnauthenticatedUser_whenLogin_thenStatusIsOkAndLoginViewDisplayed() throws Exception {
//        Arrange
        String urlTemplate = UrlTemplates.LOGIN_URL;
        String expectedViewName = ViewNames.LOGIN_VIEW;

        mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));
    }

    @Test
    void givenUnauthenticatedUser_whenLogout_thenStatusIsRedirected() throws Exception {
        //        Arrange
        String urlTemplate = UrlTemplates.LOGOUT_URL;
        String expectedRedirectUrl = UrlTemplates.HOME_URL;
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

//        Act & Assert
        mockMvc.perform(get(urlTemplate))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    @WithMockCustomUser
    void givenAuthenticatedUser_whenLogout_thenUserIsLoggedOutAndStatusIsRedirected() throws Exception {
        //        Arrange
        String urlTemplate = UrlTemplates.LOGOUT_URL;
        String expectedRedirectUrl = UrlTemplates.HOME_URL;
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

//        Act & Assert
        mockMvc.perform(get(urlTemplate))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}