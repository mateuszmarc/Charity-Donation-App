package pl.mateuszmarcyk.charity_donation_app.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.Email;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @MockBean
    private UserRepository userRepository;

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

    @Test
    @WithAnonymousUser
    void whenProcessResetPasswordFormAndEmailIsValid_thenStatusIsOkAndAttributesAddedToModel() throws Exception {
//        Arrange
        String urlTemplate = "/reset-password";
        String expectedViewName = "register-confirmation";
        String passwordResendTitle = "Password resend title";
        String passwordResendMessage = "Password resend message";
        String tokenValidPeriod = "5";
        String expectedPasswordResendMessage = passwordResendMessage + " " + tokenValidPeriod + " minut";
        String email = "test@gmail.com";
        User user = new User();

        when(messageSource.getMessage("passwordresend.title", null, Locale.getDefault())).thenReturn(passwordResendTitle);
        when(messageSource.getMessage("passwordresend.message", null, Locale.getDefault())).thenReturn(passwordResendMessage);
        when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn(tokenValidPeriod);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                .flashAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        ArgumentCaptor<Email> emailArgumentCaptor = ArgumentCaptor.forClass(Email.class);
        verify(userService, times(1)).resetPassword(emailArgumentCaptor.capture(), any());
        Email capturedEmail = emailArgumentCaptor.getValue();
        assertThat(capturedEmail.getAddressEmail()).isSameAs(email);

        verify(messageSource, times(1)).getMessage("passwordresend.title", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("passwordresend.message", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
        assertAll(
                () -> assertThat(modelAndView.getModel().get("registrationCompleteTitle")).isEqualTo(passwordResendTitle),
                () -> assertThat(modelAndView.getModel().get("registrationMessage")).isEqualTo(expectedPasswordResendMessage)
        );
    }

    @Test
    @WithAnonymousUser
    void whenProcessResetPasswordFormAndEmailIsInvalid_thenStatusIsOkAndAttributesAddedToModel() throws Exception {
//        Arrange
        String urlTemplate = "/reset-password";
        String expectedViewName = "password-reset-form";
        Email email = new Email();

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("email", "addressEmail"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(userService, never()).resetPassword(any(), any());

        verify(messageSource, never()).getMessage("passwordresend.title", null, Locale.getDefault());
        verify(messageSource, never()).getMessage("passwordresend.message", null, Locale.getDefault());
        verify(messageSource, never()).getMessage("token.valid.time", null, Locale.getDefault());
    }

    @Test
    @WithAnonymousUser
    void whenProcesChangePasswordFormAndPasswordIsValid_thenStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/new-password";
        String expectedRedirectUrl = "/login";
        User user = getUser();

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).changePassword(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(user);
    }

    @Test
    @WithAnonymousUser
    void whenProcesChangePasswordFormAndPasswordIsInValid_thenStatusIsOkAndAttributesAddedToModel() throws Exception {
//        Arrange
        String urlTemplate = "/new-password";
        String expectedViewName = "new-password-form";
        User user = getUser();
        user.setPassword("");

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));

        verify(userService, never()).changePassword(any(User.class));
    }

    private static User getUser() {
        UserProfile userProfile = new UserProfile(2L, null, "Mateusz", "Marcykiewicz", "Kielce",
                "Poland", null, "555666777");
        UserType userType = new UserType(2L, "ROLE_USER", new ArrayList<>());
        User user = new User(
                1L,
                "test@email.com",
                true,
                false,
                "testPW123!!",
                LocalDateTime.of(2023, 11, 11, 12, 25, 11),
                "testPW123!!",
                new HashSet<>(Set.of(userType)),
                userProfile,
                null,
                null,
                new ArrayList<>()
        );

        userProfile.setUser(user);
        return user;
    }
}