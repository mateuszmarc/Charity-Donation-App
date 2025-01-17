package pl.mateuszmarcyk.charity_donation_app.controller;

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
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.UrlTemplates;
import pl.mateuszmarcyk.charity_donation_app.ViewNames;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.Email;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    private void mockPasswordRuleMessage(String message) {
        when(messageSource.getMessage("password.rule", null, Locale.getDefault())).thenReturn(message);
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenShowResetPasswordEmailForm_thenStatusIsOkAndModelAttributeAdded() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.RESET_PASSWORD_URL;
        String expectedViewName = ViewNames.PASSWORD_RESET_FORM_VIEW;
        String passwordRule = "password rule";

        when(messageSource.getMessage("password.rule", null, Locale.getDefault())).thenReturn(passwordRule);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();

        // Assert
        assertThat(modelAndView).isNotNull();

        assertAll(
                () -> assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull(),
                () -> assertThat(modelAndView.getModel().get("email")).isNotNull(),
                () -> assertThat(modelAndView.getModel()).containsEntry("passwordRule", passwordRule),
                () -> verify(messageSource, times(1)).getMessage("password.rule", null, Locale.getDefault())

        );
    }

    @Test
    @WithMockCustomUser
    void givenAuthenticatedUser_whenShowResetPasswordEmailForm_thenStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.RESET_PASSWORD_URL;
        String expectedForwardedUrl = UrlTemplates.ACCESS_DENIED_URL;
        // Act & Assert
        mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(expectedForwardedUrl))
                .andReturn();
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenShowChangePasswordForm_thenStatusIsOkAndModelAttributeAdded() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.RESET_PASSWORD_VERIFY_EMAIL_URL;
        String token = "token";
        String expectedViewName = ViewNames.NEW_PASSWORD_FORM_VIEW ;
        String passwordRuleMessage = "password rule";
        User expectedUser = createTestUser(1L);

        mockPasswordRuleMessage(passwordRuleMessage);
        when(userService.validatePasswordResetToken(token)).thenReturn(expectedUser);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate).param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        // Assert
        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertAll(
                () -> verify(messageSource, times(1)).getMessage("password.rule", null, Locale.getDefault()),
                () -> assertThat(modelAndView.getModel()).containsEntry("passwordRule", passwordRuleMessage),
                () -> assertThat(modelAndView.getModel().get("user")).isSameAs(expectedUser)
        );
    }

    private User createTestUser(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenShowChangePasswordFormAndUserServiceThrowsException_thenStatusIsOkAndModelAttributesAdded() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.RESET_PASSWORD_VERIFY_EMAIL_URL;
        String token = "token";
        String expectedViewName = ViewNames.ERROR_PAGE_VIEW;
        String passwordRuleMessage = "password rule";
        String errorTitle = "Token nie znaleziony";
        String errorMessage = "Link jest uszkodzony";

        mockPasswordRuleMessage(passwordRuleMessage);
        mockTokenValidationException(token, errorTitle, errorMessage);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate).param("token", token))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        // Assert
        verifyChangePasswordFormErrorAssertions(mvcResult, errorTitle, passwordRuleMessage);
    }

    private void mockTokenValidationException(String token, String errorTitle, String errorMessage) {
        when(userService.validatePasswordResetToken(token)).thenThrow(new TokenNotFoundException(errorTitle, errorMessage));
    }

    private void verifyChangePasswordFormErrorAssertions(MvcResult mvcResult, String expectedErrorTitle, String expectedPasswordRule) {
        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(messageSource, times(1)).getMessage("password.rule", null, Locale.getDefault());

        assertAll(
                () -> assertThat(modelAndView.getModel()).containsEntry("errorTitle", expectedErrorTitle),
                () -> assertThat(modelAndView.getModel()).doesNotContainEntry("passwordRule", expectedPasswordRule)
        );
    }

    @Test
    @WithMockCustomUser
    void givenAuthenticatedUser_whenShowChangePasswordForm_thenStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.RESET_PASSWORD_VERIFY_EMAIL_URL;
        String token = "token";
        String expectedForwardedUrl = UrlTemplates.ACCESS_DENIED_URL;

        // Act & Assert
        mockMvc.perform(get(urlTemplate).param("token", token))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(expectedForwardedUrl))
                .andReturn();
    }

    @Test
    @WithAnonymousUser
    void whenProcessResetPasswordFormAndEmailIsValid_thenStatusIsOkAndAttributesAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.RESET_PASSWORD_URL;
        String expectedViewName = ViewNames.REGISTER_CONFIRMATION_VIEW;
        String passwordResendTitle = "Password resend title";
        String passwordResendMessage = "Password resend message";
        String tokenValidPeriod = "5";
        String expectedPasswordResendMessage = passwordResendMessage + " " + tokenValidPeriod + " minut";
        String email = "test@gmail.com";
        User user = new User();

        mockPasswordResendMessages(passwordResendTitle, passwordResendMessage, tokenValidPeriod);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        // Assert
        verifyPasswordResendFlow(email, passwordResendTitle, expectedPasswordResendMessage, mvcResult);
    }

    private void mockPasswordResendMessages(String title, String message, String tokenValidPeriod) {
        when(messageSource.getMessage("passwordresend.title", null, Locale.getDefault())).thenReturn(title);
        when(messageSource.getMessage("passwordresend.message", null, Locale.getDefault())).thenReturn(message);
        when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn(tokenValidPeriod);
    }

    private void verifyPasswordResendFlow(String email, String expectedTitle, String expectedMessage, MvcResult mvcResult) {
        assertThat(mvcResult).isNotNull();

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
                () -> assertThat(modelAndView.getModel()).containsEntry("registrationCompleteTitle", expectedTitle),
                () -> assertThat(modelAndView.getModel()).containsEntry("registrationMessage", expectedMessage)
        );
    }

    @Test
    @WithAnonymousUser
    void whenProcessResetPasswordFormAndEmailIsInvalid_thenStatusIsOkAndAttributesAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.RESET_PASSWORD_URL;
        String expectedViewName = ViewNames.PASSWORD_RESET_FORM_VIEW;
        Email email = new Email();

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("email", email))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("email", "addressEmail"))
                .andReturn();

        // Assert
        assertInvalidEmailFlow(email, mvcResult, expectedViewName);
    }

    private void assertInvalidEmailFlow(Email email, MvcResult mvcResult, String expectedViewName) {
        assertThat(mvcResult).isNotNull();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(userService, never()).resetPassword(any(), any());

        verify(messageSource, never()).getMessage("passwordresend.title", null, Locale.getDefault());
        verify(messageSource, never()).getMessage("passwordresend.message", null, Locale.getDefault());
        verify(messageSource, never()).getMessage("token.valid.time", null, Locale.getDefault());

        assertAll(
                () -> assertThat(modelAndView.getViewName()).isEqualTo(expectedViewName),
                () -> assertThat(modelAndView.getModel()).containsEntry("email", email)
        );
    }

    @Test
    @WithAnonymousUser
    void whenProcessChangePasswordFormAndPasswordIsValid_thenStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.NEW_PASSWORD_URL;
        String expectedRedirectUrl = UrlTemplates.LOGIN_URL;
        User user = TestDataFactory.getUser();

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("user", user))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl))
                .andReturn();

        // Assert
        assertPasswordChangeFlow(user, mvcResult, expectedRedirectUrl);
    }

    private void assertPasswordChangeFlow(User expectedUser, MvcResult mvcResult, String expectedRedirectUrl) {
        assertThat(mvcResult).isNotNull();

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).changePassword(userArgumentCaptor.capture());

        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(expectedUser);

        assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl);
    }

    @Test
    @WithAnonymousUser
    void whenProcesChangePasswordFormAndPasswordIsInValid_thenStatusIsOkAndAttributesAddedToModel() throws Exception {
//        Arrange
        String urlTemplate = UrlTemplates.NEW_PASSWORD_URL;
        String expectedViewName = ViewNames.NEW_PASSWORD_FORM_VIEW ;
        User user = TestDataFactory.getUser();
        user.setPassword("");

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));

        verify(userService, never()).changePassword(any(User.class));
    }


}