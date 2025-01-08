package pl.mateuszmarcyk.charity_donation_app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;
import pl.mateuszmarcyk.charity_donation_app.util.MailMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private  UserService userService;

    @MockBean
    private  DonationService donationService;

    @MockBean
    private  InstitutionService institutionService;

    @MockBean
    private  AppMailSender appMailSender;

    @MockBean
    private  MailMessage mailMessageHelper;

    @MockBean
    private  LoggedUserModelHandler loggedUserModelHandler;

    @Test
    @WithAnonymousUser
    void givenUnauthenticatedUser_whenIndex_thenStatusIsOkAndModelAttributesAdded() throws Exception {
//        Arrange
        List<Institution> institutions = new ArrayList<>(List.of(getInstitution(), getInstitution()));
        Integer countedBags = 100;
        Integer countedDonations = 10;

        when(institutionService.findAll()).thenReturn(institutions);
        when(donationService.countAllBags()).thenReturn(countedBags);
        when(donationService.countAllDonations()).thenReturn(countedDonations);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, never()).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, never()).addUserToModel(any(User.class), any(Model.class));

        verify(institutionService, times(1)).findAll();
        verify(donationService, times(1)).countAllBags();
        verify(donationService, times(1)).countAllDonations();

        assertAll(
                () -> assertIterableEquals(institutions, (List) modelAndView.getModel().get("institutions")),
                () -> assertThat(modelAndView.getModel().get("allDonations")).isEqualTo(countedDonations),
                () -> assertThat(modelAndView.getModel().get("allDonationBags")).isEqualTo(countedBags)
        );
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenIndex_thenStatusIsOkAndModelAttributesAdded() throws Exception {
//       Arrange
        User loggedInUser = getUser();
        List<Institution> institutions = new ArrayList<>(List.of(getInstitution(), getInstitution()));
        Integer countedBags = 100;
        Integer countedDonations = 10;

        when(institutionService.findAll()).thenReturn(institutions);
        when(donationService.countAllBags()).thenReturn(countedBags);
        when(donationService.countAllDonations()).thenReturn(countedDonations);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);

        doAnswer(invocation -> {

            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        verify(institutionService, times(1)).findAll();
        verify(donationService, times(1)).countAllBags();
        verify(donationService, times(1)).countAllDonations();

        assertAll(
                () -> assertIterableEquals(institutions, (List) modelAndView.getModel().get("institutions")),
                () -> assertThat(modelAndView.getModel().get("allDonations")).isEqualTo(countedDonations),
                () -> assertThat(modelAndView.getModel().get("allDonationBags")).isEqualTo(countedBags)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_USER", "ROLE_ADMIN"})
    void givenUserWithBothUserRoleAndAdminRole_whenIndex_thenStatusIsOkAndModelAttributesAdded() throws Exception {
//       Arrange
        User loggedInUser = getUser();
        List<Institution> institutions = new ArrayList<>(List.of(getInstitution(), getInstitution()));
        Integer countedBags = 100;
        Integer countedDonations = 10;

        when(institutionService.findAll()).thenReturn(institutions);
        when(donationService.countAllBags()).thenReturn(countedBags);
        when(donationService.countAllDonations()).thenReturn(countedDonations);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);

        doAnswer(invocation -> {

            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        //        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));

        verify(institutionService, times(1)).findAll();
        verify(donationService, times(1)).countAllBags();
        verify(donationService, times(1)).countAllDonations();

        assertAll(
                () -> assertIterableEquals(institutions, (List) modelAndView.getModel().get("institutions")),
                () -> assertThat(modelAndView.getModel().get("allDonations")).isEqualTo(countedDonations),
                () -> assertThat(modelAndView.getModel().get("allDonationBags")).isEqualTo(countedBags)
        );
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenIndex_thenStatusIsRedirected() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admins/dashboard"));

        verify(loggedUserModelHandler, never()).getUser(any(CustomUserDetails.class));
        verify(loggedUserModelHandler,  never()).addUserToModel(any(User.class), any(Model.class));

        verify(institutionService,  never()).findAll();
        verify(donationService,  never()).countAllBags();
        verify(donationService,  never()).countAllDonations();
    }


    private static Institution getInstitution() {
        return new Institution(1L, "test name", "test description", new ArrayList<>());
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