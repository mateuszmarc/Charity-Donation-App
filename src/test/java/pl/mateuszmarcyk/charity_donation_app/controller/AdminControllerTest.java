package pl.mateuszmarcyk.charity_donation_app.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomAccessDeniedHandler;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.service.CategoryService;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.FileUploadUtil;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
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
    public void givenUserWithAdminRole_whenEnterDashboardEndpoint_thenStatusIsOkAndModelIsPopulated() throws Exception {
        String expectedEmail = "admin@admin.com";
        String expectedProfileFirstName = "Mateusz";
        String expectedProfileLastName = "Marcykiewicz";
        String expectedCity = "Kielce";
        String expectedCountry = "Poland";
        String expectedPhoneNumber = "555666777";
        MvcResult mvcResult = mockMvc.perform(get("/admins/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-dashboard"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertTrue(modelAndView.getModel().containsKey("user"));
        assertTrue(modelAndView.getModel().containsKey("userProfile"));

        User user = (User) modelAndView.getModel().get("user");
        UserProfile userProfile = (UserProfile) modelAndView.getModel().get("userProfile");

        assertAll(
                () -> assertThat(user).isNotNull(),
                () -> assertThat(userProfile).isNotNull(),
                () -> {
                    assert user != null;
                    assertThat(user.getEmail()).isEqualTo(expectedEmail);
                },
                () -> {
                    assert userProfile != null;
                    assertThat(userProfile.getFirstName()).isEqualTo(expectedProfileFirstName);
                    assertThat(userProfile.getLastName()).isEqualTo(expectedProfileLastName);
                    assertThat(userProfile.getCity()).isEqualTo(expectedCity);
                    assertThat(userProfile.getPhoneNumber()).isEqualTo(expectedPhoneNumber);
                    assertThat(userProfile.getCountry()).isEqualTo(expectedCountry);
                }
        );
    }

    @Test
    @WithMockCustomUser(email = "admin@admin.com", roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenEnterAddAdminsEndpoint_thenStatusIsOkAndModelIsPopulated() throws Exception {
//       Arrange
        String expectedEmail = "admin@admin.com";
        String expectedProfileFirstName = "Mateusz";
        String expectedProfileLastName = "Marcykiewicz";
        String expectedCity = "Kielce";
        String expectedCountry = "Poland";
        String expectedPhoneNumber = "555666777";
        List<User> admins = new ArrayList<>(List.of(new User(), new User()));
        when(userService.findAllAdmins(any(User.class))).thenReturn(admins);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(get("/admins/all-admins"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users-all"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertAll(
                () -> assertTrue(modelAndView.getModel().containsKey("user")),
                () -> assertTrue(modelAndView.getModel().containsKey("userProfile")),
                () -> assertTrue(modelAndView.getModel().containsKey("users"))
        );

        User user = (User) modelAndView.getModel().get("user");
        UserProfile userProfile = (UserProfile) modelAndView.getModel().get("userProfile");
        List allAdmins = (List) modelAndView.getModel().get("users");
        assertAll(
                () -> assertThat(user).isNotNull(),
                () -> assertThat(userProfile).isNotNull(),
                () -> {
                    assert user != null;
                    assertThat(user.getEmail()).isEqualTo(expectedEmail);
                },
                () -> {
                    assert userProfile != null;
                    assertThat(userProfile.getFirstName()).isEqualTo(expectedProfileFirstName);
                    assertThat(userProfile.getLastName()).isEqualTo(expectedProfileLastName);
                    assertThat(userProfile.getCity()).isEqualTo(expectedCity);
                    assertThat(userProfile.getPhoneNumber()).isEqualTo(expectedPhoneNumber);
                    assertThat(userProfile.getCountry()).isEqualTo(expectedCountry);
                },
                () -> assertIterableEquals(admins, allAdmins),
                () -> assertThat(allAdmins.get(0)).isSameAs(admins.get(0)),
                () -> assertThat(allAdmins.get(1)).isSameAs(admins.get(1))
        );

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)) .findAllAdmins(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();

        assertThat(capturedUser).isSameAs(user);

    }

}