package pl.mateuszmarcyk.charity_donation_app.controller;

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
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
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
import static pl.mateuszmarcyk.charity_donation_app.TestDataFactory.*;

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
    UserRepository userRepository;

    @MockBean
    private FileUploadUtil fileUploadUtil;

    @MockBean
    private DonationService donationService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private InstitutionService institutionService;

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    public void whenShowDashboard_thenStatusIsOkAndModelIsPopulated() throws Exception {
//        Arrange
        User loggedInUser = getUser();
        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowAllAdmins_thenStatusIsOkAndModelIsPopulated() throws Exception {
//       Arrange
        List<User> admins = new ArrayList<>(List.of(new User(), new User()));
        when(userService.findAllAdmins(any(User.class))).thenReturn(admins);
        User loggedInUser = getUser();

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/all-admins"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users-all"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        List allAdmins = (List) modelAndView.getModel().get("users");
        assertIterableEquals(admins, allAdmins);

        User user = (User) modelAndView.getModel().get("user");
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).findAllAdmins(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(user);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowAllUsers_thenStatusIsOkAndModelIsPopulated() throws Exception {
//       Arrange
        User loggedInUser = getUser();
        List<User> users = new ArrayList<>(List.of(new User(), new User()));
        when(userService.findAllUsers(any(User.class))).thenReturn(users);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users-all"))
                .andReturn();


        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        assertThat(modelAndView.getModel().get("users")).isNotNull();

        List allUsers = (List) modelAndView.getModel().get("users");
        assertIterableEquals(users, allUsers);

        User user = (User) modelAndView.getModel().get("user");
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).findAllUsers(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser).isSameAs(user);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowUserById_thenStatusIsOkAndModelIsPopulated() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        User userToFind = getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);
        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-account-details"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        User userFromModel = (User) modelAndView.getModel().get("searchedUser");
        assertThat(userFromModel).isSameAs(userToFind);

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowUserByIdThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        String exceptionTitle = "Brak użytkownika";
        String exceptionMessage = "Użytkownik nie istnieje";
        Long userId = 1L;

        when(userService.findUserById(userId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        String modelExceptionTitle = (String) modelAndView.getModel().get("errorTitle");
        String modelExceptionMessage = (String) modelAndView.getModel().get("errorMessage");

        assertAll(
                () -> assertThat(modelExceptionTitle).isEqualTo(exceptionTitle),
                () -> assertThat(modelExceptionMessage).isEqualTo(exceptionMessage),
                () -> assertThat(modelAndView.getModel().get("searchedUser")).isNull()
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowUserProfileDetailsByUserId_thenStatusIsOkAndModelIsPopulated() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        User userToFind = getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/profiles/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-profile-details"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        UserProfile modelUserProfile = (UserProfile) modelAndView.getModel().get("profile");
        assertThat(modelUserProfile).isNotNull();
        assertThat(modelUserProfile).isSameAs(userToFind.getProfile());
    }

    @ParameterizedTest
    @CsvSource({"/admins/users/profiles/{id}",
            "/admins/users/profiles/edit{id}"})
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowUserProfileDetailsByUserIdOrShowUserProfileDetailsEditFormForUserThatIsNoInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        String exceptionTitle = "Brak użytkownika";
        String exceptionMessage = "Użytkownik nie istnieje";
        Long userId = 1L;

        when(userService.findUserById(userId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));
        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/profiles/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        String modelExceptionTitle = (String) modelAndView.getModel().get("errorTitle");
        String modelExceptionMessage = (String) modelAndView.getModel().get("errorMessage");

        assertAll(
                () -> assertThat(modelExceptionTitle).isEqualTo(exceptionTitle),
                () -> assertThat(modelExceptionMessage).isEqualTo(exceptionMessage),
                () -> assertThat(modelAndView.getModel().get("profile")).isNull()
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowUserProfileDetailsEditForm_thenStatusIsOkAndModelIsPopulated() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        User userToFind = getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/profiles/edit/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-profile-details-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        UserProfile modelUserProfile = (UserProfile) modelAndView.getModel().get("profile");
        assertThat(modelUserProfile).isNotNull();
        assertThat(modelUserProfile).isSameAs(userToFind.getProfile());
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenProcessUserProfileDetailsEditFormAndNoBidingErrors_thenUserProfileUpdatedAndStatusIsRedirected() throws Exception {
//        Arrange
        UserProfile changedUserProfile = TestDataFactory.getUserProfile();

        long profileId = 1L;
        User profileOwner = getUser();
        profileOwner.setId(2L);

        String endpoint = "/admins/users/profiles/edit";
        String expectedRedirectedUrl = "/admins/users/profiles/" + profileOwner.getId();

        when(userService.findUserByProfileId(profileId)).thenReturn(profileOwner);

        doAnswer(invocationOnMock -> null).when(fileUploadUtil).saveImage(any(UserProfile.class), any(MultipartFile.class), any(User.class));

//        Act & Assert
        mockMvc.perform(multipart(endpoint)
                        .file(new MockMultipartFile("image", new byte[0]))
                        .param("id", "1")
                        .flashAttr("profile", changedUserProfile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl))
                .andReturn();

        verify(fileUploadUtil, times(1)).saveImage(any(UserProfile.class), any(MultipartFile.class), any(User.class));
    }


    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowUserEditForm_thenStatusIsOkAndModelIsPopulated() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        User userToFind = getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/edit/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-account-edit-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        User modelUserToEdit = (User) modelAndView.getModel().get("userToEdit");

        assertThat(modelUserToEdit).isNotNull();
        assertThat(modelUserToEdit).isSameAs(userToFind);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowUserEditFormForUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        String exceptionTitle = "Brak użytkownika";
        String exceptionMessage = "Użytkownik nie istnieje";
        Long userId = 1L;

        when(userService.findUserById(userId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));
        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/edit/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        String modelExceptionTitle = (String) modelAndView.getModel().get("errorTitle");
        String modelExceptionMessage = (String) modelAndView.getModel().get("errorMessage");

        assertAll(
                () -> assertThat(modelExceptionTitle).isEqualTo(exceptionTitle),
                () -> assertThat(modelExceptionMessage).isEqualTo(exceptionMessage),
                () -> assertThat(modelAndView.getModel().get("userToEdit")).isNull()
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenProcessChangeEmailFormForInvalidEmail_thenEmailNotChangedAndStatusIsOkAndModelAttributesAdded() throws Exception {
//        Arrange
        User loggedInUser = getUser();
        User userToEdit = getUser();
        userToEdit.setEmail(null);
        userToEdit.setId(22L);
        String urlTemplate = "/admins/users/change-email";
        String expectedViewName = "admin-user-account-edit-form";

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("userToEdit", "email"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        verify(userRepository, never()).findByEmail(userToEdit.getEmail());
        verify(userService, never()).updateUserEmail(any(User.class));
    }


    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenProcessChangeEmailFormForValidEmail_thenEmailChangedAndStatusIsRedirected() throws Exception {
        //        Arrange
        User loggedInUser = getUser();
        User userToEdit = getUser();
        userToEdit.setId(22L);
        String urlTemplate = "/admins/users/change-email";
        String expectedRedirectedUrl = "/admins/users/%d".formatted(userToEdit.getId());

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        when(userRepository.findByEmail(userToEdit.getEmail())).thenReturn(Optional.empty());

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));


        verify(userRepository, times(1)).findByEmail(eq(userToEdit.getEmail()));

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).updateUserEmail(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(userToEdit);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenProcessChangePasswordFormForValidPassword_thenPasswordChangedAndStatusIsRedirected() throws Exception {
//        Arrange
        User loggedInUser = getUser();
        User userToEdit = getUser();

        String urlTemplate = "/admins/users/change-password";
        String expectedRedirectedUrl = "/admins/users/%d".formatted(userToEdit.getId());

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).changePassword(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(userToEdit);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenProcessChangePasswordFormForInvalidPassword_thenPasswordNotChangedAndStatusIsOk() throws Exception {
//        Arrange
        User loggedInUser = getUser();
        User userToEdit = getUser();
        userToEdit.setPassword(null);

        String urlTemplate = "/admins/users/change-password";
        String expectedViewName = "admin-user-account-edit-form";

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userToEdit", "password"))
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        verify(userService, never()).changePassword(any(User.class));
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenBlockUser_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        //       Arrange
        User userToFind = getUser();
        Long userId = 1L;

//        Act & Assert
        mockMvc.perform(get("/admins/users/block/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admins/users/" + userId));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).blockUserById(longArgumentCaptor.capture());
        Long capturedId= longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userToFind.getId());
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenBlockUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        //       Arrange
        String urlTemplate = "/admins/users/block/{id}";
        Long userId = 1L;
        String exceptionTitle = "Brak użytkownika";
        String exceptionMessage = "Użytkownik nie istnieje";

        doAnswer(invocationOnMock -> {
           throw  new ResourceNotFoundException(exceptionTitle, exceptionMessage);
        }).when(userService).blockUserById(userId);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(userService, times(1)).blockUserById(any(Long.class));
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenUnblockUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        //       Arrange
        String urlTemplate = "/admins/users/unblock/{id}";
        Long userId = 1L;
        String exceptionTitle = "Brak użytkownika";
        String exceptionMessage = "Użytkownik nie istnieje";

        doAnswer(invocationOnMock -> {
            throw  new ResourceNotFoundException(exceptionTitle, exceptionMessage);
        }).when(userService).blockUserById(userId);

        doAnswer(invocationOnMock -> {
            throw  new ResourceNotFoundException(exceptionTitle, exceptionMessage);
        }).when(userService).unblockUser(userId);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(userService, times(1)).unblockUser(any(Long.class));
    }


    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenUnblockUser_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        //       Arrange
        Long userId = 1L;

//        Act & Assert
        mockMvc.perform(get("/admins/users/unblock/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admins/users/" + userId));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).unblockUser(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenAddAdminRole_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        //       Arrange
        Long userId = 1L;

//        Act & Assert
        mockMvc.perform(get("/admins/users/upgrade/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admins/users/" + userId));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService).addAdminRole(longArgumentCaptor.capture());
        Long capturedLong = longArgumentCaptor.getValue();
        assertThat(capturedLong).isEqualTo(userId);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenAddAdminRoleToTheUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        //       Arrange
        String urlTemplate = "/admins/users/upgrade/{id}";
        Long userId = 1L;
        String exceptionTitle = "Brak użytkownika";
        String exceptionMessage = "Użytkownik nie istnieje";

        doAnswer(invocationOnMock -> {
            throw new ResourceNotFoundException(exceptionTitle, exceptionMessage);
        }).when(userService).addAdminRole(userId);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(userService, times(1)).addAdminRole(any(Long.class));

        String modelExceptionTitle = (String) modelAndView.getModel().get("errorTitle");
        String modelExceptionMessage = (String) modelAndView.getModel().get("errorMessage");

        assertAll(
                () -> assertThat(modelExceptionTitle).isEqualTo(exceptionTitle),
                () -> assertThat(modelExceptionMessage).isEqualTo(exceptionMessage)
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenRemoveAdminRoleToTheUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
        //       Arrange
        String urlTemplate = "/admins/users/downgrade/{id}";
        Long userId = 1L;
        String exceptionTitle = "Brak użytkownika";
        String exceptionMessage = "Użytkownik nie istnieje";

        doAnswer(invocationOnMock -> {
            throw new ResourceNotFoundException(exceptionTitle, exceptionMessage);
        }).when(userService).removeAdminRole(userId);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate, userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(userService, times(1)).removeAdminRole(any(Long.class));

        String modelExceptionTitle = (String) modelAndView.getModel().get("errorTitle");
        String modelExceptionMessage = (String) modelAndView.getModel().get("errorMessage");

        assertAll(
                () -> assertThat(modelExceptionTitle).isEqualTo(exceptionTitle),
                () -> assertThat(modelExceptionMessage).isEqualTo(exceptionMessage)
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenRemoveAdminRole_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        //       Arrange
        Long userId = 1L;

//        Act & Assert
        mockMvc.perform(get("/admins/users/downgrade/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admins/users/" + userId));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).removeAdminRole(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenDeleteUser_thenUserDeletedStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/admins/users/delete";
        String expectedRedirectedUrl = "/admins/users";
        Long userId = 1L;

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                .param("id", userId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).deleteUser(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(userId);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenDeleteUserThrowsException_thenStatusIsOkAndErrorPageRendered() throws Exception {
//        Arrange
        String urlTemplate = "/admins/users/delete";
        String expectedViewName = "error-page";
        String exceptionTitle = "Exception title";
        String exceptionMessage = "Exception message";

        Long userId = 1L;

        doAnswer(invocationOnMock -> {
            throw new ResourceNotFoundException(exceptionTitle, exceptionMessage);
        }).when(userService).deleteUser(userId);

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).deleteUser(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(userId);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowAllDonations_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        String sortType = "testSortType";
        User loggedInUser = getUser();
        List<Donation> donations = new ArrayList<>(List.of(getDonation(), getDonation()));

        when(donationService.findAll(sortType)).thenReturn(donations);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/donations").param("sortType", sortType))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-donations-all"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(donationService, times(1)).findAll(stringArgumentCaptor.capture());
        String capturedSortType = stringArgumentCaptor.getValue();
        assertThat(capturedSortType).isEqualTo(sortType);

        List modelDonations = (List) modelAndView.getModel().get("donations");

        assertIterableEquals(donations, modelDonations);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenArchiveDonation_thenStatusIsRedirected() throws Exception {
        //        Arrange
        String urlTemplate = "/admins/donations/archive";
        String expectedRedirectedUrl = "/admins/donations";
        Long donationId = 1L;

        Donation donationToArchive = getDonation();

        when(donationService.findDonationById(donationId)).thenReturn(donationToArchive);

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("donationId", donationId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(donationService, times(1)).findDonationById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(donationId);

        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
        verify(donationService, times(1)).archiveDonation(donationArgumentCaptor.capture());
        Donation capturedDonation = donationArgumentCaptor.getValue();
        assertThat(capturedDonation).isSameAs(donationToArchive);
    }

    @ParameterizedTest(name = "url={1}")
    @CsvSource({"/admins/donations/archive", "/admins/donations/unarchive"})
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenArchiveOrUnArchiveDonationAndExceptionIsThrown_thenStatusIsOkAndErrorPageRendered(String url) throws Exception {
        //        Arrange
        String expectedViewName = "error-page";
        String exceptionTitle = "Exception title";
        String exceptionMessage = "Exception message";
        Long donationId = 1L;


        when(donationService.findDonationById(donationId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

//        Act & Assert
        mockMvc.perform(post(url)
                        .param("donationId", donationId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(donationService, times(1)).findDonationById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(donationId);

        verify(donationService, never()).archiveDonation(any(Donation.class));
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenUnArchiveDonation_thenStatusIsRedirected() throws Exception {
        //        Arrange
        String urlTemplate = "/admins/donations/unarchive";
        String expectedRedirectedUrl = "/admins/donations";
        Long donationId = 1L;

        Donation donationToArchive = getDonation();

        when(donationService.findDonationById(donationId)).thenReturn(donationToArchive);

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("donationId", donationId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(donationService, times(1)).findDonationById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(donationId);

        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
        verify(donationService, times(1)).unArchiveDonation(donationArgumentCaptor.capture());
        Donation capturedDonation = donationArgumentCaptor.getValue();
        assertThat(capturedDonation).isSameAs(donationToArchive);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenDeleteDonation_thenStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/admins/donations/delete";
        String expectedRedirectUrl = "/admins/donations";
        Donation donationToDelete = getDonation();
        Long donationId = 1L;

        when(donationService.findDonationById(donationId)).thenReturn(donationToDelete);

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", donationId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(donationService, times(1)).findDonationById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(donationId);

        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
        verify(donationService, times(1)).deleteDonation(donationArgumentCaptor.capture());
        Donation capturedDonation = donationArgumentCaptor.getValue();
        assertThat(capturedDonation).isSameAs(donationToDelete);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenDeleteDonationAndExceptionIsThrown_thenStatusIsOkAndErrorPageRendered() throws Exception {
        //        Arrange
        String urlTemplate = "/admins/donations/delete";
        String expectedViewName = "error-page";
        String exceptionTitle = "Exception title";
        String exceptionMessage = "Exception message";
        Long donationId = 1L;


        when(donationService.findDonationById(donationId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", donationId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(donationService, times(1)).findDonationById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(donationId);

        verify(donationService, never()).deleteDonation(any(Donation.class));
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowDonationDetails_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        User loggedInUser = getUser();
        Donation foundDonation = getDonation();
        Long donationId = 1L;

        when(donationService.findDonationById(donationId)).thenReturn(foundDonation);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/donations/{id}", donationId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-donation-details"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(donationService, times(1)).findDonationById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(donationId);

        Donation modelDonation = (Donation) modelAndView.getModel().get("donation");
        assertThat(modelDonation).isSameAs(foundDonation);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowAllCategories_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        List<Category> categories = new ArrayList<>(List.of(getCategory(), getCategory()));

        when(categoryService.findAll()).thenReturn(categories);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-categories-all"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        verify(categoryService, times(1)).findAll();

        List modelCategories = (List) modelAndView.getModel().get("categories");

        assertIterableEquals(modelCategories, categories);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowCategoryDetails_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        Category foundCategory = getCategory();
        Long categoryId = 1L;

        when(categoryService.findCategoryById(categoryId)).thenReturn(foundCategory);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/categories/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-category-details"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(categoryService, times(1)).findCategoryById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(categoryId);

        Category modelCategory = (Category) modelAndView.getModel().get("category");
        assertThat(modelCategory).isSameAs(foundCategory);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "/admins/categories/{id}",
            "/admins/categories/edit/{id}"
    })
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowCategoryDetailsOrShowCategoryEditFormForCategoryThatIsNotInDatabase_thenAppExceptionHandlerHandlesException(String url) throws Exception {
        //       Arrange
        Long categoryId = 1L;
        String exceptionTitle = "Kategoria nie znaleziona";
        String exceptionMessage = "Kategoria nie istnieje";

        when(categoryService.findCategoryById(categoryId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(url, categoryId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(categoryService, times(1)).findCategoryById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(categoryId);

        String modelExceptionTitle = (String) modelAndView.getModel().get("errorTitle");
        String modelExceptionMessage = (String) modelAndView.getModel().get("errorMessage");

        assertAll(
                () -> assertThat(modelExceptionTitle).isEqualTo(exceptionTitle),
                () -> assertThat(modelExceptionMessage).isEqualTo(exceptionMessage),
                () -> assertThat(modelAndView.getModel().get("category")).isNull()
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowCategoryForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/categories/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-category-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        Category modelCategory = (Category) modelAndView.getModel().get("category");
        assertAll(
                () -> assertThat(modelCategory.getId()).isNull(),
                () -> assertThat(modelCategory.getName()).isNull(),
                () -> assertThat(modelCategory.getDonations()).isNull()
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenProcessCategoryFormAndCategoryValid_thenCategoryIsSavedAndStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/admins/categories/add";
        String expectedRedirectUrl = "/admins/categories";
        Category categoryToAdd = getCategory();
        User loggedInUser = getUser();

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                .flashAttr("category", categoryToAdd)
                .param("id", categoryToAdd.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Category> categoryArgumentCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryService, times(1)).save(categoryArgumentCaptor.capture());
        Category capturedCategory = categoryArgumentCaptor.getValue();
        assertThat(capturedCategory).isSameAs(categoryToAdd);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenProcessCategoryFormAndCategoryInvValid_thenStatusIsOkAndCategoryNotSaved() throws Exception {
//        Arrange
        String urlTemplate = "/admins/categories/add";
        String expectedViewName = "admin-category-form";
        Category categoryToAdd = getCategory();
        categoryToAdd.setName(null);
        User loggedInUser = getUser();

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("category", categoryToAdd)
                        .param("id", categoryToAdd.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("category", "name"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        verify(categoryService, never()).save(any(Category.class));
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowCategoryEditForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        Category foundCategory = getCategory();
        Long categoryId = 1L;

        when(categoryService.findCategoryById(categoryId)).thenReturn(foundCategory);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/categories/edit/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-category-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(categoryService, times(1)).findCategoryById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(categoryId);

        Category modelCategory = (Category) modelAndView.getModel().get("category");
        assertThat(modelCategory).isSameAs(foundCategory);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenDeleteCategory_thenStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/admins/categories/delete";
        String expectedRedirectUrl = "/admins/categories";
        Long categoryId = 1L;

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", categoryId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(categoryService, times(1)).deleteById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(categoryId);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenDeleteCategoryAndExceptionIsThrown_thenStatusIsOkAndErrorPageRendered() throws Exception {
        //        Arrange
        String urlTemplate = "/admins/categories/delete";
        String expectedViewName = "error-page";
        String exceptionTitle = "Exception title";
        String exceptionMessage = "Exception message";
        Long donationId = 1L;

        doAnswer(invocationOnMock -> {
            throw new ResourceNotFoundException(exceptionTitle, exceptionMessage);
        }).when(categoryService).deleteById(donationId);

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", donationId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(categoryService, times(1)).deleteById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(donationId);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowAllInstitutions_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        List<Institution> institutions = new ArrayList<>(List.of(getInstitution(), getInstitution()));

        when(institutionService.findAll()).thenReturn(institutions);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/institutions"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-institutions-all"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(institutionService, times(1)).findAll();
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowInstitutionDetails_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        Institution foundInstitution = getInstitution();
        Long institutionId = 1L;

        when(institutionService.findInstitutionById(institutionId)).thenReturn(foundInstitution);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/institutions/{id}", institutionId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-institution-details"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(institutionService, times(1)).findInstitutionById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(institutionId);

        Institution modelinstitution = (Institution) modelAndView.getModel().get("institution");
        assertThat(modelinstitution).isSameAs(foundInstitution);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "/admins/institutions/{id}",
            "/admins/institutions/edit/{id}"
    })
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowInstitutionDetailsOrShowInstitutionEditFormForInstitutionThatIsNotInDatabase_thenAppExceptionHandlerHandlesException(String url) throws Exception {
        //       Arrange
        Long institutionId = 1L;
        String exceptionTitle = "Instytucja nie znaleziona";
        String exceptionMessage = "Instytucja nie istnieje";

        when(institutionService.findInstitutionById(institutionId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(url, institutionId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(institutionService, times(1)).findInstitutionById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(institutionId);

        String modelExceptionTitle = (String) modelAndView.getModel().get("errorTitle");
        String modelExceptionMessage = (String) modelAndView.getModel().get("errorMessage");

        assertAll(
                () -> assertThat(modelExceptionTitle).isEqualTo(exceptionTitle),
                () -> assertThat(modelExceptionMessage).isEqualTo(exceptionMessage),
                () -> assertThat(modelAndView.getModel().get("institution")).isNull()
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowInstitutionForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/institutions/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-institution-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));


        Institution modelinstitution = (Institution) modelAndView.getModel().get("institution");

        assertAll(
                () -> assertThat(modelinstitution.getId()).isNull(),
                () -> assertThat(modelinstitution.getName()).isNull(),
                () -> assertThat(modelinstitution.getDescription()).isNull(),
                () -> assertThat(modelinstitution.getDescription()).isNull()
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenProcessInstitutionFormAndInstitutionIsValid_thenInstitutionAddedAndStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/admins/institutions/add";
        String expectedRedirectUrl = "/admins/institutions";
        User loggedInUser = getUser();
        Institution institutionToAdd = getInstitution();

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("institution", institutionToAdd))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Institution> institutionArgumentCaptor = ArgumentCaptor.forClass(Institution.class);
        verify(institutionService, times(1)).saveInstitution(institutionArgumentCaptor.capture());
        Institution capturedInstitution = institutionArgumentCaptor.getValue();
        assertThat(capturedInstitution).isSameAs(institutionToAdd);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenProcessInstitutionFormAndInstitutionIsInvalid_thenStatusIsOkAndInstitutionNotSaved() throws Exception {
//        Arrange
        String urlTemplate = "/admins/institutions/add";
        String expectedViewName = "admin-institution-form";
        User loggedInUser = getUser();
        Institution institutionToAdd = getInstitution();
        institutionToAdd.setDescription(null);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("institution", institutionToAdd))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("institution", "description"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        verify(institutionService, never()).saveInstitution(any(Institution.class));

    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenShowInstitutionEditForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        Institution foundInstitution = getInstitution();
        Long institutionId = 1L;

        when(institutionService.findInstitutionById(institutionId)).thenReturn(foundInstitution);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/institutions/edit/{id}", institutionId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-institution-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));


        Institution modelinstitution = (Institution) modelAndView.getModel().get("institution");

        assertThat(modelinstitution).isSameAs(foundInstitution);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenDeleteInstitution_thenStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/admins/institutions/delete";
        String expectedRedirectUrl = "/admins/institutions";
        Long institutionId = 1L;

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", institutionId.toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(institutionService, times(1)).deleteIntitutionById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(institutionId);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void whenDeleteInstitutionAndExceptionIsThrown_thenStatusIsOkAndErrorPageRendered() throws Exception {
        //        Arrange
        String urlTemplate = "/admins/institutions/delete";
        String expectedViewName = "error-page";
        String exceptionTitle = "Exception title";
        String exceptionMessage = "Exception message";
        Long donationId = 1L;

        doAnswer(invocationOnMock -> {
            throw new ResourceNotFoundException(exceptionTitle, exceptionMessage);
        }).when(institutionService).deleteIntitutionById(donationId);

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", donationId.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(institutionService, times(1)).deleteIntitutionById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(donationId);
    }

}


