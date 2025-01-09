package pl.mateuszmarcyk.charity_donation_app.controller;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.service.CategoryService;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class DonationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InstitutionService institutionService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private DonationService donationService;

    @MockBean
    private UserService userService;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private LoggedUserModelHandler loggedUserModelHandler;

    @Test
    @WithMockCustomUser
    void whenShowDonationForm_thenStatusIsOkModelAttributesAddedAndViewRendered() throws Exception {
//        Arrange
        List<Institution> institutions = new ArrayList<>(List.of(getInstitution(), getInstitution()));
        List<Category> categories = new ArrayList<>(List.of(getCategory(), getCategory()));
        User loggedInUser = getUser();

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedInUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        when(institutionService.findAll()).thenReturn(institutions);
        when(categoryService.findAll()).thenReturn(categories);

//            Act & Assert
        MvcResult mvcResult = mockMvc
                .perform(get("/donate"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-donation-form"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        verify(institutionService, times(1)).findAll();
        verify(categoryService, times(1)).findAll();

        assertAll(
                () -> assertIterableEquals(institutions, (List) modelAndView.getModel().get("institutions")),
                () -> assertIterableEquals(categories, (List) modelAndView.getModel().get("allCategories")),
                () -> assertThat(modelAndView.getModel().get("donation")).isNotNull()
        );
    }

    @Test
    @WithMockCustomUser
    void whenProcessDonationFormAndDonationValid_thenDonationSavedAndStatusIsOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/donate";
        String expectedViewName = "form-confirmation";

        Long userId = 1L;
        User loggedUser = getUser();
        Donation donationToSave = spy(getDonation());
        donationToSave.setDonationPassedTime(LocalDateTime.now().plusDays(5));

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

        when(userService.findUserById(userId)).thenReturn(loggedUser);

//        Act & Assert
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("donation", donationToSave))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userService).findUserById(longArgumentCaptor.capture());
        Long capturedUserId = longArgumentCaptor.getValue();
        assertThat(capturedUserId).isEqualTo(userId);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(donationToSave).setUser(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(loggedUser);

        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
        verify(donationService, times(1)).save(donationArgumentCaptor.capture());
        Donation capturedDonation = donationArgumentCaptor.getValue();
        assertThat(capturedDonation).isSameAs(donationToSave);

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
                LocalDateTime.now(),
                false,
                user,
                institution,
                new ArrayList<>(List.of(category)),
                "444555666",
                "Please call on arrival.",
                LocalTime.parse("10:30:00"),
                LocalDate.now().plusDays(10),
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

}