package pl.mateuszmarcyk.charity_donation_app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.test.web.servlet.MockMvc;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoginLogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private  MessageSource messageSource;

    @MockBean
    private  UserService userService;

    @MockBean
    SecurityContextLogoutHandler logoutHandler;


    @Test
    @WithAnonymousUser
    void givenUnauthenticatedUser_whenLogin_thenStatusIsOkAndLoginViewDisplayed() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void givenUnauthenticatedUser_whenLogout_thenStatusIsRedirected() throws Exception {
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @WithMockCustomUser
    void givenAuthenticatedUser_whenLogout_thenUserIsLoggedOutAndStatusIsRedirected() throws Exception {
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        mockMvc.perform(get("/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}