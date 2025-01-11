package pl.mateuszmarcyk.charity_donation_app.controller;

import org.junit.jupiter.api.Test;
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
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    void whenShowUserDetails_thenStatusIsOkAndModelAttributesAdded() throws Exception {
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
    void whenShowUserProfileEditForm_thenStatusIsOkAndModelAttributesAdded() throws Exception {
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

    @Test
    @WithMockCustomUser
    void whenProcessUserProfileEditForm_thenStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/profile/edit";
        String expectedRedirectUrl = "/profile";
        User loggeduser = getUser();
        UserProfile userProfile = loggeduser.getProfile();
        MockMultipartFile multipartFile = new MockMultipartFile("image", new byte[0]);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggeduser);

        mockMvc.perform(multipart(urlTemplate)
                        .file(multipartFile)
                        .param("id", "2")
                        .flashAttr("userProfile", userProfile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl))
                .andReturn();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<UserProfile> userProfileArgumentCaptor = ArgumentCaptor.forClass(UserProfile.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        verify(fileUploadUtil, times(1)).saveImage(userProfileArgumentCaptor.capture(), any(MultipartFile.class), userArgumentCaptor.capture());
        UserProfile capturedProfile = userProfileArgumentCaptor.getValue();
        assertThat(capturedProfile).isSameAs(userProfile);

        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(loggeduser);
    }

    @Test
    @WithMockCustomUser
    void whenShowUserAccountEditForm_thenStatusIsOkAndModelAttributesAdded() throws Exception {
        //        Arrange
        User spyUser = spy(getUser());
        spyUser.setPasswordRepeat(null);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(spyUser);

        doAnswer(invocationOnMock -> {
            User modelUser = invocationOnMock.getArgument(0);
            Model model = invocationOnMock.getArgument(1);

            model.addAttribute("user", modelUser);
            model.addAttribute("userProfile", modelUser.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/account/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-account-edit-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        assertThat(spyUser.getPasswordRepeat()).isEqualTo(spyUser.getPassword());

        assertAll(
                () -> assertThat(modelAndView.getModel().get("user")).isSameAs(spyUser),
                () -> assertThat(modelAndView.getModel().get("userProfile")).isSameAs(spyUser.getProfile())
        );
    }

    @Test
    @WithMockCustomUser
    void thenProcessUserChangePasswordFormAndPasswordValid_thenStatusIsRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/account/change-password";
        String expectedRedirectUrl = "/profile";
        User loggedUser = getUser();

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedUser);

        doAnswer(invocationOnMock -> {
            User modelUser = invocationOnMock.getArgument(0);
            Model model = invocationOnMock.getArgument(1);

            model.addAttribute("user", modelUser);
            model.addAttribute("userProfile", modelUser.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("user", loggedUser)
                        .param("id", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).changePassword(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(loggedUser);
    }

    @Test
    @WithMockCustomUser
    void thenProcessUserChangePasswordFormAndPasswordIsInvalid_thenStatusIsOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/account/change-password";
        String expectedViewName = "user-account-edit-form";
        User loggedUser = getUser();
        loggedUser.setPassword(null);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedUser);

        doAnswer(invocationOnMock -> {
            User modelUser = invocationOnMock.getArgument(0);
            Model model = invocationOnMock.getArgument(1);

            model.addAttribute("user", modelUser);
            model.addAttribute("userProfile", modelUser.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("user", loggedUser)
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andExpect(model().attributeHasFieldErrors("user", "password"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        assertAll(
                () -> assertThat(modelAndView.getModel().get("user")).isEqualTo(loggedUser),
                () -> assertThat(modelAndView.getModel().get("userProfile")).isEqualTo(loggedUser.getProfile())
        );
        verify(userService, never()).changePassword(any(User.class));
    }

    @Test
    @WithMockCustomUser
    void whenShowAllDonations_thenStatusIsOkAndAllAttributesAddedToModel() throws Exception {
        //       Arrange
        String sortType = "testSortType";
        User loggedInUser = getUser();
        List<Donation> donations = new ArrayList<>(List.of(getDonation(), getDonation()));

        when(donationService.getDonationsForUserSortedBy(sortType, loggedInUser)).thenReturn(donations);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/donations").param("sortType", sortType))
                .andExpect(status().isOk())
                .andExpect(view().name("user-donations"))
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
        Long donationId = 1L;
        User loggedInUser = getUser();
        Donation donation = getDonation();

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
        MvcResult mvcResult = mockMvc.perform(get("/donations/{id}", donationId))
                .andExpect(status().isOk())
                .andExpect(view().name("user-donation-details"))
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
        Long donationId = 1L;
        User loggedInUser = getUser();
        String exceptionTitle = "Nie znaleziono";
        String exceptionMessage = "Dar nie istnieje";

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
        MvcResult mvcResult = mockMvc.perform(get("/donations/{id}", donationId))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
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

    private static User getUser() {
        UserProfile userProfile = new UserProfile(2L, null, "Mateusz", "Marcykiewicz", "Kielce",
                "Poland", null, "555666777");
        UserType userType = new UserType(2L, "ROLE_USER", new ArrayList<>());
        User user = new User(
                1L,
                "test@email.com",
                true,
                false,
                "testPW1!",
                LocalDateTime.of(2023, 11, 11, 12, 25, 11),
                "testPW1!",
                new HashSet<>(Set.of(userType)),
                userProfile,
                null,
                null,
                new ArrayList<>()
        );

        userProfile.setUser(user);
        return user;
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
}