package pl.mateuszmarcyk.charity_donation_app.config.security;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.mateuszmarcyk.charity_donation_app.entity.Category;
import pl.mateuszmarcyk.charity_donation_app.entity.Donation;
import pl.mateuszmarcyk.charity_donation_app.entity.Institution;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    DonationService donationService;


    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/admin-get-method-urls.csv")
    @WithMockCustomUser(email = "mati@gmail.com", roles = {"ROLE_ADMIN"})
    void givenAdminWithAdminRole_whenAccessAdminRestrictedEndpointWithGetMethod_thenStatusIsOkAndViewIsRendered(String url, String view) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(view));
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/admin-get-method-urls.csv")
    @WithMockCustomUser(email = "mati@gmail.com")
    void givenAdminWithUserRole_whenAccessAdminRestrictedEndpointWithGetMethod_thenStatusIsOkAndViewIsRendered(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/error/403"));
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/admin-get-method-urls.csv")
    @WithAnonymousUser
    void givenAnonymousUser_whenAccessAdminRestrictedEndpointWithGetMethod_thenRedirectedToLogin(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/user-get-method-urls.csv")
    @WithMockCustomUser
    void givenUserWithUserRole_whenAccessUserRestrictedEndpointWithGetMethod_thenStatusIsOkAndViewIsRendered(String url, String view) throws Exception {

        when(donationService.getDonationsForUserSortedBy(any(String.class), any(User.class))).thenReturn(new ArrayList<>());
        when(donationService.getDonationById(1L)).thenReturn(getDonation());

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(view));
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/user-get-method-urls.csv")
    @WithMockCustomUser(roles = {"ADMIN_ROLE"})
    void givenUserWithAdminRole_whenAccessUserRestrictedEndpointWithGetMethod_thenAccessDenied(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/error/403"));
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/user-get-method-urls.csv")
    @WithAnonymousUser
    void givenUnauthenticatedUser_whenAccessUserRestrictedEndpointWithGetMethod_thenAccessDenied(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }



    public static Donation getDonation() {
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
        donationOne.setCreated(LocalDateTime.now());

        institution.getDonations().add(donationOne);
        donationOne.setInstitution(institution);

        user.getDonations().add(donationOne);
        donationOne.setUser(user);

        category.getDonations().add(donationOne);
        donationOne.getCategories().add(category);

        return donationOne;
    }
}