package pl.mateuszmarcyk.charity_donation_app.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomAccessDeniedHandler;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;
import pl.mateuszmarcyk.charity_donation_app.service.CategoryService;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.FileUploadUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

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
        ExpectedData expectedData = new ExpectedData();

        MvcResult mvcResult = mockMvc.perform(get("/admins/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertUserAndUserProfileInModel(modelAndView, expectedData);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenShowAllAdmins_thenStatusIsOkAndModelIsPopulated() throws Exception {
//       Arrange
        ExpectedData expectedData = new ExpectedData();
        List<User> admins = new ArrayList<>(List.of(new User(), new User()));
        when(userService.findAllAdmins(any(User.class))).thenReturn(admins);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/all-admins"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users-all"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertUserAndUserProfileInModel(modelAndView, expectedData);
        assertThat(modelAndView).isNotNull();

        List allAdmins = (List) modelAndView.getModel().get("users");

        assertAll(
                () ->assertThat(allAdmins).isNotNull(),
                () ->assertThat(allAdmins).isNotEmpty(),
                () -> assertIterableEquals(admins, allAdmins),
                () -> assertThat(allAdmins.get(0)).isSameAs(admins.get(0)),
                () -> assertThat(allAdmins.get(1)).isSameAs(admins.get(1))
        );

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
        ExpectedData expectedData = new ExpectedData();
        List<User> users = new ArrayList<>(List.of(new User(), new User()));
        when(userService.findAllUsers(any(User.class))).thenReturn(users);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users-all"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertUserAndUserProfileInModel(modelAndView, expectedData);


        assertThat(modelAndView.getModel().get("users")).isNotNull();

        List allUsers = (List) modelAndView.getModel().get("users");

        assertAll(
                () -> assertThat(allUsers).isNotNull(),
                () -> assertThat(allUsers).isNotEmpty(),
                () -> assertIterableEquals(users, allUsers),
                () -> assertThat(allUsers.get(0)).isSameAs(users.get(0)),
                () -> assertThat(allUsers.get(1)).isSameAs(users.get(1))
        );

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
        ExpectedData expectedData = new ExpectedData();

        User userToFind = getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);
        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-account-details"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertUserAndUserProfileInModel(modelAndView, expectedData);

        User userFromModel = (User) modelAndView.getModel().get("searchedUser");
        assertThat(userFromModel).isSameAs(userToFind);

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenShowUserProfileDetailsForm_thenStatusIsOkAndModelIsPopulated() throws Exception {
        //       Arrange
        ExpectedData expectedData = new ExpectedData();
        User userToFind = getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);
        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/profiles/edit/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-profile-details-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertUserAndUserProfileInModel(modelAndView, expectedData);

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
    void givenUserWithAdminRole_whenShowUserProfileDetailsEditForm_thenStatusIsOkAndModelIsPopulated() throws Exception {
        //       Arrange
        ExpectedData expectedData = new ExpectedData();
        User userToFind = getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);
        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/profiles/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-profile-details"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertUserAndUserProfileInModel(modelAndView, expectedData);

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        UserProfile foundProfile = (UserProfile) modelAndView.getModel().get("profile");
        assertThat(foundProfile).isNotNull();
        assertThat(foundProfile).isSameAs(userToFind.getProfile());
    }


    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenShowEditUserForm_thenStatusIsOkAndModelIsPopulated() throws Exception {
        //       Arrange
        ExpectedData expectedData = new ExpectedData();
        User userToFind = getUser();
        Long userId = 1L;

        when(userService.findUserById(userId)).thenReturn(userToFind);
        MvcResult mvcResult = mockMvc.perform(get("/admins/users/edit/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-user-account-edit-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertUserAndUserProfileInModel(modelAndView, expectedData);

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService, times(1)).findUserById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        User modelUserToEdit = (User) modelAndView.getModel().get("userToEdit");

        assertThat(modelUserToEdit).isNotNull();
        assertThat(modelUserToEdit).isSameAs(userToFind);
    }




    private static void assertUserAndUserProfileInModel(ModelAndView modelAndView, ExpectedData expectedData) {
        assertTrue(modelAndView.getModel().containsKey("user"));
        assertTrue(modelAndView.getModel().containsKey("userProfile"));

        User user = (User) modelAndView.getModel().get("user");
        UserProfile userProfile = (UserProfile) modelAndView.getModel().get("userProfile");

        assertAll(
                () -> assertThat(user).isNotNull(),
                () -> assertThat(userProfile).isNotNull(),
                () -> {
                    assert user != null;
                    assertThat(user.getEmail()).isEqualTo(expectedData.getExpectedEmail());
                },
                () -> {
                    assert userProfile != null;
                    assertThat(userProfile.getFirstName()).isEqualTo(expectedData.getExpectedProfileFirstName());
                    assertThat(userProfile.getLastName()).isEqualTo(expectedData.getExpectedProfileLastName());
                    assertThat(userProfile.getCity()).isEqualTo(expectedData.getExpectedCity());
                    assertThat(userProfile.getPhoneNumber()).isEqualTo(expectedData.getExpectedPhoneNumber());
                    assertThat(userProfile.getCountry()).isEqualTo(expectedData.getExpectedCountry());
                }
        );
    }

    private static User getUser() {
        UserProfile userProfile = new UserProfile(2L, null, "Mateusz", "Test", "TestCity",
                "Test Country", null, "TestPhoneNumber");
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
        String expectedEmail = "admin@admin.com";
        String expectedProfileFirstName = "Mateusz";
        String expectedProfileLastName = "Marcykiewicz";
        String expectedCity = "Kielce";
        String expectedCountry = "Poland";
        String expectedPhoneNumber = "555666777";
    }
}


