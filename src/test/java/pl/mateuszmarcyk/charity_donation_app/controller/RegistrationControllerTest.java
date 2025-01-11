package pl.mateuszmarcyk.charity_donation_app.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyExpiredException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.service.RegistrationService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @WithAnonymousUser
    void whenProcessRegistrationFormAndUserValid_thenStatusIsOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/register";
        String expectedViewName = "register-confirmation";
        User userToRegister = getUser();
        String registrationCompleteMessage = "Registration complete";

        when(registrationService.getRegistrationCompleteMessage()).thenReturn(registrationCompleteMessage);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                .flashAttr("user", userToRegister))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(registrationService, times(1)).registerUser(userArgumentCaptor.capture(), any(HttpServletRequest.class));
        User captureduser = userArgumentCaptor.getValue();
        assertThat(captureduser).isEqualTo(userToRegister);

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getModel().get("registrationMessage")).isEqualTo(registrationCompleteMessage);
    }

    @Test
    @WithAnonymousUser
    void whenProcessRegistrationFormAndUseIsInvalid_thenStatusIsOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/register";
        String expectedViewName = "register-form";
        User userToRegister = getUser();
        userToRegister.setPassword(null);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("user", userToRegister))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("user", "password"))
                .andReturn();

        verify(registrationService, never()).registerUser(any(User.class), any(HttpServletRequest.class));
        verify(registrationService, never()).getRegistrationCompleteMessage();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getModel().get("registrationMessage")).isNull();
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

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenVerifyUserAndTokenAlreadyExpiredExceptionOccurs_thenStatusIsOkAndModelAttributeAdded() throws Exception {
        String exceptionTitle = "exception title";
        String exceptionMessage = "Exception message";
        String token = "token";

        doAnswer(invocation -> {
            throw new TokenAlreadyExpiredException(exceptionTitle, exceptionMessage,  token);
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
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(exceptionMessage),
                () -> assertThat(modelAndView.getModel().get("token")).isEqualTo(token)
        );
    }

    @Test
    @WithAnonymousUser
    void whenResendToken_thenStatusIsOKAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/register/resendToken";
        String expectedViewName = "register-confirmation";
        String oldToken = "OldToken";
        String registrationCompleteMessage = "Registration complete";
        when(registrationService.getRegistrationCompleteMessage()).thenReturn(registrationCompleteMessage);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                .param("token", oldToken))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(registrationService, times(1)).resendToken(stringArgumentCaptor.capture(), any(HttpServletRequest.class));
        String capturedToken = stringArgumentCaptor.getValue();
        assertThat(capturedToken).isEqualTo(oldToken);

        verify(registrationService, times(1)).getRegistrationCompleteMessage();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();
        assertThat(modelAndView.getModel().get("registrationMessage")).isEqualTo(registrationCompleteMessage);
    }

    @Test
    @WithAnonymousUser
    void whenResendTokenAndExceptionIsThrown_thenStatusIsOKAndErrorViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/register/resendToken";
        String expectedViewName = "error-page";
        String oldToken = "OldToken";
        String registrationCompleteMessage = "Registration complete";
        String exceptionTitle = "Title";
        String exceptionMessage = "Message";
        when(registrationService.getRegistrationCompleteMessage()).thenReturn(registrationCompleteMessage);

       doAnswer(invocationOnMock -> {
           throw new ResourceNotFoundException(exceptionTitle, exceptionMessage);
       }).when(registrationService).resendToken(any(String.class), any(HttpServletRequest.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("token", oldToken))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(registrationService, times(1)).resendToken(stringArgumentCaptor.capture(), any(HttpServletRequest.class));
        String capturedToken = stringArgumentCaptor.getValue();
        assertThat(capturedToken).isEqualTo(oldToken);

        verify(registrationService, never()).getRegistrationCompleteMessage();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertAll(
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(exceptionTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(exceptionMessage)
        );
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