package pl.mateuszmarcyk.charity_donation_app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;
import pl.mateuszmarcyk.charity_donation_app.entity.Category;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.service.CategoryService;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;
import pl.mateuszmarcyk.charity_donation_app.util.MessageDTO;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.mateuszmarcyk.charity_donation_app.GlobalTestMethodVerifier.*;
import static pl.mateuszmarcyk.charity_donation_app.TestDataFactory.*;
import static pl.mateuszmarcyk.charity_donation_app.UrlTemplates.USER_DONATION_FORM_URL;
import static pl.mateuszmarcyk.charity_donation_app.ViewNames.USER_DONATION_FORM_CONFIRMATION_VIEW;
import static pl.mateuszmarcyk.charity_donation_app.ViewNames.USER_DONATION_FORM_VIEW;

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

    private User loggedInUser;
    private Map<String, Object> expectedAttributes;
    private List<Category> categories;
    private List<Institution> institutions;

    @BeforeEach
    void setUp() {
        loggedInUser = getUser();
        stubLoggedUserModelHandlerMethodsInvocation(loggedUserModelHandler, loggedInUser);

        institutions = new ArrayList<>(List.of(getInstitution(), getInstitution()));
        categories = new ArrayList<>(List.of(getCategory(), getCategory()));

        expectedAttributes = new HashMap<>(Map.of(
                "user", loggedInUser,
                "userProfile", loggedInUser.getProfile(),
                "institutions", institutions,
                "allCategories", categories
        ));

    }

    @Test
    @WithMockCustomUser
    void whenShowDonationForm_thenStatusIsOkModelAttributesAddedAndViewRendered() throws Exception {
        // Arrange
        String urlTemplate = USER_DONATION_FORM_URL;
        String expectedView = USER_DONATION_FORM_VIEW;

        when(institutionService.findAll()).thenReturn(institutions);
        when(categoryService.findAll()).thenReturn(categories);

        // Act
        MvcResult mvcResult = mockMvc.perform(get(urlTemplate)).andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedView, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(institutionService, times(1)).findAll(),
                () -> verify(categoryService, times(1)).findAll(),
                () -> {
                    Donation donation = (Donation) mvcResult.getModelAndView().getModel().get("donation");
                    assertEmptyDonation(donation);
                },
                () -> {
                    MessageDTO messageDTO = (MessageDTO) mvcResult.getModelAndView().getModel().get("message");
                    assertThat(messageDTO.getEmail()).isEqualTo(loggedInUser.getEmail());
                }
        );
    }

    @Test
    @WithMockCustomUser
    void whenProcessDonationFormAndDonationIsValid_thenDonationSavedAndStatusIsOkAndViewRendered() throws Exception {
        // Arrange
        String urlTemplate = USER_DONATION_FORM_URL;
        String expectedViewName = USER_DONATION_FORM_CONFIRMATION_VIEW;

        Long id = 1L;
        Donation spyDonationToSave = spy(getDonation());
        when(userService.findUserById(id)).thenReturn(loggedInUser);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("donation", spyDonationToSave))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> {
                    ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
                    verify(userService).findUserById(longArgumentCaptor.capture());
                    Long capturedUserId = longArgumentCaptor.getValue();
                    assertThat(capturedUserId).isEqualTo(id);
                },
                () -> {
                    ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
                    verify(spyDonationToSave).setUser(userArgumentCaptor.capture());
                    User capturedUser = userArgumentCaptor.getValue();
                    assertThat(capturedUser).isSameAs(loggedInUser);
                },
                () -> {
                    ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
                    verify(donationService).save(donationArgumentCaptor.capture());
                    Donation capturedDonation = donationArgumentCaptor.getValue();
                    assertThat(capturedDonation).isSameAs(spyDonationToSave);
                },
                () -> {
                    MessageDTO messageDTO = (MessageDTO) mvcResult.getModelAndView().getModel().get("message");
                    assertThat(messageDTO.getEmail()).isEqualTo(loggedInUser.getEmail());
                }
        );
    }

    @Test
    @WithMockCustomUser
    void whenProcessDonationFormAndDonationIsInvalid_thenModelContainsErrorsAndViewRendered() throws Exception {
        // Arrange
        String urlTemplate = USER_DONATION_FORM_URL;
        String expectedViewName = USER_DONATION_FORM_VIEW;
        String errorMessage = "You have errors in your form";

        Donation invalidDonation = spy(getDonation());
        invalidDonation.setQuantity(null);
        invalidDonation.setDonationPassedTime(LocalDateTime.now().plusDays(5));

        when(institutionService.findAll()).thenReturn(institutions);
        when(categoryService.findAll()).thenReturn(categories);
        when(messageSource.getMessage("donation.form.error.message", null, Locale.getDefault())).thenReturn(errorMessage);

        expectedAttributes.put("errorMessage", errorMessage);

        // Act
        MvcResult mvcResult = mockMvc.perform(post(urlTemplate)
                        .flashAttr("donation", invalidDonation))
                .andExpect(model().attributeHasFieldErrors("donation", "quantity"))
                .andReturn();

        // Assert
        assertAll(
                () -> assertMvcResult(mvcResult, expectedViewName, 200),
                () -> verifyInvocationOfLoggedUserModelHandlerMethods(loggedUserModelHandler),
                () -> verify(categoryService, times(1)).findAll(),
                () -> verify(institutionService, times(1)).findAll(),
                () -> verify(messageSource, times(1)).getMessage("donation.form.error.message", null, Locale.getDefault()),
                () -> assertModelAndViewAttributes(mvcResult, expectedAttributes),
                () -> verify(userService, never()).findUserById(any(Long.class)),
                () -> verify(invalidDonation, never()).setUser(any(User.class)),
                () -> verify(donationService, never()).save(any(Donation.class))
        );

    }



}