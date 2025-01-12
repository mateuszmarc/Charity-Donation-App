package pl.mateuszmarcyk.charity_donation_app.config.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;
import pl.mateuszmarcyk.charity_donation_app.service.CategoryService;
import pl.mateuszmarcyk.charity_donation_app.service.DonationService;
import pl.mateuszmarcyk.charity_donation_app.service.InstitutionService;
import pl.mateuszmarcyk.charity_donation_app.service.UserService;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DonationService donationService;

    @MockBean
    private LoggedUserModelHandler loggedUserModelHandler;

    @MockBean
    private UserService userService;

    @MockBean
    private InstitutionService institutionService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserRepository userRepository;


    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/admin-get-method-urls.csv")
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenAccessAdminRestrictedEndpointWithGetMethod_thenAccessIsGranted(String url, String view) throws Exception {
//      Arrange
        User loggedUser = getUser();
        Donation donation = getDonation();
        Category category = donation.getCategories().get(0);
        Institution institution = donation.getInstitution();

//        Stub all dependency methods that return some object
        when(donationService.getDonationsForUserSortedBy(any(String.class), any(User.class))).thenReturn(new ArrayList<>());
        when(donationService.findDonationById(1L)).thenReturn(getDonation());
        when(userService.findAllAdmins(any(User.class))).thenReturn(new ArrayList<>());
        when(userService.findAllUsers(any(User.class))).thenReturn(new ArrayList<>());
        when(userService.findUserById(any(Long.class))).thenReturn(loggedUser);
        when(donationService.findAll(any(String.class))).thenReturn(new ArrayList<>());
        when(donationService.findDonationById(any(Long.class))).thenReturn(donation);
        when(categoryService.findAll()).thenReturn(new ArrayList<>());
        when(categoryService.findCategoryById(any(Long.class))).thenReturn(category);
        when(institutionService.findAll()).thenReturn(new ArrayList<>());
        when(institutionService.findInstitutionById(any(Long.class))).thenReturn(institution);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(view));
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/admin-get-method-urls.csv")
    @WithMockCustomUser(roles = {"ROLE_ADMIN", "ROLE_USER"})
    void givenUserWithBothAdminAndUserRole_whenAccessAdminRestrictedEndpointWithGetMethod_thenAccessIsGranted(String url, String view) throws Exception {
//      Arrange
        User loggedUser = getUser();
        Donation donation = getDonation();
        Category category = donation.getCategories().get(0);
        Institution institution = donation.getInstitution();

//        Stub all dependency methods that return some object
        when(donationService.getDonationsForUserSortedBy(any(String.class), any(User.class))).thenReturn(new ArrayList<>());
        when(donationService.findDonationById(1L)).thenReturn(getDonation());
        when(userService.findAllAdmins(any(User.class))).thenReturn(new ArrayList<>());
        when(userService.findAllUsers(any(User.class))).thenReturn(new ArrayList<>());
        when(userService.findUserById(any(Long.class))).thenReturn(loggedUser);
        when(donationService.findAll(any(String.class))).thenReturn(new ArrayList<>());
        when(donationService.findDonationById(any(Long.class))).thenReturn(donation);
        when(categoryService.findAll()).thenReturn(new ArrayList<>());
        when(categoryService.findCategoryById(any(Long.class))).thenReturn(category);
        when(institutionService.findAll()).thenReturn(new ArrayList<>());
        when(institutionService.findInstitutionById(any(Long.class))).thenReturn(institution);

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(view));
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/admin-get-method-urls.csv")
    @WithMockCustomUser
    void givenUserWithUserRole_whenAccessAdminRestrictedEndpointWithGetMethod_thenAccessDenied(String url) throws Exception {
//      Arrange
        String expectedForwardedUrl = "/error/403";

//        Act & Assert
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(expectedForwardedUrl));
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/admin-get-method-urls.csv")
    @WithAnonymousUser
    void givenAnonymousUser_whenAccessAdminRestrictedEndpointWithGetMethod_thenAccessDeniedAndRedirectedToLogin(String url) throws Exception {
//      Arrange
        String expectedRedirectUrl = "http://localhost/login";

//        Act & Assert
        mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenProcessUserProfileDetailsEditForm_thenAccessDeniedAndStatusIsRedirected() throws Exception {
//        Arrange
        Long profileId = 1L;
        User profileOwner = getUser();
        profileOwner.setId(2L);
        User loggedInUser = getUser();
        UserProfile changedUserProfile = loggedInUser.getProfile();

        String endpoint = "/admins/users/profiles/edit";
        String expectedRedirectedUrl = "/admins/users/profiles/" + profileOwner.getId();

        when(userService.findUserByProfileId(profileId)).thenReturn(profileOwner);

//        Act & Assert
        mockMvc.perform(multipart(endpoint)
                        .file(new MockMultipartFile("image", new byte[0]))
                        .param("id", profileId.toString())
                        .flashAttr("profile", changedUserProfile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl))
                .andReturn();
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN", "ROLE_USER"})
    void givenUserWithBothUserAndAdminRole_whenProcessUserProfileDetailsEditForm_thenAccessDeniedAndStatusIsRedirected() throws Exception {
//        Arrange
        Long profileId = 1L;
        User profileOwner = getUser();
        profileOwner.setId(2L);
        User loggedInUser = getUser();
        UserProfile changedUserProfile = loggedInUser.getProfile();

        String endpoint = "/admins/users/profiles/edit";
        String expectedRedirectedUrl = "/admins/users/profiles/" + profileOwner.getId();

        when(userService.findUserByProfileId(profileId)).thenReturn(profileOwner);

//        Act & Assert
        mockMvc.perform(multipart(endpoint)
                        .file(new MockMultipartFile("image", new byte[0]))
                        .param("id", profileId.toString())
                        .flashAttr("profile", changedUserProfile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl))
                .andReturn();
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessUserProfileDetailsEditForm_thenAccessDenied() throws Exception {
//        Arrange
        String endpoint = "/admins/users/profiles/edit";
        String expectedForwardedUrl = "/error/403";

        User loggedInUser = getUser();
        UserProfile changedUserProfile = loggedInUser.getProfile();

//        Act & Assert
        mockMvc.perform(multipart(endpoint)
                        .file(new MockMultipartFile("image", new byte[0]))
                        .param("id", "1")
                        .flashAttr("profile", changedUserProfile)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(expectedForwardedUrl));
    }


    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessUserProfileDetailsEditForm_thenAccessDeniedAndStatusIsRedirected() throws Exception {
//        Arrange
        String endpoint = "/admins/users/profiles/edit";
        String expectedRedirectUrl = "http://localhost/login";

        User loggedInUser = getUser();
        UserProfile changedUserProfile = loggedInUser.getProfile();

//        Act & Assert
        mockMvc.perform(multipart(endpoint)
                        .file(new MockMultipartFile("image", new byte[0]))
                        .param("id", "1")
                        .flashAttr("profile", changedUserProfile)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenProcessChangeEmailForm_thenAccessIsGranted() throws Exception {
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
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl))
                .andReturn();
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN", "ROLE_USER"})
    void givenUserWithBothUserAndAdminRole_whenProcessChangeEmailForm_thenAccessIsGranted() throws Exception {
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
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl))
                .andReturn();
    }


    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenProcessChangePasswordForm_thenAccessIsGranted() throws Exception {
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
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectedUrl));
    }

    @ParameterizedTest
    @CsvSource({"/admins/users/change-password", "/admins/users/change-email"})
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessChangePasswordFormOrProcessChangeEmailForm_thenAccessIsDenied(String urlTemplate) throws Exception {
        //        Arrange
        User userToEdit = getUser();
        userToEdit.setId(22L);
        String expectedForwardedUrl = "/error/403";

        //        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(expectedForwardedUrl))
                .andReturn();
    }

    @ParameterizedTest
    @CsvSource({"/admins/users/change-password", "/admins/users/change-email"})
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessChangePasswordFormOrProcessChangeEmailForm_thenAccessDeniedAndStatusIsRedirected(String urlTemplate) throws Exception {
        //        Arrange
        User userToEdit = getUser();
        userToEdit.setId(22L);
        String expectedRedirectUrl = "http://localhost/login";

        //        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("userToEdit", userToEdit)
                        .param("id", String.valueOf(userToEdit.getId()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl))
                .andReturn();
    }

    @ParameterizedTest(name = "urlTemplate={0}, redirectUrl={1}")
    @CsvSource({
            "/admins/users/delete,         /admins/users",
            "/admins/donations/archive,    /admins/donations",
            "/admins/donations/unarchive,  /admins/donations",
            "/admins/donations/delete,     /admins/donations",
            "/admins/categories/delete,    /admins/categories",
            "/admins/institutions/delete,  /admins/institutions"
    })
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenAccessEndpointsWithPostRequest_thenAccessIsGrantedAndStatusRedirected(String urlTemplate, String redirectUrl) throws Exception {
//       Arrange
        long id = 1L;

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", Long.toString(id))
                        .param("donationId", Long.toString(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectUrl));
    }

    @ParameterizedTest(name = "urlTemplate={0}, redirectUrl={1}")
    @CsvSource({
            "/admins/users/delete,         /admins/users",
            "/admins/donations/archive,    /admins/donations",
            "/admins/donations/unarchive,  /admins/donations",
            "/admins/donations/delete,     /admins/donations",
            "/admins/categories/delete,    /admins/categories",
            "/admins/institutions/delete,  /admins/institutions"
    })
    @WithMockCustomUser(roles = {"ROLE_ADMIN", "ROLE_USER"})
    void givenUserWithBothUserAndAdminRole_whenAccessEndpointsWithPostRequest_thenAccessIsGrantedAndStatusRedirected(String urlTemplate, String redirectUrl) throws Exception {
//       Arrange
        long id = 1L;

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", Long.toString(id))
                        .param("donationId", Long.toString(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(redirectUrl));
    }

    @ParameterizedTest(name = "urlTemplate={0}")
    @CsvSource({
            "/admins/users/delete",
            "/admins/donations/archive",
            "/admins/donations/unarchive",
            "/admins/donations/delete",
            "/admins/categories/delete",
            "/admins/institutions/delete"
    })
    @WithMockCustomUser
    void givenUserWithUserRole_whenAccessEndpointsWithPostRequest_thenAccessDenied(String urlTemplate) throws Exception {
//       Arrange
        long id = 1L;
        String expectedForwardedUrl = "/error/403";

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", Long.toString(id))
                        .param("donationId", Long.toString(id)))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(expectedForwardedUrl));
    }

    @ParameterizedTest(name = "urlTemplate={0}")
    @CsvSource({
            "/admins/users/delete",
            "/admins/donations/archive",
            "/admins/donations/unarchive",
            "/admins/donations/delete",
            "/admins/categories/delete",
            "/admins/institutions/delete"
    })
    @WithAnonymousUser
    void givenAnonymousUser_whenAccessEndpointsWithPostRequest_thenAccessDeniedAndStatusIsRedirected(String urlTemplate) throws Exception {
//       Arrange
        long id = 1L;
        String expectedRedirectUrl = "http://localhost/login";

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .param("id", Long.toString(id))
                        .param("donationId", Long.toString(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenProcessCategoryForm_thenAccessIsGrantedAndStatusRedirected() throws Exception {
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
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("category", categoryToAdd)
                        .param("id", categoryToAdd.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithBothUserAndAdminRole_whenProcessCategory_thenAccessIsGrantedAndStatusRedirected() throws Exception {
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
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("category", categoryToAdd)
                        .param("id", categoryToAdd.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessCategoryForm_thenAccessIsDenied() throws Exception {
//        Arrange
        String urlTemplate = "/admins/categories/add";
        String expectedForwardedUrl = "/error/403";
        Category categoryToAdd = getCategory();


//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("category", categoryToAdd)
                        .param("id", categoryToAdd.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(expectedForwardedUrl));
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessCategoryForm_thenAccessIsDeniedAndStatusRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/admins/categories/add";
        String expectedRedirectUrl = "http://localhost/login";
        Category categoryToAdd = getCategory();

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("category", categoryToAdd)
                        .param("id", categoryToAdd.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN"})
    void givenUserWithAdminRole_whenProcessInstitutionForm_thenAccessIsGrantedAndStatusRedirected() throws Exception {
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
    }

    @Test
    @WithMockCustomUser(roles = {"ROLE_ADMIN", "USER_ROLE"})
    void givenUserWithBothUserAndAdminRole_whenProcessInstitutionForm_thenAccessIsGrantedAndStatusRedirected() throws Exception {
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
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserAdminRole_whenProcessInstitutionForm_thenAccessIsDenied() throws Exception {
//        Arrange
        String urlTemplate = "/admins/institutions/add";
        String expectedForwardedUrl = "/error/403";

        Institution institutionToAdd = getInstitution();

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("institution", institutionToAdd))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl(expectedForwardedUrl));
    }

    @Test
    @WithAnonymousUser
    void givenAnonymousUser_whenProcessInstitutionForm_thenAccessIsDeniedAndStatusRedirected() throws Exception {
//        Arrange
        String urlTemplate = "/admins/institutions/add";
        String expectedRedirectUrl = "http://localhost/login";
        Institution institutionToAdd = getInstitution();

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("institution", institutionToAdd))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(expectedRedirectUrl));
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/user-get-method-urls.csv")
    @WithMockCustomUser
    void givenUserWithUserRole_whenAccessUserRestrictedEndpointWithGetMethod_thenStatusIsOkAndViewIsRendered(String url, String view) throws Exception {
//        Arrange
        when(donationService.getDonationsForUserSortedBy(any(String.class), any(User.class))).thenReturn(new ArrayList<>());
        when(donationService.getUserDonationById(any(User.class), any(Long.class))).thenReturn(getDonation());
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

    @ParameterizedTest(name = "url={0}")
    @CsvSource({"/css", "/js", "/images"})
    @WithMockCustomUser(roles = {"ROLE_ADMIN", "ROLE_USER"})
    void givenUserWithBothAdminAndUserRoles_whenAccessPublicEndpointWithGetMethod_thenStatusIsOk(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "url={0}")
    @CsvSource({"/css", "/js", "/images"})
    @WithMockCustomUser
    void givenUserWithUserRole_whenAccessPublicEndpointWithGetMethod_thenStatusIsOk(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "url={0}")
    @CsvSource({"/css", "/js", "/images"})
    @WithMockCustomUser(roles = {"ADMIN_ROLE"})
    void givenUserWithAdminRole_whenAccessPublicEndpointWithGetMethod_thenStatusIsOk(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "url={0}")
    @CsvSource({"/css", "/js", "/images"})
    @WithAnonymousUser
    void givenUnauthenticatedUser_whenAccessPublicEndpointWithGetMethod_thenStatusIsOk(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/public-get-method-urls.csv")
    @WithAnonymousUser
    void givenUnauthenticatedUser_whenAccessPublicEndpointWithGetMethod_thenStatusIsOkAndViewRendered(String url, String view) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(view));
    }

    @Test
    @WithMockCustomUser
    void givenUserWithUserRole_whenProcessDonationFormAndDonationIsValid_thenDonationSavedAndStatusIsOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/donate";
        String expectedViewName = "form-confirmation";

        User loggedUser = getUser();
        Donation spyDonationToSave = getDonation();
        spyDonationToSave.setDonationPassedTime(LocalDateTime.now().plusDays(5));

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("donation", spyDonationToSave))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();
    }

    @Test
    @WithMockCustomUser
    void whenProcessDonationFormAndDonationIsValid_thenDonationSavedAndStatusIsOkAndViewRendered() throws Exception {
//        Arrange
        String urlTemplate = "/donate";
        String expectedViewName = "form-confirmation";

        User loggedUser = getUser();
        Donation spyDonationToSave = getDonation();
        spyDonationToSave.setDonationPassedTime(LocalDateTime.now().plusDays(5));

        when(loggedUserModelHandler.getUser(any(CustomUserDetails.class))).thenReturn(loggedUser);
        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            Model model = invocation.getArgument(1);

            model.addAttribute("user", user);
            model.addAttribute("userProfile", user.getProfile());
            return null;
        }).when(loggedUserModelHandler).addUserToModel(any(User.class), any(Model.class));

//        Act & Assert
        mockMvc.perform(post(urlTemplate)
                        .flashAttr("donation", spyDonationToSave))
                .andExpect(status().isOk())
                .andExpect(view().name(expectedViewName))
                .andReturn();
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

    private static Category getCategory() {
        return new Category(1L, "CategoryName", new ArrayList<>());
    }
}