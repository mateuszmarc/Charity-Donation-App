package pl.mateuszmarcyk.charity_donation_app.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.UrlTemplates;
import pl.mateuszmarcyk.charity_donation_app.ViewNames;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;
import pl.mateuszmarcyk.charity_donation_app.service.CategoryService;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.FileUploadUtil;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static pl.mateuszmarcyk.charity_donation_app.ErrorMessages.USER_NOT_FOUND_EXCEPTION_MESSAGE;
import static pl.mateuszmarcyk.charity_donation_app.ErrorMessages.USER_NOT_FOUND_EXCEPTION_TITLE;
import static pl.mateuszmarcyk.charity_donation_app.TestDataFactory.*;
import static pl.mateuszmarcyk.charity_donation_app.UrlTemplates.*;
import static pl.mateuszmarcyk.charity_donation_app.ViewNames.*;
import static pl.mateuszmarcyk.charity_donation_app.GlobalTestMethodVerifier.*;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private LoggedUserModelHandler loggedUserModelHandler;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private FileUploadUtil fileUploadUtil;

    @MockBean
    private DonationService donationService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private InstitutionService institutionService;

    private User loggedInUser;

    private Map<String, Object> expectedAttributes;

    @BeforeEach
    void setUp() {
        loggedInUser = TestDataFactory.getUser();
        expectedAttributes = new HashMap<>(Map.of("user", loggedInUser, "userProfile", loggedInUser.getProfile()));
        TestDataFactory.stubLoggedUserModelHandlerMethodsInvocation(loggedUserModelHandler, loggedInUser);
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowDashboard_thenStatusIsOkAndModelIsPopulated() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_DASHBOARD_URL;
        String expectedView = ViewNames.ADMIN_DASHBOARD_VIEW;

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowAllAdmins_thenStatusIsOkAndModelIsPopulated() throws Exception {
        //       Arrange
        List<User> admins = new ArrayList<>(List.of(new User(), new User()));
        when(userService.findAllAdmins(any(User.class))).thenReturn(admins);

        String urlTemplate = UrlTemplates.ADMIN_ALL_ADMINS_URL;
        String expectedView = ViewNames.ALL_ADMINS_AND_USERS_VIEW;

        //        Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        // Assert
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        expectedAttributes.put("users", admins);
        expectedAttributes.put("title", "Lista administratorów");
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),

                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verify(userService, times(1)).findAllAdmins(userArgumentCaptor.capture()),
                () -> assertThat(userArgumentCaptor.getValue()).isEqualTo(loggedInUser),

                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowAllUsers_thenStatusIsOkAndModelIsPopulated() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_ALL_USERS_URL;
        String expectedView = ViewNames.ALL_ADMINS_AND_USERS_VIEW;

        List<User> users = new ArrayList<>(List.of(new User(), new User()));
        when(userService.findAllUsers(any(User.class))).thenReturn(users);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        // Assert
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        expectedAttributes = Map.of(
                "user", loggedInUser,
                "userProfile", loggedInUser.getProfile(),
                "users", users
        );

        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verify(userService, times(1)).findAllUsers(userArgumentCaptor.capture()),
                () -> assertThat(userArgumentCaptor.getValue()).isEqualTo(loggedInUser),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowUserByIdAndFoundUserHasOneRoleUser_thenStatusIsOkAndModelIsPopulated() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_USER_ACCOUNT_DETAILS_URL;
        String expectedView = ViewNames.ADMIN_USER_ACCOUNT_DETAILS_VIEW;

        User userToFind = TestDataFactory.getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);
        expectedAttributes.put("searchedUser", userToFind);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId.toString())).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(userId);
                },
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("admin")).isNull()
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowUserByIdAndFoundUserHanTwoRoles_thenStatusIsOkAndModelIsPopulated() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_USER_ACCOUNT_DETAILS_URL;
        String expectedView = ViewNames.ADMIN_USER_ACCOUNT_DETAILS_VIEW;

        User userToFind = TestDataFactory.getUser();
        userToFind.getUserTypes().add(new UserType(1L, "ROLE_ADMIN", new ArrayList<>()));
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);
        expectedAttributes.put("searchedUser", userToFind);
        expectedAttributes.put("admin", true);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId.toString())).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(userId);
                },
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes)
        );
    }


    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowUserByIdThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_USER_ACCOUNT_DETAILS_URL;
        String expectedView = ERROR_PAGE_VIEW;
        String exceptionTitle = USER_NOT_FOUND_EXCEPTION_TITLE;
        String exceptionMessage = USER_NOT_FOUND_EXCEPTION_MESSAGE;
        Long userId = 1L;

        when(userService.findUserById(userId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

        expectedAttributes = new HashMap<>(Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        ));

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId.toString())).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(userId);
                },
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("searchedUser")).isNull()
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowUserProfileDetailsByUserId_thenStatusIsOkAndModelIsPopulated() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_USER_PROFILE_DETAILS_URL;
        String expectedView = ViewNames.ADMIN_USER_PROFILE_DETAILS_VIEW;

        User userToFind = TestDataFactory.getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);

        expectedAttributes = new HashMap<>(this.expectedAttributes);
        expectedAttributes.put("profile", userToFind.getProfile());

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId.toString())).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(userId);
                },
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler)
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
            ADMIN_USER_PROFILE_DETAILS_URL,
            ADMIN_USER_PROFILE_DETAILS_EDIT_URL
    })
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowUserProfileDetailsByUserIdOrShowUserProfileDetailsEditFormForUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException(String urlTemplate) throws Exception {
        // Arrange
        String expectedView = ERROR_PAGE_VIEW;
        String exceptionTitle = USER_NOT_FOUND_EXCEPTION_TITLE;
        String exceptionMessage = USER_NOT_FOUND_EXCEPTION_MESSAGE;
        Long userId = 1L;

        when(userService.findUserById(userId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );
        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId.toString())).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verify(userService, times(1)).findUserById(userId),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("searchedUser")).isNull()

        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowUserProfileDetailsEditForm_thenStatusIsOkAndModelIsPopulated() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_USER_PROFILE_DETAILS_EDIT_URL;
        String expectedView = ViewNames.ADMIN_USER_PROFILE_DETAILS_FORM_VIEW;
        User userToFind = TestDataFactory.getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);

        expectedAttributes.put("profile", userToFind.getProfile());

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId.toString())).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verify(userService, times(1)).findUserById(userId)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenProcessUserProfileDetailsEditFormAndNoBindingErrors_thenUserProfileUpdatedAndStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_USER_PROFILE_DETAILS_EDIT_POST_URL;
        UserProfile changedUserProfile = TestDataFactory.getUserProfile();
        long profileId = 1L;
        User profileOwner = TestDataFactory.getUser();
        profileOwner.setId(2L);
        String expectedRedirectUrl = UrlTemplates.ADMIN_USERS_PROFILES_URL + "/" + profileOwner.getId();

        when(userService.findUserByProfileId(profileId)).thenReturn(profileOwner);

        // Act
        MvcResult mvcResult = mockMvc.perform(multipart(urlTemplate)
                        .file(new MockMultipartFile("image", new byte[0]))
                        .param("id", String.valueOf(profileId))
                        .flashAttr("profile", changedUserProfile)
                        .with(csrf()))
                .andReturn();

        // Assert
        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> verify(fileUploadUtil, times(1)).saveImage(any(UserProfile.class), any(MultipartFile.class), any(User.class))
        );
    }


    @ParameterizedTest
    @CsvSource({
            "/admins/users/change-email/{id}, admin-user-email-edit-form",
            "/admins/users/change-password/{id}, admin-user-password-edit-form",
    })
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowUserChangeEmailOrChangePasswordForm_thenStatusIsOkAndModelIsPopulated(String url, String view ) throws Exception {
        // Arrange
        User userToFind = TestDataFactory.getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);

        expectedAttributes.put("userToEdit", userToFind);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(url, userId.toString())).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, view, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(userId);
                }
        );
    }

    @ParameterizedTest
    @CsvSource({UrlTemplates.ADMIN_USERS_EMAIL_EDIT_FORM_URL, UrlTemplates.ADMIN_USERS_PASSWORD_EDIT_FORM_URL})
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowUserEditEmailFormForUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException(String urlTemplate) throws Exception {
        // Arrange
        String expectedView = ERROR_PAGE_VIEW;

        String exceptionTitle = USER_NOT_FOUND_EXCEPTION_TITLE;

        String exceptionMessage = USER_NOT_FOUND_EXCEPTION_MESSAGE;
        Long userId = 1L;

        when(userService.findUserById(userId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verify(userService, times(1)).findUserById(userId),
                () -> assertThat(mvcResult.getModelAndView().getModel().get("userToEdit")).isNull()
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenProcessChangeEmailFormForInvalidEmail_thenEmailNotChangedAndStatusIsOkAndModelAttributesAdded() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_USERS_EMAIL_CHANGE_URL;
        String expectedView = ViewNames.ADMIN_USERS_CHANGE_EMAIL_FORM_VIEW;

        User userToEdit = TestDataFactory.getUser();
        userToEdit.setEmail(null); // Invalid email
        userToEdit.setId(22L);

        expectedAttributes.put("userToEdit", userToEdit);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(model().attributeHasFieldErrors("userToEdit", "email"))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verify(userRepository, never()).findByEmail(userToEdit.getEmail()),
                () -> verify(userService, never()).updateUserEmail(any(User.class))
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenProcessChangeEmailFormForValidEmail_thenEmailChangedAndStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_USERS_EMAIL_CHANGE_URL;

        User userToEdit = TestDataFactory.getUser();
        userToEdit.setId(22L);
        String expectedRedirectUrl = UrlTemplates.ADMIN_ALL_USERS_URL + "/" + userToEdit.getId();

        when(userRepository.findByEmail(userToEdit.getEmail())).thenReturn(Optional.empty());

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andReturn();

        // Assert
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        assertAll(
                () -> assertThat(modelAndView).isNotNull(),
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),

                () -> verify(userRepository, times(1)).findByEmail(userToEdit.getEmail()),
                () -> verify(userService, times(1)).updateUserEmail(userArgumentCaptor.capture()),
                () -> assertThat(userArgumentCaptor.getValue()).isSameAs(userToEdit)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenProcessChangePasswordFormForValidPassword_thenPasswordChangedAndStatusIsRedirected() throws Exception {
        // Arrange
        User userToEdit = TestDataFactory.getUser();
        String urlTemplate = UrlTemplates.ADMIN_USERS_PASSWORD_CHANGE_URL;
        String expectedRedirectedUrl = UrlTemplates.ADMIN_ALL_USERS_URL + "/" + userToEdit.getId();


        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andReturn();

        // Assert
        ModelAndView modelAndView = mvcResult.getModelAndView();
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        assertAll(
                () -> assertThat(modelAndView).isNotNull(),
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectedUrl),

                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),

                () -> verify(userService, times(1)).changePassword(userArgumentCaptor.capture()),
                () -> assertThat(userArgumentCaptor.getValue()).isSameAs(userToEdit)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenProcessChangePasswordFormForInvalidPassword_thenPasswordNotChangedAndStatusIsOk() throws Exception {
        // Arrange
        User userToEdit = TestDataFactory.getUser();
        userToEdit.setPassword(null); // Invalid password

        String urlTemplate = UrlTemplates.ADMIN_USERS_PASSWORD_CHANGE_URL;
        String expectedView = ViewNames.ADMIN_USERS_CHANGE_PASSWORD_FORM_VIEW;

        expectedAttributes.put("userToEdit", userToEdit);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(model().attributeHasFieldErrors("userToEdit", "password"))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verify(userService, never()).changePassword(any(User.class))
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenBlockUser_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        // Arrange
        Long userId = 1L;
        String urlTemplate = ADMIN_USERS_BLOCK_URL;
        String expectedRedirectUrl = ADMIN_ALL_USERS_URL + "/" + userId;

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId)).andReturn();

        // Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> verify(userService, times(1)).blockUserById(longArgumentCaptor.capture()),
                () -> assertThat(longArgumentCaptor.getValue()).isEqualTo(userId)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenBlockUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_USERS_BLOCK_URL;
        String expectedView = ERROR_PAGE_VIEW;
        Long userId = 1L;
        String exceptionTitle = USER_NOT_FOUND_EXCEPTION_TITLE;

        String exceptionMessage = USER_NOT_FOUND_EXCEPTION_MESSAGE;

        doThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage))
                .when(userService).blockUserById(userId);

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(userService, times(1)).blockUserById(userId)
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenUnblockUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_USERS_UNBLOCK_URL;
        String expectedView = ERROR_PAGE_VIEW;
        Long userId = 1L;
        String exceptionTitle = USER_NOT_FOUND_EXCEPTION_TITLE;

        String exceptionMessage = USER_NOT_FOUND_EXCEPTION_MESSAGE;

        doThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage))
                .when(userService).unblockUser(userId);

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(userService, times(1)).unblockUser(userId)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenUnblockUser_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        // Arrange
        Long userId = 1L;
        String urlTemplate = ADMIN_USERS_UNBLOCK_URL;
        String expectedRedirectUrl = ADMIN_ALL_USERS_URL + "/" + userId;

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId)).andReturn();

        // Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> verify(userService, times(1)).unblockUser(longArgumentCaptor.capture()),
                () -> assertThat(longArgumentCaptor.getValue()).isEqualTo(userId)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenAddAdminRole_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        // Arrange
        Long userId = 1L;
        String urlTemplate = ADMIN_USERS_UPGRADE_URL;
        String expectedRedirectUrl = ADMIN_ALL_USERS_URL + "/" + userId;

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId)).andReturn();

        // Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> verify(userService, times(1)).addAdminRole(longArgumentCaptor.capture()),
                () -> assertThat(longArgumentCaptor.getValue()).isEqualTo(userId)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenAddAdminRoleToTheUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_USERS_UPGRADE_URL;
        String expectedView = ERROR_PAGE_VIEW;
        Long userId = 1L;
        String exceptionTitle = USER_NOT_FOUND_EXCEPTION_TITLE;

        String exceptionMessage = USER_NOT_FOUND_EXCEPTION_MESSAGE;

        doThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage))
                .when(userService).addAdminRole(userId);

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(userService, times(1)).addAdminRole(userId)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenRemoveAdminRoleFromUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_USERS_DOWNGRADE_URL;
        String expectedView = ERROR_PAGE_VIEW;
        Long userId = 1L;
        String exceptionTitle = USER_NOT_FOUND_EXCEPTION_TITLE;

        String exceptionMessage = USER_NOT_FOUND_EXCEPTION_MESSAGE;

        doThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage))
                .when(userService).removeAdminRole(userId);

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(userService, times(1)).removeAdminRole(userId)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenRemoveAdminRole_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        // Arrange
        Long userId = 1L;
        String urlTemplate = ADMIN_USERS_DOWNGRADE_URL;
        String expectedRedirectUrl = ADMIN_ALL_USERS_URL + "/" + userId;

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId)).andReturn();

        // Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> verify(userService, times(1)).removeAdminRole(longArgumentCaptor.capture()),
                () -> assertThat(longArgumentCaptor.getValue()).isEqualTo(userId)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenDeleteUserWithUserRole_thenUserDeletedAndStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_USERS_DELETE_URL;
        String expectedRedirectedUrl = ADMIN_ALL_USERS_URL;
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(loggedInUser);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("id", userId.toString())
                        .with(csrf()))
                .andReturn();

        // Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectedUrl),

                () -> verify(userService, times(1)).deleteUser(longArgumentCaptor.capture()),
                () -> assertThat(longArgumentCaptor.getValue()).isEqualTo(userId)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenDeleteUserWithAdminRole_thenUserDeletedAndStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_USERS_DELETE_URL;
        String expectedRedirectedUrl = ADMIN_ALL_ADMINS_URL;
        Long userId = 1L;
        loggedInUser.getUserTypes().add(new UserType(1L, "ROLE_ADMIN", new ArrayList<>()));

        when(userService.findUserById(userId)).thenReturn(loggedInUser);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("id", userId.toString())
                        .with(csrf()))
                .andReturn();

        // Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectedUrl),

                () -> verify(userService, times(1)).deleteUser(longArgumentCaptor.capture()),
                () -> assertThat(longArgumentCaptor.getValue()).isEqualTo(userId)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenDeleteUserThrowsException_thenStatusIsOkAndErrorPageRendered() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_USERS_DELETE_URL;
        String expectedView = ERROR_PAGE_VIEW;
        String exceptionTitle = "Exception title";
        String exceptionMessage = "Exception message";
        Long userId = 1L;

        doThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage))
                .when(userService).deleteUser(userId);

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("id", userId.toString())
                        .with(csrf()))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(userService, times(1)).deleteUser(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(userId);
                }
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowAllDonations_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = UrlTemplates.ADMIN_DONATIONS_URL;
        String expectedView = ADMIN_DONATIONS_ALL_VIEW;
        String sortType = "testSortType";
        List<Donation> donations = List.of(getDonation(), getDonation());

        when(donationService.findAll(sortType)).thenReturn(donations);

        expectedAttributes.put("donations", donations);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)
                        .param("sortType", sortType))
                .andReturn();

        // Assert
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verify(donationService, times(1)).findAll(stringArgumentCaptor.capture()),
                () -> assertThat(stringArgumentCaptor.getValue()).isEqualTo(sortType)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenArchiveDonation_thenStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_DONATIONS_ARCHIVE_URL;
        String expectedRedirectedUrl = ADMIN_DONATIONS_URL;
        Long donationId = 1L;
        Donation donationToArchive = getDonation();

        when(donationService.findDonationById(donationId)).thenReturn(donationToArchive);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("donationId", donationId.toString())
                        .with(csrf()))
                .andReturn();

        // Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);

        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectedUrl),

                () -> verify(donationService, times(1)).findDonationById(longArgumentCaptor.capture()),
                () -> assertThat(longArgumentCaptor.getValue()).isEqualTo(donationId),

                () -> verify(donationService, times(1)).archiveDonation(donationArgumentCaptor.capture()),
                () -> assertThat(donationArgumentCaptor.getValue()).isSameAs(donationToArchive)
        );
    }

    @ParameterizedTest(name = "url={0}")
    @CsvSource({
            ADMIN_DONATIONS_ARCHIVE_URL,
            ADMIN_DONATIONS_UN_ARCHIVE_URL
    })
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenArchiveOrUnArchiveDonationAndExceptionIsThrown_thenStatusIsOkAndErrorPageRendered(String url) throws Exception {
        // Arrange
        String expectedView = ERROR_PAGE_VIEW;
        String exceptionTitle = "Exception title";
        String exceptionMessage = "Exception message";
        Long donationId = 1L;

        when(donationService.findDonationById(donationId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .param("donationId", donationId.toString())
                        .with(csrf()))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(donationService, times(1)).findDonationById(donationId),
                () -> verify(donationService, never()).archiveDonation(any(Donation.class))
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenUnArchiveDonation_thenStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_DONATIONS_UN_ARCHIVE_URL;
        String expectedRedirectedUrl = ADMIN_DONATIONS_URL;
        Long donationId = 1L;
        Donation donationToUnArchive = getDonation();

        when(donationService.findDonationById(donationId)).thenReturn(donationToUnArchive);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("donationId", donationId.toString())
                        .with(csrf()))
                .andReturn();

        // Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);

        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectedUrl),

                () -> verify(donationService, times(1)).findDonationById(longArgumentCaptor.capture()),
                () -> assertThat(longArgumentCaptor.getValue()).isEqualTo(donationId),

                () -> verify(donationService, times(1)).unArchiveDonation(donationArgumentCaptor.capture()),
                () -> assertThat(donationArgumentCaptor.getValue()).isSameAs(donationToUnArchive)
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenDeleteDonation_thenStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_DONATIONS_DELETE_URL;
        String expectedRedirectUrl = ADMIN_DONATIONS_URL;
        Long donationId = 1L;
        Donation donationToDelete = getDonation();

        when(donationService.findDonationById(donationId)).thenReturn(donationToDelete);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("id", donationId.toString())
                        .with(csrf()))
                .andReturn();

        // Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);

        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> verify(donationService, times(1)).findDonationById(longArgumentCaptor.capture()),
                () -> assertThat(longArgumentCaptor.getValue()).isEqualTo(donationId),

                () -> verify(donationService, times(1)).deleteDonation(donationArgumentCaptor.capture()),
                () -> assertThat(donationArgumentCaptor.getValue()).isSameAs(donationToDelete)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenDeleteDonationAndExceptionIsThrown_thenStatusIsOkAndErrorPageRendered() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_DONATIONS_DELETE_URL;
        String expectedView = ERROR_PAGE_VIEW;
        Long donationId = 1L;
        String exceptionTitle = "Exception title";
        String exceptionMessage = "Exception message";

        when(donationService.findDonationById(donationId))
                .thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("id", donationId.toString())
                        .with(csrf()))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(donationService, times(1)).findDonationById(donationId),
                () -> verify(donationService, never()).deleteDonation(any(Donation.class))
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowDonationDetails_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        // Arrange
        Donation foundDonation = getDonation();
        Long donationId = 1L;
        String expectedView = ADMIN_DONATIONS_DETAILS_VIEW;

        when(donationService.findDonationById(donationId)).thenReturn(foundDonation);

        expectedAttributes.put("donation", foundDonation);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(ADMIN_DONATIONS_DONATION_DETAILS_URL, donationId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(donationService, times(1)).findDonationById(donationId),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowAllCategories_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_CATEGORIES_URL;
        String expectedView = ADMIN_CATEGORIES_ALL_VIEW;

        List<Category> categories = List.of(getCategory(), getCategory());
        when(categoryService.findAll()).thenReturn(categories);

        expectedAttributes.put("categories", categories);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(categoryService, times(1)).findAll(),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler)
        );
    }


    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowCategoryDetails_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_CATEGORIES_DETAILS_URL;
        String expectedView = ADMIN_CATEGORY_DETAILS_VIEW;

        Category foundCategory = getCategory();
        Long categoryId = 1L;

        when(categoryService.findCategoryById(categoryId)).thenReturn(foundCategory);

        expectedAttributes.put("category", foundCategory);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, categoryId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(categoryService, times(1)).findCategoryById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(categoryId);
                },
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler)
        );
    }

    @ParameterizedTest(name = "URL={0}")
    @CsvSource(value = {
            ADMIN_CATEGORIES_DETAILS_URL,
            ADMIN_CATEGORIES_EDIT_URL
    })
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowCategoryDetailsOrEditFormForNonExistentCategory_thenAppExceptionHandlerHandlesException(String urlTemplate) throws Exception {
        // Arrange
        Long categoryId = 1L;
        String expectedView = ERROR_PAGE_VIEW;
        String exceptionTitle = "Kategoria nie znaleziona";
        String exceptionMessage = "Kategoria nie istnieje";

        when(categoryService.findCategoryById(categoryId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, categoryId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(categoryService, times(1)).findCategoryById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(categoryId);
                },
                () -> assertThat(mvcResult.getModelAndView().getModel().get("category")).isNull()
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowCategoryForm_thenStatusIsOkAndAllAttributesAreAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_CATEGORIES_ADD_URL;
        String expectedView = ADMIN_CATEGORY_FORM_VIEW;
        Category emptyCategory = new Category();

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> {
                    Category categoryFromModel = (Category) mvcResult.getModelAndView().getModel().get("category");
                    assertThat(categoryFromModel.getId()).isEqualTo(emptyCategory.getId());
                    assertThat(categoryFromModel.getName()).isEqualTo(emptyCategory.getName());
                    assertThat(categoryFromModel.getDonations()).isEqualTo(emptyCategory.getDonations());
                }
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenProcessCategoryFormAndCategoryValid_thenCategoryIsSavedAndStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_CATEGORIES_ADD_URL;
        String expectedRedirectUrl = ADMIN_CATEGORIES_URL;
        Category categoryToAdd = getCategory();

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("category", categoryToAdd)
                        .param("id", categoryToAdd.getId().toString()))
                .andReturn();

        // Assert
        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),

                () -> {
                    ArgumentCaptor<Category> categoryArgumentCaptor = ArgumentCaptor.forClass(Category.class);
                    verify(categoryService, times(1)).save(categoryArgumentCaptor.capture());
                    Category capturedCategory = categoryArgumentCaptor.getValue();
                    assertThat(capturedCategory).isSameAs(categoryToAdd);
                }
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenProcessCategoryFormAndCategoryInvalid_thenStatusIsOkAndCategoryNotSaved() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_CATEGORIES_ADD_URL;
        String expectedView = ADMIN_CATEGORY_FORM_VIEW;
        Category categoryToAdd = getCategory();
        categoryToAdd.setName(null);

        expectedAttributes.put("category", categoryToAdd);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("category", categoryToAdd)
                        .param("id", categoryToAdd.getId().toString()))
                .andExpect(model().attributeHasFieldErrors("category", "name"))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verify(categoryService, never()).save(any(Category.class))
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowCategoryEditForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_CATEGORIES_EDIT_URL;
        String expectedView = ADMIN_CATEGORY_FORM_VIEW;
        Category foundCategory = getCategory();
        Long categoryId = 1L;

        when(categoryService.findCategoryById(categoryId)).thenReturn(foundCategory);

       expectedAttributes.put("category", foundCategory);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, categoryId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(categoryService, times(1)).findCategoryById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(categoryId);
                }
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenDeleteCategory_thenStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_CATEGORIES_DELETE_URL;
        String expectedRedirectUrl = ADMIN_CATEGORIES_URL;
        Long categoryId = 1L;

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("id", categoryId.toString()))
                .andReturn();

        // Assert
        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(categoryService, times(1)).deleteById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(categoryId);
                }
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenDeleteCategoryAndExceptionIsThrown_thenStatusIsOkAndErrorPageRendered() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_CATEGORIES_DELETE_URL;
        String expectedView = ERROR_PAGE_VIEW;
        String exceptionTitle = "Exception title";
        String exceptionMessage = "Exception message";
        Long categoryId = 1L;

        doThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage))
                .when(categoryService).deleteById(categoryId);

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("id", categoryId.toString()))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(categoryService, times(1)).deleteById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(categoryId);
                }
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowAllInstitutions_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_INSTITUTIONS_URL;
        String expectedView = ADMIN_INSTITUTIONS_ALL_VIEW;
        List<Institution> institutions = List.of(getInstitution(), getInstitution());

        when(institutionService.findAll()).thenReturn(institutions);

       expectedAttributes.put("institutions", institutions);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(institutionService, times(1)).findAll(),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowInstitutionDetails_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_INSTITUTIONS_DETAILS_URL;
        String expectedView = ADMIN_INSTITUTION_DETAILS_VIEW;
        Long institutionId = 1L;
        Institution foundInstitution = getInstitution();

        when(institutionService.findInstitutionById(institutionId)).thenReturn(foundInstitution);

       expectedAttributes.put("institution", foundInstitution);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, institutionId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(institutionService, times(1)).findInstitutionById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(institutionId);
                },
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler)
        );
    }

    @ParameterizedTest
    @CsvSource(value = {
            ADMIN_INSTITUTIONS_DETAILS_URL,
            ADMIN_INSTITUTIONS_EDIT_URL
    })
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowInstitutionDetailsOrShowInstitutionEditFormForInstitutionThatIsNotInDatabase_thenAppExceptionHandlerHandlesException(String url) throws Exception {
        // Arrange
        Long institutionId = 1L;
        String expectedView = ERROR_PAGE_VIEW;
        String exceptionTitle = "Instytucja nie znaleziona";
        String exceptionMessage = "Instytucja nie istnieje";

        when(institutionService.findInstitutionById(institutionId))
                .thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(get(url, institutionId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(institutionService, times(1)).findInstitutionById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(institutionId);
                },
                () -> assertThat(mvcResult.getModelAndView().getModel().get("institution")).isNull()
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowInstitutionForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_INSTITUTIONS_ADD_URL;
        String expectedView = ADMIN_INSTITUTION_FORM_VIEW;

        Institution emptyInstitution = new Institution();

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    Institution institution = (Institution) mvcResult.getModelAndView().getModel().get("institution");
                    assertThat(institution.getId()).isEqualTo(emptyInstitution.getId());
                    assertThat(institution.getName()).isEqualTo(emptyInstitution.getName());
                    assertThat(institution.getDescription()).isEqualTo(emptyInstitution.getDescription());
                    assertThat(institution.getDonations()).isEqualTo(emptyInstitution.getDonations());
                }
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenProcessInstitutionFormAndInstitutionIsValid_thenInstitutionAddedAndStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_INSTITUTIONS_ADD_URL;
        String expectedRedirectUrl = ADMIN_INSTITUTIONS_URL;
        Institution institutionToAdd = getInstitution();


        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("institution", institutionToAdd))
                .andReturn();

        // Assert
        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),

                () -> {
                    ArgumentCaptor<Institution> institutionArgumentCaptor = ArgumentCaptor.forClass(Institution.class);
                    verify(institutionService, times(1)).saveInstitution(institutionArgumentCaptor.capture());
                    Institution capturedInstitution = institutionArgumentCaptor.getValue();
                    assertThat(capturedInstitution).isSameAs(institutionToAdd);
                }
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenProcessInstitutionFormAndInstitutionIsInvalid_thenStatusIsOkAndInstitutionNotSaved() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_INSTITUTIONS_ADD_URL;
        String expectedView = ADMIN_INSTITUTION_FORM_VIEW;
        Institution institutionToAdd = getInstitution();
        institutionToAdd.setDescription(null);

        expectedAttributes.put("institution", institutionToAdd);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("institution", institutionToAdd))
                .andExpect(model().attributeHasFieldErrors("institution", "description"))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(institutionService, never()).saveInstitution(any(Institution.class)),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenShowInstitutionEditForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_INSTITUTIONS_EDIT_URL;
        String expectedView = ADMIN_INSTITUTION_FORM_VIEW;
        Institution foundInstitution = getInstitution();
        Long institutionId = 1L;

        when(institutionService.findInstitutionById(institutionId)).thenReturn(foundInstitution);

        expectedAttributes.put("institution", foundInstitution);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, institutionId)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(institutionService, times(1)).findInstitutionById(longArgumentCaptor.capture());
                    Long capturedId = longArgumentCaptor.getValue();
                    assertThat(capturedId).isEqualTo(institutionId);
                },
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenDeleteInstitution_thenStatusIsRedirected() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_INSTITUTIONS_DELETE_URL;
        String expectedRedirectUrl = ADMIN_INSTITUTIONS_URL;
        Long institutionId = 1L;

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("id", institutionId.toString()))
                .andReturn();

        // Assert
        assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus()).isEqualTo(302),
                () -> assertThat(mvcResult.getResponse().getRedirectedUrl()).isEqualTo(expectedRedirectUrl),

                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(institutionService, times(1)).deleteIntitutionById(longArgumentCaptor.capture());
                    Long capturedId = longArgumentCaptor.getValue();
                    assertThat(capturedId).isEqualTo(institutionId);
                }
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void whenDeleteInstitutionAndExceptionIsThrown_thenStatusIsOkAndErrorPageRendered() throws Exception {
        // Arrange
        String urlTemplate = ADMIN_INSTITUTIONS_DELETE_URL;
        String expectedView = ERROR_PAGE_VIEW;
        Long institutionId = 1L;
        String exceptionTitle = "Exception title";
        String exceptionMessage = "Exception message";

        doThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage))
                .when(institutionService).deleteIntitutionById(institutionId);

        expectedAttributes = Map.of(
                "errorTitle", exceptionTitle,
                "errorMessage", exceptionMessage
        );

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .param("id", institutionId.toString()))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(institutionService, times(1)).deleteIntitutionById(longArgumentCaptor.capture());
                    assertThat(longArgumentCaptor.getValue()).isEqualTo(institutionId);
                }
        );
    }
}


