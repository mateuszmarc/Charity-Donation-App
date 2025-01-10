package pl.mateuszmarcyk.charity_donation_app.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;

import java.util.Locale;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private UserService userService;

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenShowResetPasswordEmailFormThenStatusIsOkAndModelAttributeAdded() throws Exception {

        when(messageSource.getMessage("password.rule", null, Locale.getDefault())).thenReturn("password rule");

        MvcResult mvcResult = mockMvc.perform(get("/reset-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("password-reset-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertThat(modelAndView).isNotNull();

        assertAll(
                () -> assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull(),
                () -> assertThat(modelAndView.getModel().get("email")).isNotNull(),
                () -> assertThat(modelAndView.getModel().get("passwordRule")).isNotNull()
        );
    }

    @Test
    @WithMockCustomUser
    void givenAuthenticatedUser_whenShowResetPasswordEmailForm_thenStatusIsRedirected() throws Exception {

        mockMvc.perform(get("/reset-password"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/error/403"))
                .andReturn();
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenShowChangePasswordForm_thenStatusIsOkAndModelAttributeAdded() throws Exception {
        String token = "token";
        User user = new User();
        user.setId(1L);

        when(messageSource.getMessage("password.rule", null, Locale.getDefault())).thenReturn("password rule");

        when(userService.validatePasswordResetToken(token)).thenReturn(user);

        MvcResult mvcResult = mockMvc.perform(get("/reset-password/verifyEmail").param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("new-password-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertThat(modelAndView).isNotNull();

        verify(messageSource, times(1)).getMessage("password.rule", null, Locale.getDefault());


        assertAll(
                () -> assertThat(modelAndView.getModel().get("passwordRule")).isNotNull(),
                () -> assertThat(modelAndView.getModel().get("user")).isSameAs(user)
        );
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenShowChangePasswordFormAndUserServiceThrowException_thenStatusIsOkAndModelAttributesAdded() throws Exception {
        String token = "token";
        String errorTitle = "Token nie znaleziony";
        String errorMessage = "Link jest uszkodzony";

        when(messageSource.getMessage("password.rule", null, Locale.getDefault())).thenReturn("password rule");

        when(userService.validatePasswordResetToken(token)).thenThrow(new TokenNotFoundException(errorTitle, errorMessage));

        MvcResult mvcResult = mockMvc.perform(get("/reset-password/verifyEmail").param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertThat(modelAndView).isNotNull();

        verify(messageSource, times(1)).getMessage("password.rule", null, Locale.getDefault());
        assertAll(
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(errorTitle),
                () -> assertThat(modelAndView.getModel().get("passwordRule")).isNull()
        );
    }

    @Test
    @WithMockCustomUser
    void givenAuthenticatedUser_whenShowChangePasswordFormThenStatusIsRedirected() throws Exception {
        String token = "token";

        mockMvc.perform(get("/reset-password/verifyEmail").param("token", token))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/error/403"))
                .andReturn();
    }
}