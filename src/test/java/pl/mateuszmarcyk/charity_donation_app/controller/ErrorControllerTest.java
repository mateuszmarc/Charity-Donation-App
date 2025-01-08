package pl.mateuszmarcyk.charity_donation_app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;
import pl.mateuszmarcyk.charity_donation_app.config.security.WithMockCustomUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ErrorController.class)
class ErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockCustomUser
    void givenErrorController_whenAccessDenied_thenStatusIsOkAndModelAttributesAdded() throws Exception {
//        Arrange
        String errorTitle = "Odmowa dostępu";
        String errorMessage = "Nie masz uprawnień aby wejść na stronę";

//
        MvcResult mvcResult = mockMvc.perform(get("/error/403"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"))
                .andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        assertThat(modelAndView).isNotNull();

        assertAll(
                () -> assertThat(modelAndView.getModel().get("errorTitle")).isEqualTo(errorTitle),
                () -> assertThat(modelAndView.getModel().get("errorMessage")).isEqualTo(errorMessage)
        );
    }
}