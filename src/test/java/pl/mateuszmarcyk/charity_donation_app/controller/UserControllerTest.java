package pl.mateuszmarcyk.charity_donation_app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.FileUploadUtil;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenShowUserDetails_thenStatusIsOkAndModelAttributesAdded() throws Exception {
//        Arrange
        User user = spy(getUser());

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(user);

        doAnswer(invocationOnMock -> {
            User modelUser = invocationOnMock.getArgument(0);
            Model model = invocationOnMock.getArgument(1);

            model.addAttribute("user", modelUser);
            model.addAttribute("userProfile", modelUser.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile-details-info"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        assertAll(
                () -> assertThat(modelAndView.getModel().get("user")).isSameAs(user),
                () -> assertThat(modelAndView.getModel().get("userProfile")).isSameAs(user.getProfile())
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenDisplayUserProfileEditForm_thenStatusIsOkAndModelAttributesAdded() throws Exception {
        //        Arrange
        User user = spy(getUser());

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(user);

        doAnswer(invocationOnMock -> {
            User modelUser = invocationOnMock.getArgument(0);
            Model model = invocationOnMock.getArgument(1);

            model.addAttribute("user", modelUser);
            model.addAttribute("userProfile", modelUser.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/profile/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-profile-edit-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        assertAll(
                () -> assertThat(modelAndView.getModel().get("user")).isSameAs(user),
                () -> assertThat(modelAndView.getModel().get("userProfile")).isSameAs(user.getProfile())
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
}