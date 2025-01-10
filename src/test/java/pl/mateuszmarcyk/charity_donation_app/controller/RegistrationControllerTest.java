package pl.mateuszmarcyk.charity_donation_app.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.service.RegistrationService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        when(messageSource.getMessage("password.rule", null, Locale.getDefault())).thenReturn("Password test rule");
        when(messageSource.getMessage("token.resend.title", null, Locale.getDefault())).thenReturn("Resend token title");
        when(messageSource.getMessage("registration.confirmation.title", null, Locale.getDefault())).thenReturn("Registration confirmation title");
        when(messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault())).thenReturn("Token not found title");
        when(messageSource.getMessage("token.validation.message", null, Locale.getDefault())).thenReturn("Test validation message");
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenShowRegisterForm_thenStatusIsOkAndModelAttributeAdded() throws Exception {
        String passwordRule = "Password test rule";
//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();
        assertAll(
                () -> assertThat(messageSource.getMessage("password.rule", null, Locale.getDefault())).isEqualTo(passwordRule),
                () -> assertThat(modelAndView.getModel().get("passwordRule")).isEqualTo(passwordRule),
                () -> assertThat(modelAndView.getModel().get("user")).isInstanceOf(User.class)
        );
    }

    @Test
    @WithMockCustomUser
    void givenAuthenticatedUser_whenShowRegisterForm_thenStatusIsRedirected() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/error/403"))
                .andReturn();
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenVerifyUser_thenStatusIsOkAndModelAttributeAdded() throws Exception {
        String token = "token";

        doAnswer(invocation -> null).when(userService).validateToken(token);

        MvcResult mvcResult = mockMvc.perform(get("/register/verifyEmail").param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("validation-complete"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertThat(modelAndView).isNotNull();

        verify(messageSource, times(1)).getMessage("password.rule", null, Locale.getDefault());


        assertAll(
                () -> assertThat(modelAndView.getModel().get("validationTitle")).isEqualTo("Token not found title"),
                () -> assertThat(modelAndView.getModel().get("validationMessage")).isEqualTo("Test validation message")
        );
    }

    @Test
    @WithMockCustomUser
    void givenAuthenticatedUser_whenVerifyUser_thenStatusIsRedirected() throws Exception {
        String token = "token";

        mockMvc.perform(get("/register/verifyEmail").param("token", token))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/error/403"))
                .andReturn();

    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenVerifyUserAndExceptionOccurs_thenStatusIsOkAndModelAttributeAdded() throws Exception {
        String exceptionTitle = "exception title";
        String exceptionMessage = "Exception message";
        String token = "token";

        doAnswer(invocation -> {
            throw new TokenNotFoundException(exceptionTitle, exceptionMessage);
        }).when(userService).validateToken(token);


        MvcResult mvcResult = mockMvc.perform(get("/register/verifyEmail").param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertThat(modelAndView).isNotNull();

        verify(messageSource, times(1)).getMessage("password.rule", null, Locale.getDefault());


        assertAll(
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(exceptionTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(exceptionMessage)
        );
    }


}