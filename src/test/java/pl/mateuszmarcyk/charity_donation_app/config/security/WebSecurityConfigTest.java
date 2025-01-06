package pl.mateuszmarcyk.charity_donation_app.config.security;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;


    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/admin-get-method-urls.csv")
    @WithMockCustomUser( email ="mati@gmail.com", roles = {"ROLE_ADMIN"}, enabled = "true", blocked = "false")
    void givenAdminWithAdminRole_whenAccessAdminRestrictedEndpoint_thenStatusIsOkAndViewIsRendered(String url, String view) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name(view));
    }

    @ParameterizedTest(name = "url={0}, view={1}")
    @CsvFileSource(resources = "/security/admin-get-method-urls.csv")
    @WithMockCustomUser( email ="mati@gmail.com", roles = {"ROLE_USER"}, enabled = "true", blocked = "false")
    void givenAdminWithUserRole_whenAccessAdminRestrictedEndpoint_thenStatusIsOkAndViewIsRendered(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/error/403"));
    }

}