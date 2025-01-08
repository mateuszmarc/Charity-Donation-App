package pl.mateuszmarcyk.charity_donation_app.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomAccessDeniedHandler;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
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

    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    public void givenUserWithAdminRole_whenShowDashboard_thenStatusIsOkAndModelIsPopulated() throws Exception {
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
    void givenUserWithAdminRole_whenShowAllAdmins_thenStatusIsOkAndModelIsPopulated() throws Exception {
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
    void givenUserWithAdminRole_whenShowAllUsers_thenStatusIsOkAndModelIsPopulated() throws Exception {
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
    void givenUserWithAdminRole_whenShowUserById_thenStatusIsOkAndModelIsPopulated() throws Exception {
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
    void givenUserWithAdminRole_whenShowUserByIdThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
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
    void givenUserWithAdminRole_whenShowUserProfileDetailsByUserId_thenStatusIsOkAndModelIsPopulated() throws Exception {
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
    void givenUserWithAdminRole_whenShowUserProfileDetailsByUserIdOrShowUserProfileDetailsEditFormForUserThatIsNoInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
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
    void givenUserWithAdminRole_whenShowUserProfileDetailsEditForm_thenStatusIsOkAndModelIsPopulated() throws Exception {
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
    void givenUserWithAdminRole_whenShowUserEditForm_thenStatusIsOkAndModelIsPopulated() throws Exception {
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
    void givenUserWithAdminRole_whenShowUserEditFormForUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException() throws Exception {
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
    void givenUserWithAdminRole_whenBlockUser_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        //       Arrange
        User userToFind = getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);

//        Act & Assert
        mockMvc.perform(get("/admins/users/block/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admins/users/" + userId));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).blockUser(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(userToFind);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "/admins/users/block/{id}",
            "/admins/users/unblock/{id}"
    })
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenBlockOrUnblockUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException(String url) throws Exception {
        //       Arrange
        Long userId = 1L;
        String exceptionTitle = "Brak użytkownika";
        String exceptionMessage = "Użytkownik nie istnieje";

        when(userService.findUserById(userId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(url, userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        verify(userService, never()).blockUser(any(User.class));
        verify(userService, never()).unblockUser(any(User.class));

        String modelExceptionTitle = (String) modelAndView.getModel().get("errorTitle");
        String modelExceptionMessage = (String) modelAndView.getModel().get("errorMessage");

        assertAll(
                () -> assertThat(modelExceptionTitle).isEqualTo(exceptionTitle),
                () -> assertThat(modelExceptionMessage).isEqualTo(exceptionMessage)
        );
    }


    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenUnblockUser_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        //       Arrange
        User userToFind = getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);

//        Act & Assert
        mockMvc.perform(get("/admins/users/unblock/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admins/users/" + userId));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).unblockUser(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(userToFind);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenAddAdminRole_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        //       Arrange
        User userToFind = getUser();
        Long userId = 1L;

//        Act & Assert
        when(userService.findUserById(userId)).thenReturn(userToFind);

        mockMvc.perform(get("/admins/users/upgrade/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admins/users/" + userId));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).addAdminRole(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(userToFind);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "/admins/users/upgrade/{id}",
            "/admins/users/downgrade/{id}"
    })
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenAddOrRemoveAdminRoleToTheUserThatIsNotInDatabase_thenAppExceptionHandlerHandlesException(String url) throws Exception {
        //       Arrange
        Long userId = 1L;
        String exceptionTitle = "Brak użytkownika";
        String exceptionMessage = "Użytkownik nie istnieje";

        when(userService.findUserById(userId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(url, userId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        verify(userService, never()).addAdminRole(any(User.class));
        verify(userService, never()).removeAdminRole(any(User.class));

        String modelExceptionTitle = (String) modelAndView.getModel().get("errorTitle");
        String modelExceptionMessage = (String) modelAndView.getModel().get("errorMessage");

        assertAll(
                () -> assertThat(modelExceptionTitle).isEqualTo(exceptionTitle),
                () -> assertThat(modelExceptionMessage).isEqualTo(exceptionMessage)
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenRemoveAdminRole_thenStatusIsRedirectedAndServiceMethodCalled() throws Exception {
        //       Arrange
        User userToFind = getUser();
        Long userId = 1L;

//        Act & Assert
        when(userService.findUserById(userId)).thenReturn(userToFind);

        mockMvc.perform(get("/admins/users/downgrade/{id}", userId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admins/users/" + userId));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).removeAdminRole(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(userToFind);
    }


    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenShowAllDonations_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
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
    void givenUserWithAdminRole_whenShowDonationDetails_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        User loggedInUser = getUser();
        Donation foundDonation = getDonation();
        Long donationId = 1L;

        when(donationService.getDonationById(donationId)).thenReturn(foundDonation);

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
        verify(donationService, times(1)).getDonationById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(donationId);

        Donation modelDonation = (Donation) modelAndView.getModel().get("donation");
        assertThat(modelDonation).isSameAs(foundDonation);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenShowAllCategories_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        String sortType = "testSortType";
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
    void givenUserWithAdminRole_whenShowCategoryDetails_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        Category foundCategory = getCategory();
        Long categoryId = 1L;

        when(categoryService.findById(categoryId)).thenReturn(foundCategory);

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
        verify(categoryService, times(1)).findById(longArgumentCaptor.capture());
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
    void givenUserWithAdminRole_whenShowCategoryDetailsOrShowCategoryEditFormForCategoryThatIsNotInDatabase_thenAppExceptionHandlerHandlesException(String url) throws Exception {
        //       Arrange
        Long categoryId = 1L;
        String exceptionTitle = "Kategoria nie znaleziona";
        String exceptionMessage = "Kategoria nie istnieje";

        when(categoryService.findById(categoryId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(url, categoryId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(categoryService, times(1)).findById(longArgumentCaptor.capture());
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
    void givenUserWithAdminRole_whenShowCategoryForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
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
    void givenUserWithAdminRole_whenShowEditCategoryForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        Category foundCategory = getCategory();
        Long categoryId = 1L;

        when(categoryService.findById(categoryId)).thenReturn(foundCategory);

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
        verify(categoryService, times(1)).findById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(categoryId);

        Category modelCategory = (Category) modelAndView.getModel().get("category");
        assertThat(modelCategory).isSameAs(foundCategory);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenShowAllInstitutions_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
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
    void givenUserWithAdminRole_whenShowInstitutionDetails_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        Institution foundInstitution = getInstitution();
        Long institutionId = 1L;

        when(institutionService.findById(institutionId)).thenReturn(foundInstitution);

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
        verify(institutionService, times(1)).findById(longArgumentCaptor.capture());
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
    void givenUserWithAdminRole_whenShowInstitutionDetailsOrShowInstitutionEditFormForInstitutionThatIsNotInDatabase_thenAppExceptionHandlerHandlesException(String url) throws Exception {
        //       Arrange
        Long institutionId = 1L;
        String exceptionTitle = "Instytucja nie znaleziona";
        String exceptionMessage = "Instytucja nie istnieje";

        when(institutionService.findById(institutionId)).thenThrow(new ResourceNotFoundException(exceptionTitle, exceptionMessage));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get(url, institutionId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(institutionService, times(1)).findById(longArgumentCaptor.capture());
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
    void givenUserWithAdminRole_whenShowInstitutionForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
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
    void givenUserWithAdminRole_whenShowInstitutionEditForm_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        User loggedInUser = getUser();
        Institution foundInstitution = getInstitution();
        Long institutionId = 1L;

        when(institutionService.findById(institutionId)).thenReturn(foundInstitution);

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

    private static Institution getInstitution() {
        return new Institution(1L, "test name", "test description", new ArrayList<>());
    }

    private static Category getCategory() {
        return new Category(1L, "CategoryName", new ArrayList<>());
    }

    private static Donation getDonation() {
        Institution institution = new Institution(1L, "Pomocna Dłoń", "Description", new ArrayList<>());
        User user = new User();
        user.setDonations(new ArrayList<>());
        Category category = new Category(1L, "Jedzenie", new ArrayList<>());

        Donation donationOne = new Donation(
                LocalDateTime.parse("2024-12-24T12:00:00"),
                false,
                user,
                institution,
                new ArrayList<>(List.of(category)),
                "123456789",
                "Please call on arrival.",
                LocalTime.parse("10:30:00"),
                LocalDate.parse("2024-12-31"),
                "12-345",
                "Kindness City",
                "123 Charity Lane",
                10
        );
        donationOne.setId(1L);

        institution.getDonations().add(donationOne);
        donationOne.setInstitution(institution);
        donationOne.setCreated(LocalDateTime.now());

        user.getDonations().add(donationOne);
        donationOne.setUser(user);

        category.getDonations().add(donationOne);
        donationOne.getCategories().add(category);

        return donationOne;
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
                "testPW",
                LocalDateTime.of(2023, 11, 11, 12, 25, 11),
                "testPW",
                new HashSet<>(Set.of(userType)),
                userProfile,
                null,
                null,
                new ArrayList<>()
        );

        userProfile.setUser(user);
        return user;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    private static class ExpectedData {
        String expectedEmail = "test@email.com";
        String expectedProfileFirstName = "Mateusz";
        String expectedProfileLastName = "Marcykiewicz";
        String expectedCity = "Kielce";
        String expectedCountry = "Poland";
        String expectedPhoneNumber = "555666777";
    }
}


