package pl.mateuszmarcyk.charity_donation_app.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.GlobalTestMethodVerifier;
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.UrlTemplates;
import pl.mateuszmarcyk.charity_donation_app.ViewNames;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.FileUploadUtil;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;
import pl.mateuszmarcyk.charity_donation_app.util.LogoutHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private  UserService userService;

    @MockBean
    private  DonationService donationService;

    @MockBean
    private  FileUploadUtil fileUploadUtil;

    @MockBean
    private  LoggedUserModelHandler loggedUserModelHandler;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LogoutHandler logoutHandler;

    private User loggedInUser;

    @BeforeEach
    void setUp() {
        loggedInUser = TestDataFactory.getUser();
        TestDataFactory.stubLoggedUserModelHandlerMethodsInvocation(loggedUserModelHandler, loggedInUser);
    }

    private void assertUserAndProfileInModel(ModelAndView modelAndView, User user) {
        assertAll(
                () -> assertThat(modelAndView.getModel().get("user")).isSameAs(user),
                () -> assertThat(modelAndView.getModel().get("userProfile")).isSameAs(user.getProfile())
        );
    }

    @Test
    @WithMockCustomUser
    void whenShowUserDetails_thenStatusIsOkAndModelAttributesAdded() throws Exception {
//        Arrange
        String urlTemplate = UrlTemplates.USER_PROFILE_DETAILS_URL;
        String expectedView = ViewNames.USER_PROFILE_VIEW;

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedView))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();
        GlobalTestMethodVerifier.verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler);
        assertUserAndProfileInModel(modelAndView, loggedInUser);
    }

    @Test
    @WithMockCustomUser
    void whenShowUserProfileEditForm_thenStatusIsOkAndModelAttributesAdded() throws Exception {
        //        Arrange
        String urlTemplate = UrlTemplates.USER_PROFILE_EDIT_FORM_URL;
        String expectedView = ViewNames.USER_PROFILE_EDIT_VIEW;

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedView))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        GlobalTestMethodVerifier.verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler);
        assertUserAndProfileInModel(modelAndView, loggedInUser);
    }

    @Test
    @WithMockCustomUser
    void whenProcessUserProfileEditForm_thenStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = UrlTemplates.USER_PROFILE_EDIT_FORM_URL;
        String expectedRedirectUrl = UrlTemplates.USER_PROFILE_DETAILS_URL;
        
        MockMultipartFile multipartFile = new MockMultipartFile("image", new byte[0]);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);

//        Act & Assert
        mockMvc.perform(multipart(urlTemplate)
                        .file(multipartFile)
                        .param("id", "2")
                        .flashAttr("userProfile", loggedInUser.getProfile())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl))
                .andReturn();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<UserProfile> userProfileArgumentCaptor = ArgumentCaptor.forClass(UserProfile.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        verify(fileUploadUtil, times(1)).saveImage(userProfileArgumentCaptor.capture(), any(MultipartFile.class), userArgumentCaptor.capture());
        UserProfile capturedProfile = userProfileArgumentCaptor.getValue();
        assertThat(capturedProfile).isSameAs(loggedInUser.getProfile());

        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(loggedInUser);
    }

    @Test
    @WithMockCustomUser
    void whenShowUserAccountEditForm_thenStatusIsOkAndModelAttributesAdded() throws Exception {
        //        Arrange
        String urlTemplate = UrlTemplates.USER_ACCOUNT_EDIT_FORM_URL;
        String expectedView = ViewNames.USER_ACCOUNT_EDIT_VIEW;
        loggedInUser.setPasswordRepeat(null);


//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedView))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        GlobalTestMethodVerifier.verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler);
        
        assertThat(loggedInUser.getPasswordRepeat()).isEqualTo(loggedInUser.getPassword());
        assertUserAndProfileInModel(modelAndView, loggedInUser);
    }

    @Test
    @WithMockCustomUser
    void thenProcessUserChangePasswordFormAndPasswordValid_thenStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = UrlTemplates.USER_ACCOUNT_CHANGE_PASSWORD_URL;
        String expectedRedirectUrl = UrlTemplates.USER_PROFILE_DETAILS_URL;

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("user", loggedInUser)
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        GlobalTestMethodVerifier.verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler);
        
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).changePassword(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(loggedInUser);
    }

    @Test
    @WithMockCustomUser
    void thenProcessUserChangePasswordFormAndPasswordIsInvalid_thenStatusIsOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = UrlTemplates.USER_ACCOUNT_CHANGE_PASSWORD_URL;
        String expectedViewName = ViewNames.USER_ACCOUNT_EDIT_VIEW;
        loggedInUser.setPassword(null);
        
//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("user", loggedInUser)
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("user", "password"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        GlobalTestMethodVerifier.verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler);
        
        assertUserAndProfileInModel(modelAndView, loggedInUser);
        verify(userService, never()).changePassword(any(User.class));
    }

    @Test
    @WithMockCustomUser
    void whenProcessChangeEmailFormAndEmailValid_thenUserLoggedOutAndStatusRedirected() throws Exception {
        String utlTemplate = UrlTemplates.USER_ACCOUNT_CHANGE_EMAIL_URL;
        String expectedRedirectUrl = UrlTemplates.HOME_URL;
        
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        
        when(userRepository.findByEmail(loggedInUser.getEmail())).thenReturn(Optional.of(loggedInUser));

//        Act & Assert
        mockMvc.perform(post(utlTemplate)
                .param("id", "1")
                .flashAttr("user", loggedInUser))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl))
                .andReturn();

        GlobalTestMethodVerifier.verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler);
        
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).changeEmail(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(loggedInUser);
        verify(logoutHandler, times(1)).performLogout(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Authentication.class));
    }

    @Test
    @WithMockCustomUser
    void whenProcessChangeEmailFormAndEmailIsInvalid_thenStatusIsOkAndUser() throws Exception {
        String utlTemplate = UrlTemplates.USER_ACCOUNT_CHANGE_EMAIL_URL;
        String expectedViewName = ViewNames.USER_ACCOUNT_EDIT_VIEW;
        
        loggedInUser.setEmail(null);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        
//        Act & Assert
        mockMvc.perform(post(utlTemplate)
                        .param("id", "1")
                        .flashAttr("user", loggedInUser))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("user", "email"))
                .andReturn();

        GlobalTestMethodVerifier.verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler);
        
        verify(userService, never()).changeEmail(any(User.class));
        verify(logoutHandler, never()).performLogout(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Authentication.class));
    }


    @Test
    @WithMockCustomUser
    void whenArchiveDonation_thenDonationServiceInvokedStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = UrlTemplates.USER_DONATIONS_ARCHIVE_URL;
        String expectedRedirectUrl = UrlTemplates.USER_DONATIONS_URL;
        Long donationId = 1L;
        
        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                .param("donationId", donationId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        verify(donationService, times(1)).archiveUserDonation(longArgumentCaptor.capture(), userArgumentCaptor.capture());
        Long capturedLong = longArgumentCaptor.getValue();
        assertThat(capturedLong).isEqualTo(donationId);

        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isEqualTo(loggedInUser);
    }

    @Test
    @WithMockCustomUser
    void whenDeleteYourself_thenUserServiceInvokedStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = UrlTemplates.USER_ACCOUNT_DELETE_URL;
        String expectedRedirectUrl = UrlTemplates.HOME_URL;
        
        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);

//        Act & Assert
        mockMvc.perform(post(urlTemplate))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).deleteUser(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(loggedInUser.getId());

        verify(logoutHandler,   times(1)).performLogout(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Authentication.class));
    }

    @Test
    @WithMockCustomUser
    void whenDowngradeYourself_thenUserServiceInvokedStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = UrlTemplates.USER_ACCOUNT_DOWNGRADE_URL;
        String expectedRedirectUrl = UrlTemplates.HOME_URL;
        
        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);

//        Act & Assert
        mockMvc.perform(post(urlTemplate))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).removeAdminRole(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(loggedInUser.getId());

        verify(logoutHandler,   times(1)).performLogout(any(HttpServletRequest.class), any(HttpServletResponse.class), any(Authentication.class));
    }

    @Test
    @WithMockCustomUser
    void whenShowAllDonations_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        String utlTemplate = UrlTemplates.USER_DONATIONS_URL;
        String expectedViewName = ViewNames.USER_DONATIONS_VIEW;
        String sortType = "testSortType";
        List<Donation> donations = new ArrayList<>(List.of(TestDataFactory.getDonation(), TestDataFactory.getDonation()));

        when(donationService.getDonationsForUserSortedBy(sortType, loggedInUser)).thenReturn(donations);
        
//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(utlTemplate).param("sortType", sortType))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(donationService, times(1)).getDonationsForUserSortedBy(stringArgumentCaptor.capture(), userArgumentCaptor.capture());

        String capturedSortType = stringArgumentCaptor.getValue();
        User capturedUser = userArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(capturedSortType).isEqualTo(sortType),
                () -> assertThat(capturedUser).isSameAs(loggedInUser),
                () -> assertIterableEquals(donations, (List) modelAndView.getModel().get("donations"))
        );
    }

    @Test
    @WithMockCustomUser
    void whenShowDonationDetails_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
//        Arrange
        String utlTemplate = UrlTemplates.USER_DONATION_DETAILS_URL;
        String expectedViewName = ViewNames.USER_DONATION_DETAILS_VIEW;
        Long donationId = 1L;
        Donation donation = TestDataFactory.getDonation();

        when(donationService.getUserDonationById(loggedInUser, donationId)).thenReturn(donation);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(utlTemplate, donationId))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        verify(donationService, times(1)).getUserDonationById(userArgumentCaptor.capture(), longArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        Long capturedId = longArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(capturedUser).isSameAs(loggedInUser),
                () -> assertThat(capturedId).isEqualTo(donationId),
                () -> assertThat(modelAndView.getModel().get("donation")).isEqualTo(donation)
        );
    }

    @Test
    @WithMockCustomUser
    void whenShowDonationDetailsThrowException_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
//        Arrange
        String utlTemplate = UrlTemplates.USER_DONATION_DETAILS_URL;
        String expectedViewName = "error-page";
        String exceptionTitle = "Nie znaleziono";
        String exceptionMessage = "Dar nie istnieje";
        Long donationId = 1L;

        when(donationService.getUserDonationById(loggedInUser, donationId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(utlTemplate, donationId))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        verify(donationService, times(1)).getUserDonationById(userArgumentCaptor.capture(), longArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        Long capturedId = longArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(capturedUser).isSameAs(loggedInUser),
                () -> assertThat(capturedId).isEqualTo(donationId),
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(exceptionTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(exceptionMessage)
        );

    }
}